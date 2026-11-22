package ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa;

import ar.edu.uade.tenisskillgames.domain.enums.FormatoTenis;
import ar.edu.uade.tenisskillgames.domain.enums.Superficie;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Proyeccion JPA de BUSQUEDAS_FAVORITAS (alertas de RF2).
 */
@Entity
@Table(name = "busquedas_favoritas", indexes = {
        @Index(name = "idx_busquedas_usuario", columnList = "usuario_id")
})
public class BusquedaFavoritaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "usuario_id", nullable = false)
    public UUID usuarioId;

    @Column(name = "nombre", length = 80)
    public String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "formato", length = 20)
    public FormatoTenis formato;

    @Enumerated(EnumType.STRING)
    @Column(name = "superficie", length = 25)
    public Superficie superficie;

    @Column(name = "ntrp_min")
    public double ntrpMin;

    @Column(name = "ntrp_max")
    public double ntrpMax;

    @Column(name = "radio_km")
    public int radioKm;

    @Column(name = "zona", length = 60)
    public String zona;

    @Column(name = "activa")
    public boolean activa = true;
}
