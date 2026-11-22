package ar.edu.uade.tenisskillgames.domain.state;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;

/**
 * Rehidrata el estado concreto a partir del valor persistido.
 */
public final class PartidoStateFactory {

    private PartidoStateFactory() {
    }

    public static PartidoState para(EstadoPartido estado) {
        if (estado == null) {
            return new BuscandoJugadoresState();
        }
        return switch (estado) {
            case BUSCANDO_JUGADORES -> new BuscandoJugadoresState();
            case CUPO_COMPLETO -> new CupoCompletoState();
            case CONFIRMADO -> new ConfirmadoState();
            case EN_JUEGO -> new EnJuegoState();
            case FINALIZADO -> new FinalizadoState();
            case CANCELADO -> new CanceladoState();
        };
    }
}
