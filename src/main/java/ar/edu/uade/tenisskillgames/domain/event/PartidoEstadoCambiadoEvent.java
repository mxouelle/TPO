package ar.edu.uade.tenisskillgames.domain.event;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.enums.TipoEvento;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento generico de transicion de estado. Es el que consume el
 * NotificationSubscriber para resolver canales y plantillas.
 */
public record PartidoEstadoCambiadoEvent(UUID partidoId,
                                        EstadoPartido estadoAnterior,
                                        EstadoPartido estadoNuevo,
                                        LocalDateTime ocurridoEn) implements DomainEvent {

    public PartidoEstadoCambiadoEvent(UUID partidoId, EstadoPartido estadoAnterior, EstadoPartido estadoNuevo) {
        this(partidoId, estadoAnterior, estadoNuevo, LocalDateTime.now());
    }

    @Override
    public UUID getPartidoId() {
        return partidoId;
    }

    @Override
    public TipoEvento getTipo() {
        return switch (estadoNuevo) {
            case CUPO_COMPLETO -> TipoEvento.CUPO_COMPLETO;
            case CONFIRMADO -> TipoEvento.PARTIDO_CONFIRMADO;
            case EN_JUEGO -> TipoEvento.PARTIDO_INICIADO;
            case FINALIZADO -> TipoEvento.PARTIDO_FINALIZADO;
            case CANCELADO -> TipoEvento.PARTIDO_CANCELADO;
            case BUSCANDO_JUGADORES -> TipoEvento.PARTIDO_CREADO;
        };
    }

    @Override
    public LocalDateTime getOcurridoEn() {
        return ocurridoEn;
    }
}
