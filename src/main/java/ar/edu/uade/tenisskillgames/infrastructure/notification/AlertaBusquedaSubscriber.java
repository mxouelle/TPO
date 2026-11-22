package ar.edu.uade.tenisskillgames.infrastructure.notification;

import ar.edu.uade.tenisskillgames.service.NotificacionService;
import ar.edu.uade.tenisskillgames.domain.enums.TipoEvento;
import ar.edu.uade.tenisskillgames.domain.event.DomainEvent;
import ar.edu.uade.tenisskillgames.domain.event.DomainEventSubscriber;
import ar.edu.uade.tenisskillgames.domain.model.BusquedaFavorita;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.port.BusquedaFavoritaRepository;
import ar.edu.uade.tenisskillgames.domain.port.PartidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Observador de alertas de busqueda: cuando se publica un partido, avisa a
 * los usuarios cuyas busquedas favoritas son compatibles (RF2).
 */
@Component
public class AlertaBusquedaSubscriber implements DomainEventSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(AlertaBusquedaSubscriber.class);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PartidoRepository partidos;
    private final BusquedaFavoritaRepository busquedas;
    private final NotificacionService notificaciones;

    public AlertaBusquedaSubscriber(PartidoRepository partidos,
                                    BusquedaFavoritaRepository busquedas,
                                    NotificacionService notificaciones) {
        this.partidos = partidos;
        this.busquedas = busquedas;
        this.notificaciones = notificaciones;
    }

    @Override
    public void onEvent(DomainEvent evento) {
        if (evento.getTipo() != TipoEvento.PARTIDO_CREADO || evento.getPartidoId() == null) {
            return;
        }
        partidos.buscarPorId(evento.getPartidoId()).ifPresent(this::alertar);
    }

    private void alertar(Partido partido) {
        try {
            List<BusquedaFavorita> compatibles = busquedas.listarActivas().stream()
                    .filter(b -> b.getUsuario() != null)
                    .filter(b -> partido.getOrganizador() == null
                            || !b.getUsuario().getId().equals(partido.getOrganizador().getId()))
                    .filter(b -> b.esCompatible(partido))
                    .toList();

            for (BusquedaFavorita busqueda : compatibles) {
                notificaciones.enviar(busqueda.getUsuario(), partido.getId(), TipoEvento.PARTIDO_CREADO,
                        "Nuevo partido compatible con tu búsqueda",
                        "Hola " + busqueda.getUsuario().getNombre() + ": se publicó un partido "
                                + partido.getFormato() + " (" + partido.getSuperficie() + ") el "
                                + partido.getFechaHora().format(FECHA) + " en " + partido.getClubSede()
                                + ". Nivel NTRP " + partido.getNtrpMin() + "-" + partido.getNtrpMax() + ".");
            }
        } catch (RuntimeException ex) {
            LOG.warn("No se pudieron procesar las alertas de búsqueda para el partido {}", partido.getId(), ex);
        }
    }
}
