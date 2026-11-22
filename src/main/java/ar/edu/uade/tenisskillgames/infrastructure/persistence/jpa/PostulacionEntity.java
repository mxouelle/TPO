package ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoPostulacion;
import ar.edu.uade.tenisskillgames.domain.enums.Lado;
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
 * Proyeccion JPA de POSTULACIONES. Incluye el flag de confirmacion
 * (confirmado / fecha_confirmacion) tal como figura en el DER.
 */
@Entity
@Table(name = "postulaciones", indexes = {
        @Index(name = "idx_postulaciones_partido", columnList = "partido_id"),
        @Index(name = "idx_postulaciones_usuario", columnList = "usuario_id")
})
public class PostulacionEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "usuario_id", nullable = false)
    public UUID usuarioId;

    @Column(name = "partido_id", nullable = false)
    public UUID partidoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "lado_asignado", length = 15)
    public Lado ladoAsignado;

    @Column(name = "confirmado")
    public Boolean confirmado;

    @Column(name = "fecha_confirmacion")
    public LocalDateTime fechaConfirmacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    public EstadoPostulacion estado;

    @Column(name = "fecha_postulacion")
    public LocalDateTime fechaPostulacion;

    @Column(name = "motivo_rechazo", length = 200)
    public String motivoRechazo;
}
