package ar.edu.uade.tenisskillgames.domain.model;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoPostulacion;
import ar.edu.uade.tenisskillgames.domain.enums.Lado;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Postulacion de un jugador a un partido (CU4).
 */
public class Postulacion {

    private UUID id;
    private Usuario usuario;
    private Partido partido;
    private Lado ladoDeseado;
    private LocalDateTime fechaPostulacion;
    private EstadoPostulacion estado;
    private String motivoRechazo;

    public Postulacion() {
        this.id = UUID.randomUUID();
        this.fechaPostulacion = LocalDateTime.now();
        this.estado = EstadoPostulacion.PENDIENTE;
        this.ladoDeseado = Lado.INDIFERENTE;
    }

    public Postulacion(Usuario usuario, Partido partido, Lado ladoDeseado, EstadoPostulacion estado) {
        this();
        this.usuario = usuario;
        this.partido = partido;
        this.ladoDeseado = ladoDeseado == null ? Lado.INDIFERENTE : ladoDeseado;
        this.estado = estado;
    }

    public void cambiarEstado(EstadoPostulacion nuevo) {
        this.estado = nuevo;
    }

    public void rechazar(String motivo) {
        this.estado = EstadoPostulacion.RECHAZADA;
        this.motivoRechazo = motivo;
    }

    public void aceptar() {
        this.estado = EstadoPostulacion.ACEPTADA;
        this.motivoRechazo = null;
    }

    public void pasarAEspera() {
        this.estado = EstadoPostulacion.LISTA_ESPERA;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Partido getPartido() {
        return partido;
    }

    public void setPartido(Partido partido) {
        this.partido = partido;
    }

    public Lado getLadoDeseado() {
        return ladoDeseado;
    }

    public void setLadoDeseado(Lado ladoDeseado) {
        this.ladoDeseado = ladoDeseado;
    }

    public LocalDateTime getFechaPostulacion() {
        return fechaPostulacion;
    }

    public void setFechaPostulacion(LocalDateTime fechaPostulacion) {
        this.fechaPostulacion = fechaPostulacion;
    }

    public EstadoPostulacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoPostulacion estado) {
        this.estado = estado;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Postulacion that)) {
            return false;
        }
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
