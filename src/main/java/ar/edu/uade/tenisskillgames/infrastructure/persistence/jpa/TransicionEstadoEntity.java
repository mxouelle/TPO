package ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Auditoria de transiciones de estado del partido (trazabilidad RF no funcional).
 */
@Entity
@Table(name = "transiciones_estado", indexes = {
        @Index(name = "idx_transiciones_partido", columnList = "partido_id")
})
public class TransicionEstadoEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "partido_id", nullable = false)
    public UUID partidoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 25)
    public EstadoPartido estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", length = 25)
    public EstadoPartido estadoNuevo;

    @Column(name = "fecha")
    public LocalDateTime fecha;

    @Column(name = "motivo", length = 200)
    public String motivo;
}
