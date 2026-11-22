package ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa;

import ar.edu.uade.tenisskillgames.domain.enums.CanalNotificacion;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoVerificacion;
import ar.edu.uade.tenisskillgames.domain.enums.GolpePreferido;
import ar.edu.uade.tenisskillgames.domain.enums.Lado;
import ar.edu.uade.tenisskillgames.domain.enums.ManoHabil;
import ar.edu.uade.tenisskillgames.domain.enums.Rol;
import ar.edu.uade.tenisskillgames.domain.enums.Superficie;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Proyeccion JPA de la tabla USUARIOS (ver DER, figura 3).
 * Es solo un registro de persistencia: la logica vive en el dominio.
 */
@Entity
@Table(name = "usuarios", indexes = {
        @Index(name = "idx_usuarios_zona", columnList = "zona"),
        @Index(name = "idx_usuarios_ntrp", columnList = "nivel_ntrp")
})
public class UsuarioEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "nombre", nullable = false, length = 80)
    public String nombre;

    @Column(name = "email", nullable = false, unique = true, length = 120)
    public String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    public String passwordHash;

    @Column(name = "nivel_ntrp", nullable = false)
    public double nivelNtrp;

    @Enumerated(EnumType.STRING)
    @Column(name = "mano_habil", length = 15)
    public ManoHabil manoHabil;

    @Enumerated(EnumType.STRING)
    @Column(name = "golpe_preferido", length = 15)
    public GolpePreferido golpePreferido;

    @Enumerated(EnumType.STRING)
    @Column(name = "lado_preferido", length = 15)
    public Lado ladoPreferido;

    @Enumerated(EnumType.STRING)
    @Column(name = "superficie_favorita", length = 25)
    public Superficie superficieFavorita;

    @Column(name = "zona", length = 60)
    public String zona;

    @Column(name = "latitud")
    public double latitud;

    @Column(name = "longitud")
    public double longitud;

    @Column(name = "strikes", nullable = false)
    public int strikes;

    @Enumerated(EnumType.STRING)
    @Column(name = "verificado", length = 15)
    public EstadoVerificacion estadoVerificacion;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", length = 20)
    public Set<Rol> roles = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_canales", joinColumns = @JoinColumn(name = "usuario_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "canal", length = 15)
    public Set<CanalNotificacion> canales = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_franjas", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "franja", length = 40)
    public Set<String> franjasHorarias = new HashSet<>();

    @Column(name = "partidos_jugados")
    public int partidosJugados;

    @Column(name = "ausencias")
    public int ausencias;

    @Column(name = "fair_play_promedio")
    public double fairPlayPromedio;

    @Column(name = "puntualidad_promedio")
    public double puntualidadPromedio;

    @Column(name = "fecha_registro")
    public LocalDateTime fechaRegistro;

    @Column(name = "suspendido_hasta")
    public LocalDateTime suspendidoHasta;

    @Column(name = "activo", nullable = false)
    public boolean activo = true;

    @Column(name = "telefono", length = 30)
    public String telefono;

    @Column(name = "push_token", length = 200)
    public String pushToken;
}
