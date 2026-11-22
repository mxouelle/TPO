package ar.edu.uade.tenisskillgames.service;

import ar.edu.uade.tenisskillgames.controller.dto.Vistas;
import ar.edu.uade.tenisskillgames.domain.enums.CanalNotificacion;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoEnvio;
import ar.edu.uade.tenisskillgames.domain.enums.TipoEvento;
import ar.edu.uade.tenisskillgames.domain.model.Notificacion;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.notification.Notifier;
import ar.edu.uade.tenisskillgames.domain.notification.NotifierFactory;
import ar.edu.uade.tenisskillgames.domain.port.NotificacionRepository;
import ar.edu.uade.tenisskillgames.controller.mapper.VistaMapper;
import ar.edu.uade.tenisskillgames.infrastructure.notification.DespachadorAsincrono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Servicio de notificaciones multicanal (RF7). Usa la Abstract Factory para
 * obtener la familia de notificadores y aplica reintento exponencial con
 * fallback a log y estado FALLIDO en la tabla de auditoria.
 */
@Service
public class NotificacionService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacionService.class);

    private final NotificacionRepository repository;
    private final NotifierFactory notifierFactory;
    private final DespachadorAsincrono despachador;
    private final VistaMapper mapper;
    private final int reintentos;
    private final long backoffInicialMs;

    public NotificacionService(NotificacionRepository repository,
                               NotifierFactory notifierFactory,
                               DespachadorAsincrono despachador,
                               VistaMapper mapper,
                               @Value("${tenis.notificaciones.reintentos:3}") int reintentos,
                               @Value("${tenis.notificaciones.backoff-inicial-ms:200}") long backoffInicialMs) {
        this.repository = repository;
        this.notifierFactory = notifierFactory;
        this.despachador = despachador;
        this.mapper = mapper;
        this.reintentos = reintentos;
        this.backoffInicialMs = backoffInicialMs;
    }

    /**
     * Crea y despacha una notificacion por cada canal habilitado del usuario.
     * La persistencia corre en su propia transaccion (REQUIRES_NEW) y el envio
     * se dispara recien despues del commit, de modo que el despacho asincrono
     * siempre encuentre la fila ya confirmada.
     */
    @org.springframework.transaction.annotation.Transactional(
            propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void enviar(Usuario destinatario, UUID partidoId, TipoEvento tipoEvento,
                       String asunto, String contenido, String adjuntoNombre, String adjuntoContenido) {
        if (destinatario == null) {
            return;
        }
        List<Notificacion> creadas = new java.util.ArrayList<>();

        for (CanalNotificacion canal : destinatario.getCanalesHabilitados()) {
            if (!canalDisponible(destinatario, canal)) {
                continue;
            }
            Notificacion notificacion = new Notificacion(destinatario, partidoId, tipoEvento,
                    canal, asunto, contenido);
            if (canal == CanalNotificacion.EMAIL && adjuntoContenido != null) {
                notificacion.setAdjuntoNombre(adjuntoNombre);
                notificacion.setAdjuntoContenido(adjuntoContenido);
            }
            repository.guardar(notificacion);
            creadas.add(notificacion);
        }

        Runnable despacho = () -> creadas.forEach(n -> despachador.ejecutar(() -> despachar(n)));

        if (org.springframework.transaction.support.TransactionSynchronizationManager
                .isSynchronizationActive()) {
            org.springframework.transaction.support.TransactionSynchronizationManager
                    .registerSynchronization(new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            despacho.run();
                        }
                    });
        } else {
            despacho.run();
        }
    }

    /**
     * Variante sin adjunto (delega en la version completa).
     */
    public void enviar(Usuario destinatario, UUID partidoId, TipoEvento tipoEvento,
                       String asunto, String contenido) {
        enviar(destinatario, partidoId, tipoEvento, asunto, contenido, null, null);
    }

    private boolean canalDisponible(Usuario u, CanalNotificacion canal) {
        return switch (canal) {
            case EMAIL -> u.getEmail() != null && !u.getEmail().isBlank();
            case CHAT -> u.getTelefono() != null && !u.getTelefono().isBlank();
            case PUSH -> u.getPushToken() != null && !u.getPushToken().isBlank();
        };
    }

    /**
     * Envio con reintento exponencial (maximo 3 intentos por defecto).
     */
    void despachar(Notificacion notificacion) {
        Notifier notifier = notifierPara(notificacion.getCanal());
        RuntimeException ultimoError = null;

        for (int intento = 1; intento <= reintentos; intento++) {
            try {
                notificacion.registrarIntento();
                notifier.enviar(notificacion);
                notificacion.marcarEnviada();
                repository.guardar(notificacion);
                return;
            } catch (RuntimeException ex) {
                ultimoError = ex;
                notificacion.marcarFallida(describir(ex));
                esperar(intento);
            }
        }

        repository.guardar(notificacion);
        LOG.warn("Notificacion {} FALLIDA tras {} intentos (canal {}): {}", notificacion.getId(),
                notificacion.getIntentos(), notificacion.getCanal(), describir(ultimoError));
    }

    private Notifier notifierPara(CanalNotificacion canal) {
        return switch (canal) {
            case PUSH -> notifierFactory.createPushNotifier();
            case EMAIL -> notifierFactory.createEmailNotifier();
            case CHAT -> notifierFactory.createChatNotifier();
        };
    }

    private void esperar(int intento) {
        if (intento >= reintentos) {
            return;
        }
        long espera = backoffInicialMs * (1L << (intento - 1));
        if (espera <= 0) {
            return;
        }
        try {
            Thread.sleep(espera);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private String describir(Throwable ex) {
        return ex == null ? "error desconocido" : ex.getClass().getSimpleName() + ": " + ex.getMessage();
    }

    // ------------------------------------------------------------------ consultas

    public List<Vistas.Notificacion> listarPorUsuario(UUID usuarioId) {
        return repository.listarPorUsuario(usuarioId).stream().map(mapper::notificacion).toList();
    }

    public List<Vistas.Notificacion> listarPorPartido(UUID partidoId) {
        return repository.listarPorPartido(partidoId).stream().map(mapper::notificacion).toList();
    }

    public List<Vistas.Notificacion> ultimas(int limite) {
        return repository.listarUltimas(limite).stream().map(mapper::notificacion).toList();
    }

    public long enviadas() {
        return repository.contarPorEstado(EstadoEnvio.ENVIADO);
    }

    public long fallidas() {
        return repository.contarPorEstado(EstadoEnvio.FALLIDO);
    }
}
