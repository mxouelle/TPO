package ar.edu.uade.tenisskillgames.domain.event;

import ar.edu.uade.tenisskillgames.domain.enums.TipoEvento;

import java.time.LocalDateTime;
import java.util.UUID;

public record PartidoCupoCompletoEvent(UUID partidoId, LocalDateTime ocurridoEn) implements DomainEvent {

    public PartidoCupoCompletoEvent(UUID partidoId) {
        this(partidoId, LocalDateTime.now());
    }

    @Override
    public UUID getPartidoId() {
        return partidoId;
    }

    @Override
    public TipoEvento getTipo() {
        return TipoEvento.CUPO_COMPLETO;
    }

    @Override
    public LocalDateTime getOcurridoEn() {
        return ocurridoEn;
    }
}
