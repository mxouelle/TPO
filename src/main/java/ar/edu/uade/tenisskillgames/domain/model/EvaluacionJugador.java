package ar.edu.uade.tenisskillgames.domain.model;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoModeracion;
import ar.edu.uade.tenisskillgames.domain.exception.DomainValidationException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Evaluacion post-partido de un jugador a otro (RF8).
 */
public class EvaluacionJugador {

    private UUID id;
    private Partido partido;
    private Usuario evaluador;
    private Usuario evaluado;
    private int puntajeFairPlay;
    private int puntajePuntualidad;
    private String comentario;
    private EstadoModeracion estadoModeracion;
    private LocalDateTime fechaEvaluacion;

    public EvaluacionJugador() {
        this.id = UUID.randomUUID();
        this.estadoModeracion = EstadoModeracion.PENDIENTE;
        this.fechaEvaluacion = LocalDateTime.now();
    }

    public EvaluacionJugador(Partido partido, Usuario evaluador, Usuario evaluado,
                             int puntajeFairPlay, int puntajePuntualidad, String comentario) {
        this();
        if (evaluador.equals(evaluado)) {
            throw new DomainValidationException("Un jugador no puede evaluarse a si mismo");
        }
        this.partido = partido;
        this.evaluador = evaluador;
        this.evaluado = evaluado;
        setPuntajeFairPlay(puntajeFairPlay);
        setPuntajePuntualidad(puntajePuntualidad);
        this.comentario = comentario;
    }

    public void setPuntajeFairPlay(int puntaje) {
        if (puntaje < 1 || puntaje > 5) {
            throw new DomainValidationException("El puntaje de fair play debe estar entre 1 y 5");
        }
        this.puntajeFairPlay = puntaje;
    }

    public void setPuntajePuntualidad(int puntaje) {
        if (puntaje < 1 || puntaje > 5) {
            throw new DomainValidationException("El puntaje de puntualidad debe estar entre 1 y 5");
        }
        this.puntajePuntualidad = puntaje;
    }

    public void aprobar() {
        this.estadoModeracion = EstadoModeracion.APROBADO;
    }

    public void rechazar() {
        this.estadoModeracion = EstadoModeracion.RECHAZADO;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Partido getPartido() {
        return partido;
    }

    public void setPartido(Partido partido) {
        this.partido = partido;
    }

    public Usuario getEvaluador() {
        return evaluador;
    }

    public void setEvaluador(Usuario evaluador) {
        this.evaluador = evaluador;
    }

    public Usuario getEvaluado() {
        return evaluado;
    }

    public void setEvaluado(Usuario evaluado) {
        this.evaluado = evaluado;
    }

    public int getPuntajeFairPlay() {
        return puntajeFairPlay;
    }

    public int getPuntajePuntualidad() {
        return puntajePuntualidad;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public EstadoModeracion getEstadoModeracion() {
        return estadoModeracion;
    }

    public void setEstadoModeracion(EstadoModeracion estadoModeracion) {
        this.estadoModeracion = estadoModeracion;
    }

    public LocalDateTime getFechaEvaluacion() {
        return fechaEvaluacion;
    }

    public void setFechaEvaluacion(LocalDateTime fechaEvaluacion) {
        this.fechaEvaluacion = fechaEvaluacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EvaluacionJugador that)) {
            return false;
        }
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
