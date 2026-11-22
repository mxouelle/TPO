package ar.edu.uade.tenisskillgames.domain.event;

import ar.edu.uade.tenisskillgames.domain.enums.TipoEvento;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento de dominio publicado por el agregado Partido (patron Observer, CU10).
 */
public interface DomainEvent {

    UUID getPartidoId();

    TipoEvento getTipo();

    LocalDateTime getOcurridoEn();
}
