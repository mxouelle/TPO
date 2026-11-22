package ar.edu.uade.tenisskillgames.domain.event;

import ar.edu.uade.tenisskillgames.domain.enums.TipoEvento;

import java.time.LocalDateTime;
import java.util.UUID;

public record PartidoCreadoEvent(UUID partidoId,
                                 UUID organizadorId,
                                 String clubSede,
                                 String superficie,
                                 LocalDateTime fechaHora,
                                 LocalDateTime ocurridoEn) implements DomainEvent {

    public PartidoCreadoEvent(UUID partidoId, UUID organizadorId, String clubSede,
                              String superficie, LocalDateTime fechaHora) {
        this(partidoId, organizadorId, clubSede, superficie, fechaHora, LocalDateTime.now());
    }

    @Override
    public UUID getPartidoId() {
        return partidoId;
    }

    @Override
    public TipoEvento getTipo() {
        return TipoEvento.PARTIDO_CREADO;
    }

    @Override
    public LocalDateTime getOcurridoEn() {
        return ocurridoEn;
    }
}
