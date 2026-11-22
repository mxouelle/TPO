package ar.edu.uade.tenisskillgames.domain.event;

import ar.edu.uade.tenisskillgames.domain.enums.TipoEvento;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResultadoCargadoEvent(UUID partidoId, LocalDateTime ocurridoEn) implements DomainEvent {

    public ResultadoCargadoEvent(UUID partidoId) {
        this(partidoId, LocalDateTime.now());
    }

    @Override
    public UUID getPartidoId() {
        return partidoId;
    }

    @Override
    public TipoEvento getTipo() {
        return TipoEvento.RESULTADO_CARGADO;
    }

    @Override
    public LocalDateTime getOcurridoEn() {
        return ocurridoEn;
    }
}
