package ar.edu.uade.tenisskillgames.domain.model;

import ar.edu.uade.tenisskillgames.domain.enums.CanalNotificacion;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoVerificacion;
import ar.edu.uade.tenisskillgames.domain.enums.GolpePreferido;
import ar.edu.uade.tenisskillgames.domain.enums.Lado;
import ar.edu.uade.tenisskillgames.domain.enums.ManoHabil;
import ar.edu.uade.tenisskillgames.domain.enums.Rol;
import ar.edu.uade.tenisskillgames.domain.enums.Superficie;
import ar.edu.uade.tenisskillgames.domain.exception.DomainValidationException;
import ar.edu.uade.tenisskillgames.domain.exception.UsuarioSuspendidoException;
import ar.edu.uade.tenisskillgames.domain.vo.HistorialDeportivo;
import ar.edu.uade.tenisskillgames.domain.vo.UbicacionGeo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Agregado Usuario: perfil tenistico, reputacion y sanciones (RF1 / RF9).
 */
public class Usuario {

    public static final double NTRP_MINIMO = 1.5;
    public static final double NTRP_MAXIMO = 7.0;
    public static final int STRIKES_PARA_SUSPENSION = 3;
    public static final int DIAS_SUSPENSION = 7;

    private UUID id;
    private String nombre;
    private String email;
    private String passwordHash;
    private double nivelNTRP;
    private ManoHabil manoHabil;
    private GolpePreferido golpePreferido;
    private Lado ladoPreferido;
    private Superficie superficieFavorita;
    private String zona;
    private UbicacionGeo ubicacion;
    private int strikes;
    private EstadoVerificacion estadoVerificacion;
    private Set<Rol> roles;
    private Set<CanalNotificacion> canalesHabilitados;
    private List<String> franjasHorarias;
    private HistorialDeportivo historial;
    private LocalDateTime fechaRegistro;
    private LocalDateTime suspendidoHasta;
    private boolean activo;
    private String telefono;
    private String pushToken;

    public Usuario() {
        this.id = UUID.randomUUID();
        this.roles = EnumSet.of(Rol.JUGADOR);
        this.canalesHabilitados = EnumSet.allOf(CanalNotificacion.class);
        this.franjasHorarias = new ArrayList<>();
        this.estadoVerificacion = EstadoVerificacion.PENDIENTE;
        this.historial = HistorialDeportivo.nuevo();
        this.fechaRegistro = LocalDateTime.now();
        this.manoHabil = ManoHabil.DIESTRO;
        this.golpePreferido = GolpePreferido.AMBOS;
        this.ladoPreferido = Lado.INDIFERENTE;
        this.activo = true;
        this.ubicacion = UbicacionGeo.de(0, 0);
        this.zona = "Sin definir";
        this.superficieFavorita = Superficie.POLVO_DE_LADRILLO;
    }

    public Usuario(String nombre, String email, String passwordHash, double nivelNTRP, String zona,
                   Superficie superficieFavorita, ManoHabil manoHabil, GolpePreferido golpePreferido,
                   UbicacionGeo ubicacion) {
        this();
        setNombre(nombre);
        setEmail(email);
        setPasswordHash(passwordHash);
        setNivelNTRP(nivelNTRP);
        setZona(zona);
        setSuperficieFavorita(superficieFavorita);
        setManoHabil(manoHabil);
        setGolpePreferido(golpePreferido);
        setUbicacion(ubicacion);
    }

    /**
     * Regla de negocio CU1: NTRP entre 1.5 y 7.0 en incrementos de 0.5.
     */
    public static void validarNivel(double nivel) {
        if (nivel < NTRP_MINIMO || nivel > NTRP_MAXIMO) {
            throw new DomainValidationException(
                    "El nivel NTRP debe estar entre " + NTRP_MINIMO + " y " + NTRP_MAXIMO);
        }
        double escalado = nivel * 2;
        if (Math.abs(escalado - Math.rint(escalado)) > 0.0001) {
            throw new DomainValidationException("El nivel NTRP debe expresarse en incrementos de 0.5");
        }
    }

    public void verificarCuenta() {
        this.estadoVerificacion = EstadoVerificacion.VERIFICADO;
    }

    public boolean estaVerificado() {
        return estadoVerificacion == EstadoVerificacion.VERIFICADO;
    }

    public boolean estaSuspendido(LocalDateTime referencia) {
        return suspendidoHasta != null && suspendidoHasta.isAfter(referencia);
    }

    public boolean estaSuspendido() {
        return estaSuspendido(LocalDateTime.now());
    }

    public LocalDate getRehabilitacion() {
        return suspendidoHasta == null ? null : suspendidoHasta.toLocalDate();
    }

    /**
     * RF9: al alcanzar 3 strikes se aplica una suspension automatica de 7 dias.
     */
    public void aplicarStrike() {
        this.strikes++;
        if (this.strikes >= STRIKES_PARA_SUSPENSION) {
            this.suspendidoHasta = LocalDateTime.now().plusDays(DIAS_SUSPENSION);
        }
    }

    public void reiniciarStrikes() {
        this.strikes = 0;
        this.suspendidoHasta = null;
    }

    public void validarPuedePostular() {
        if (!activo) {
            throw new UsuarioSuspendidoException("La cuenta del jugador esta deshabilitada", null);
        }
        if (estadoVerificacion != EstadoVerificacion.VERIFICADO) {
            throw new UsuarioSuspendidoException("La cuenta debe estar verificada para postularse", null);
        }
        if (estaSuspendido()) {
            throw new UsuarioSuspendidoException(
                    "Cuenta suspendida por acumulacion de strikes hasta " + getRehabilitacion(), getRehabilitacion());
        }
    }

