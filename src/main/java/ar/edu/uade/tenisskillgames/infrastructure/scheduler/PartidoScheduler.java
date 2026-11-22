package ar.edu.uade.tenisskillgames.infrastructure.scheduler;

import ar.edu.uade.tenisskillgames.service.NotificacionService;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.enums.TipoEvento;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.port.PartidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Planificador interno (CU7/CU8/RF10):
 *  - inicia los partidos confirmados cuya hora llego,
 *  - finaliza los que excedieron su duracion estimada,
 *  - cancela por falta de quorum,
 *  - envia recordatorios 24 h y 2 h antes.
 */
@Component
@ConditionalOnProperty(name = "tenis.scheduler.habilitado", havingValue = "true", matchIfMissing = true)
public class PartidoScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(PartidoScheduler.class);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PartidoRepository partidos;
    private final NotificacionService notificaciones;

    public PartidoScheduler(PartidoRepository partidos, NotificacionService notificaciones) {
        this.partidos = partidos;
        this.notificaciones = notificaciones;
    }

    @Scheduled(fixedDelayString = "${tenis.scheduler.fixed-delay-ms:60000}",
            initialDelayString = "${tenis.scheduler.initial-delay-ms:15000}")
    @Transactional
    public void procesarCicloDeVida() {
        LocalDateTime ahora = LocalDateTime.now();
        int iniciados = iniciarPartidos(ahora);
        int finalizados = finalizarPartidos(ahora);
        int cancelados = cancelarSinQuorum(ahora);
        int recordatorios = enviarRecordatorios(ahora);

        if (iniciados + finalizados + cancelados + recordatorios > 0) {
            LOG.info("[Scheduler] iniciados={} finalizados={} cancelados={} recordatorios={}",
                    iniciados, finalizados, cancelados, recordatorios);
        }
    }

    public int iniciarPartidos(LocalDateTime ahora) {
        List<Partido> pendientes = partidos.buscarConfirmadosParaIniciar(ahora);
        for (Partido partido : pendientes) {
            partido.iniciar();
            partidos.guardar(partido);
            LOG.info("[Scheduler] Partido {} iniciado", partido.getId());
        }
        return pendientes.size();
    }

    public int finalizarPartidos(LocalDateTime ahora) {
        List<Partido> expirados = partidos.buscarEnJuegoExpirados(ahora);
        for (Partido partido : expirados) {
            partido.finalizar();
            partidos.guardar(partido);
            LOG.info("[Scheduler] Partido {} finalizado por expiración de duración", partido.getId());
        }
        return expirados.size();
    }

    public int cancelarSinQuorum(LocalDateTime ahora) {
        List<Partido> vencidos = partidos.buscarSinQuorumVencidos(ahora);
        for (Partido partido : vencidos) {
            partido.cancelar("Cancelado automáticamente por falta de quórum");
            partidos.guardar(partido);
            LOG.info("[Scheduler] Partido {} cancelado por falta de quórum", partido.getId());
        }
        return vencidos.size();
    }

    public int enviarRecordatorios(LocalDateTime ahora) {
        int enviados = 0;
        for (Partido partido : partidos.buscarPorEstado(EstadoPartido.CONFIRMADO)) {
            long horas = Duration.between(ahora, partido.getFechaHora()).toHours();

            if (!partido.isRecordatorio24Enviado() && horas <= 24 && horas >= 23) {
                despacharRecordatorio(partido, 24);
                partido.setRecordatorio24Enviado(true);
                partidos.guardar(partido);
                enviados++;
            } else if (!partido.isRecordatorio2Enviado() && horas <= 2 && horas >= 0) {
                despacharRecordatorio(partido, 2);
                partido.setRecordatorio2Enviado(true);
                partidos.guardar(partido);
                enviados++;
            }
        }
        return enviados;
    }

    private void despacharRecordatorio(Partido partido, int horas) {
        for (Usuario participante : partido.getParticipantes()) {
            notificaciones.enviar(participante, partido.getId(), TipoEvento.RECORDATORIO,
                    "Recordatorio: tu partido es en " + horas + " horas",
                    "Hola " + participante.getNombre() + ": tu partido en " + partido.getClubSede()
                            + " comienza el " + partido.getFechaHora().format(FECHA)
                            + ". Superficie: " + partido.getSuperficie() + ".");
        }
    }
}
