package ar.edu.uade.tenisskillgames.infrastructure.notification;

import ar.edu.uade.tenisskillgames.service.NotificacionService;
import ar.edu.uade.tenisskillgames.domain.enums.TipoEvento;
import ar.edu.uade.tenisskillgames.domain.event.DomainEvent;
import ar.edu.uade.tenisskillgames.domain.event.DomainEventSubscriber;
import ar.edu.uade.tenisskillgames.domain.event.PartidoCanceladoEvent;
import ar.edu.uade.tenisskillgames.domain.event.PartidoEstadoCambiadoEvent;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.port.CalendarioPort;
import ar.edu.uade.tenisskillgames.domain.port.PartidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Observador de eventos de dominio: resuelve destinatarios, arma el mensaje
 * de cada transicion y lo despacha por todos los canales del usuario (CU10).
 */
@Component
public class NotificacionSubscriber implements DomainEventSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacionSubscriber.class);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PartidoRepository partidos;
    private final NotificacionService notificaciones;
    private final CalendarioPort calendario;

    public NotificacionSubscriber(PartidoRepository partidos,
                                  NotificacionService notificaciones,
                                  CalendarioPort calendario) {
        this.partidos = partidos;
        this.notificaciones = notificaciones;
        this.calendario = calendario;
    }

    @Override
    public void onEvent(DomainEvent evento) {
        if (evento.getPartidoId() == null) {
            return;
        }
        // El evento generico de CUPO_COMPLETO y CANCELADO se ignora: esos flujos
        // publican eventos dedicados (PartidoCupoCompletoEvent / PartidoCanceladoEvent)
        // para no notificar dos veces al mismo jugador.
        if (evento instanceof PartidoEstadoCambiadoEvent generico
                && (generico.estadoNuevo() == ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido.CUPO_COMPLETO
                || generico.estadoNuevo() == ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido.CANCELADO)) {
            return;
        }
        if (evento.getTipo() == TipoEvento.PARTIDO_CREADO || evento.getTipo() == TipoEvento.RESULTADO_CARGADO) {
            return;
        }

        partidos.buscarPorId(evento.getPartidoId()).ifPresent(partido -> {
            try {
                procesar(evento, partido);
            } catch (RuntimeException ex) {
                LOG.warn("No se pudo notificar el evento {} del partido {}", evento.getTipo(),
                        evento.getPartidoId(), ex);
            }
        });
    }

    private void procesar(DomainEvent evento, Partido partido) {
        String asunto = asunto(evento, partido);
        String base = "partido " + partido.getFormato() + " del " + partido.getFechaHora().format(FECHA)
                + " en " + partido.getClubSede();

        Map<UUID, Usuario> destinatarios = new LinkedHashMap<>();
        partido.getParticipantes().forEach(u -> destinatarios.put(u.getId(), u));
        if (evento.getTipo() == TipoEvento.PARTIDO_CANCELADO) {
            partido.getListaEspera().forEach(u -> destinatarios.put(u.getId(), u));
        }

        String adjunto = null;
        if (evento.getTipo() == TipoEvento.PARTIDO_CONFIRMADO) {
            adjunto = new String(calendario.generar(partido), java.nio.charset.StandardCharsets.UTF_8);
        }

        for (Usuario destinatario : destinatarios.values()) {
            notificaciones.enviar(destinatario, partido.getId(), evento.getTipo(), asunto,
                    cuerpo(evento, partido, base, destinatario),
                    evento.getTipo() == TipoEvento.PARTIDO_CONFIRMADO
                            ? calendario.nombreArchivo(partido) : null,
                    adjunto);
        }
    }

    private String asunto(DomainEvent evento, Partido partido) {
        return switch (evento.getTipo()) {
            case CUPO_COMPLETO -> "Cupo completo: confirmá tu asistencia";
            case PARTIDO_CONFIRMADO -> "Partido confirmado - invitación de calendario adjunta";
            case PARTIDO_INICIADO -> "Tu partido comenzó";
            case PARTIDO_FINALIZADO -> "Partido finalizado: cargá el resultado";
            case PARTIDO_CANCELADO -> "Partido cancelado";
            case RECORDATORIO -> "Recordatorio de partido";
            case REPORTE_CONFIRMADO -> "Se registró un strike en tu cuenta";
            default -> "Actualización de " + partido.getClubSede();
        };
    }

    private String cuerpo(DomainEvent evento, Partido partido, String base, Usuario destinatario) {
        String saludo = "Hola " + destinatario.getNombre() + ": ";
        return switch (evento.getTipo()) {
            case CUPO_COMPLETO -> saludo + "el " + base + " alcanzó el cupo completo. "
                    + "Ingresá a la app para confirmar tu asistencia.";
            case PARTIDO_CONFIRMADO -> saludo + "el " + base + " quedó confirmado por todos los jugadores. "
                    + "Adjuntamos el evento iCalendar (.ics) para agendar la reserva de cancha.";
            case PARTIDO_INICIADO -> saludo + "el " + base + " está en juego.";
            case PARTIDO_FINALIZADO -> saludo + "el " + base + " finalizó. "
                    + "Ya podés cargar el marcador por sets y evaluar a tus rivales.";
            case PARTIDO_CANCELADO -> saludo + "el " + base + " fue cancelado. Motivo: "
                    + (evento instanceof PartidoCanceladoEvent cancelado ? cancelado.motivo() : "no informado");
            case RECORDATORIO -> saludo + "te recordamos que tenés el " + base + ".";
            case REPORTE_CONFIRMADO -> saludo + "se confirmó un reporte de conducta en tu contra y se "
                    + "registró un strike. Con 3 strikes la cuenta queda suspendida 7 días.";
            default -> saludo + "novedades del " + base + ".";
        };
    }
}
