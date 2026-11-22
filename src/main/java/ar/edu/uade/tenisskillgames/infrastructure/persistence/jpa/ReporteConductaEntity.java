package ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoReporte;
import ar.edu.uade.tenisskillgames.domain.enums.MotivoReporte;
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
 * Proyeccion JPA de REPORTES_CONDUCTA (RF9 / CU11).
 */
@Entity
@Table(name = "reportes_conducta", indexes = {
        @Index(name = "idx_reportes_reportado", columnList = "reportado_id")
})
public class ReporteConductaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "partido_id")
    public UUID partidoId;

    @Column(name = "reportante_id", nullable = false)
    public UUID reportanteId;

    @Column(name = "reportado_id", nullable = false)
    public UUID reportadoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", length = 30)
    public MotivoReporte motivo;

    @Column(name = "descripcion", length = 500)
    public String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 15)
    public EstadoReporte estado;

    @Column(name = "resolucion", length = 300)
    public String resolucion;

    @Column(name = "fecha_reporte")
    public LocalDateTime fechaReporte;
}
