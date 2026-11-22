package ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoModeracion;
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
 * Proyeccion JPA de EVALUACIONES (feedback post-partido, RF8).
 */
@Entity
@Table(name = "evaluaciones", indexes = {
        @Index(name = "idx_evaluaciones_evaluado", columnList = "evaluado_id")
})
public class EvaluacionJugadorEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "partido_id", nullable = false)
    public UUID partidoId;

    @Column(name = "evaluador_id", nullable = false)
    public UUID evaluadorId;

    @Column(name = "evaluado_id", nullable = false)
    public UUID evaluadoId;

    @Column(name = "fair_play", nullable = false)
    public int fairPlay;

    @Column(name = "puntualidad", nullable = false)
    public int puntualidad;

    @Column(name = "comentario", length = 500)
    public String comentario;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderacion", length = 15)
    public EstadoModeracion moderacion;

    @Column(name = "fecha_evaluacion")
    public LocalDateTime fechaEvaluacion;
}
