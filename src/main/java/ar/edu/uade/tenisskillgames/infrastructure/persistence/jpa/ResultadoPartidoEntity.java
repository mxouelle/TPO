package ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa;

import ar.edu.uade.tenisskillgames.domain.enums.Lado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Proyeccion JPA de RESULTADOS_PARTIDO (relacion 1 a 0..1 con PARTIDOS).
 */
@Entity
@Table(name = "resultados_partido")
public class ResultadoPartidoEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "partido_id", nullable = false, unique = true)
    public UUID partidoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ganador_lado", length = 5)
    public Lado ganadorLado;

    @Column(name = "marcador_sets", length = 60)
    public String marcadorSets;

    @Column(name = "duracion_min")
    public int duracionMin;

    @Column(name = "observaciones", length = 500)
    public String observaciones;

    @Column(name = "validado")
    public boolean validado;

    @Column(name = "fecha_carga")
    public LocalDateTime fechaCarga;
}
