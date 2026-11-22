package ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.enums.FormatoTenis;
import ar.edu.uade.tenisskillgames.domain.enums.Modalidad;
import ar.edu.uade.tenisskillgames.domain.enums.Superficie;
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
 * Proyeccion JPA de la tabla PARTIDOS. Indice compuesto
 * (estado_actual, fecha_hora, superficie) segun consigna.
 */
@Entity
@Table(name = "partidos", indexes = {
        @Index(name = "idx_partidos_estado_fecha_superficie",
                columnList = "estado_actual, fecha_hora, superficie")
})
public class PartidoEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "organizador_id", nullable = false)
    public UUID organizadorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "formato", length = 20)
    public FormatoTenis formato;

    @Enumerated(EnumType.STRING)
    @Column(name = "superficie", length = 25)
    public Superficie superficie;

    @Column(name = "club_sede", length = 100)
    public String clubSede;

    @Column(name = "direccion", length = 160)
    public String direccion;

    @Column(name = "latitud")
    public double latitud;

    @Column(name = "longitud")
    public double longitud;

    @Column(name = "ntrp_min")
    public double ntrpMin;

    @Column(name = "ntrp_max")
    public double ntrpMax;

    @Column(name = "radio_max_km")
    public int radioMaxKm;

    @Column(name = "fecha_hora", nullable = false)
    public LocalDateTime fechaHora;

    @Column(name = "duracion_minutos")
    public int duracionMinutos;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad", length = 30)
    public Modalidad modalidad;

    @Column(name = "cupo_maximo")
    public int cupoMaximo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_actual", length = 25, nullable = false)
    public EstadoPartido estadoActual;

    @Column(name = "estrategia", length = 20)
    public String estrategia;

    @Column(name = "motivo_cancelacion", length = 200)
    public String motivoCancelacion;

    @Column(name = "fecha_creacion")
    public LocalDateTime fechaCreacion;

    @Column(name = "observaciones", length = 500)
    public String observaciones;

    @Column(name = "recordatorio_24_enviado")
    public boolean recordatorio24Enviado;

    @Column(name = "recordatorio_2_enviado")
    public boolean recordatorio2Enviado;
}
