package ar.edu.uade.tenisskillgames.domain.enums;

/**
 * Estados del ciclo de vida del Partido (patron State, RF4).
 */
public enum EstadoPartido {

    BUSCANDO_JUGADORES,
    CUPO_COMPLETO,
    CONFIRMADO,
    EN_JUEGO,
    FINALIZADO,
    CANCELADO;

    public boolean permiteCancelacion() {
        return this == BUSCANDO_JUGADORES || this == CUPO_COMPLETO || this == CONFIRMADO;
    }

    public boolean esEstadoFinal() {
        return this == FINALIZADO || this == CANCELADO;
    }

    public boolean admiteNuevosJugadores() {
        return this == BUSCANDO_JUGADORES || this == CUPO_COMPLETO;
    }
}
