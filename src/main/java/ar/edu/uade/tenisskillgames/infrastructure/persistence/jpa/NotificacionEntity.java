package ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa;

import ar.edu.uade.tenisskillgames.domain.enums.CanalNotificacion;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoEnvio;
import ar.edu.uade.tenisskillgames.domain.enums.TipoEvento;
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
 * Proyeccion JPA de NOTIFICACIONES (auditoria de envios, RF7).
 */
@Entity
@Table(name = "notificaciones", indexes = {
        @Index(name = "idx_notificaciones_usuario", columnList = "usuario_id"),
        @Index(name = "idx_notificaciones_partido", columnList = "partido_id")
})
public class NotificacionEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "usuario_id")
    public UUID usuarioId;

    @Column(name = "partido_id")
    public UUID partidoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", length = 30)
    public TipoEvento tipoEvento;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", length = 20)
    public CanalNotificacion canal;

    @Column(name = "asunto", length = 160)
    public String asunto;

    @Column(name = "contenido", length = 1000)
    public String contenido;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_envio", length = 15)
    public EstadoEnvio estadoEnvio;

    @Column(name = "intentos")
    public int intentos;

    @Column(name = "ultimo_error", length = 300)
    public String ultimoError;

    @Column(name = "fecha_creacion")
    public LocalDateTime fechaCreacion;

    @Column(name = "fecha_envio")
    public LocalDateTime fechaEnvio;
}