    public boolean puedePostular() {
        return activo
                && estadoVerificacion == EstadoVerificacion.VERIFICADO
                && !estaSuspendido();
    }

    public boolean tieneRol(Rol rol) {
        return roles.contains(rol);
    }

    public void agregarRol(Rol rol) {
        this.roles.add(rol);
    }

    public void habilitarCanal(CanalNotificacion canal) {
        this.canalesHabilitados.add(canal);
    }

    public void deshabilitarCanal(CanalNotificacion canal) {
        this.canalesHabilitados.remove(canal);
    }

    public void registrarPartidoJugado() {
        this.historial = new HistorialDeportivo(
                historial.getPartidosJugados() + 1,
                historial.getAusencias(),
                historial.getFairPlayPromedio(),
                historial.getPuntualidadPromedio());
    }

    public void registrarAusencia() {
        this.historial = new HistorialDeportivo(
                historial.getPartidosJugados(),
                historial.getAusencias() + 1,
                historial.getFairPlayPromedio(),
                historial.getPuntualidadPromedio());
    }

    public void actualizarReputacion(int evaluaciones, double fairPlay, double puntualidad) {
        if (evaluaciones <= 0) {
            return;
        }
        this.historial = new HistorialDeportivo(
                historial.getPartidosJugados(),
                historial.getAusencias(),
                fairPlay,
                puntualidad);
    }

    // ------------------------------------------------------------------ getters / setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new DomainValidationException("El nombre es obligatorio");
        }
        this.nombre = nombre.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$")) {
            throw new DomainValidationException("Email invalido: " + email);
        }
        this.email = email.trim().toLowerCase();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new DomainValidationException("El hash de la contrasena es obligatorio");
        }
        this.passwordHash = passwordHash;
    }

    public double getNivelNTRP() {
        return nivelNTRP;
    }

    public void setNivelNTRP(double nivelNTRP) {
        validarNivel(nivelNTRP);
        this.nivelNTRP = nivelNTRP;
    }

    public ManoHabil getManoHabil() {
        return manoHabil;
    }

    public void setManoHabil(ManoHabil manoHabil) {
        this.manoHabil = manoHabil == null ? ManoHabil.DIESTRO : manoHabil;
    }

    public GolpePreferido getGolpePreferido() {
        return golpePreferido;
    }

    public void setGolpePreferido(GolpePreferido golpePreferido) {
        this.golpePreferido = golpePreferido == null ? GolpePreferido.AMBOS : golpePreferido;
    }

    public Lado getLadoPreferido() {
        return ladoPreferido;
    }

    public void setLadoPreferido(Lado ladoPreferido) {
        this.ladoPreferido = ladoPreferido == null ? Lado.INDIFERENTE : ladoPreferido;
    }

    public Superficie getSuperficieFavorita() {
        return superficieFavorita;
    }

    public void setSuperficieFavorita(Superficie superficieFavorita) {
        this.superficieFavorita = superficieFavorita == null ? Superficie.POLVO_DE_LADRILLO : superficieFavorita;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = (zona == null || zona.isBlank()) ? "Sin definir" : zona.trim();
    }

    public UbicacionGeo getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(UbicacionGeo ubicacion) {
        this.ubicacion = Objects.requireNonNullElse(ubicacion, UbicacionGeo.de(0, 0));
    }

    public int getStrikes() {
        return strikes;
    }

    public void setStrikes(int strikes) {
        this.strikes = Math.max(strikes, 0);
    }

    public EstadoVerificacion getEstadoVerificacion() {
        return estadoVerificacion;
    }

    public void setEstadoVerificacion(EstadoVerificacion estadoVerificacion) {
        this.estadoVerificacion = estadoVerificacion;
    }

    public Set<Rol> getRoles() {
        return roles;
    }

    public void setRoles(Set<Rol> roles) {
        this.roles = roles == null || roles.isEmpty() ? EnumSet.of(Rol.JUGADOR) : EnumSet.copyOf(roles);
    }

    public Set<CanalNotificacion> getCanalesHabilitados() {
        return canalesHabilitados;
    }

    public void setCanalesHabilitados(Set<CanalNotificacion> canales) {
        this.canalesHabilitados = canales == null || canales.isEmpty()
                ? EnumSet.noneOf(CanalNotificacion.class)
                : EnumSet.copyOf(canales);
    }

    public List<String> getFranjasHorarias() {
        return franjasHorarias;
    }

    public void setFranjasHorarias(List<String> franjasHorarias) {
        this.franjasHorarias = franjasHorarias == null ? new ArrayList<>() : new ArrayList<>(franjasHorarias);
    }

    public HistorialDeportivo getHistorial() {
        return historial;
    }

    public void setHistorial(HistorialDeportivo historial) {
        this.historial = Objects.requireNonNullElseGet(historial, HistorialDeportivo::nuevo);
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getSuspendidoHasta() {
        return suspendidoHasta;
    }

    public void setSuspendidoHasta(LocalDateTime suspendidoHasta) {
        this.suspendidoHasta = suspendidoHasta;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Usuario usuario)) {
            return false;
        }
        return id != null && id.equals(usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return nombre + " (" + email + ", NTRP " + nivelNTRP + ")";
    }
}
