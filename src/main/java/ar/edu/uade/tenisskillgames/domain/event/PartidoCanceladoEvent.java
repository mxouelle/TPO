package ar.edu.uade.tenisskillgames.domain.event;

import ar.edu.uade.tenisskillgames.domain.enums.TipoEvento;

import java.time.LocalDateTime;
import java.util.UUID;

public record PartidoCanceladoEvent(UUID partidoId, String motivo, LocalDateTime ocurridoEn) implements DomainEvent {

    public PartidoCanceladoEvent(UUID partidoId, String motivo) {
        this(partidoId, motivo, LocalDateTime.now());
    }

    @Override
    public UUID getPartidoId() {
        return partidoId;
    }

    @Override
    public TipoEvento getTipo() {
        return TipoEvento.PARTIDO_CANCELADO;
    }

    @Override
    public LocalDateTime getOcurridoEn() {
        return ocurridoEn;
    }
}
