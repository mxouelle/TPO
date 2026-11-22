package ar.edu.uade.tenisskillgames.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Confirmacion de asistencia de un participante (CU6).
 */
public class Confirmacion {

    private UUID id;
    private Usuario usuario;
    private Partido partido;
    private boolean confirmado;
    private LocalDateTime fechaConfirmacion;

    public Confirmacion() {
        this.id = UUID.randomUUID();
    }

    public Confirmacion(Usuario usuario, Partido partido, boolean confirmado) {
        this();
        this.usuario = usuario;
        this.partido = partido;
        this.confirmado = confirmado;
        this.fechaConfirmacion = LocalDateTime.now();
    }

    public void actualizar(boolean confirmado) {
        this.confirmado = confirmado;
        this.fechaConfirmacion = LocalDateTime.now();
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

    public boolean isConfirmado() {
        return confirmado;
    }

    public void setConfirmado(boolean confirmado) {
        this.confirmado = confirmado;
    }

    public LocalDateTime getFechaConfirmacion() {
        return fechaConfirmacion;
    }

    public void setFechaConfirmacion(LocalDateTime fechaConfirmacion) {
        this.fechaConfirmacion = fechaConfirmacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Confirmacion that)) {
            return false;
        }
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
