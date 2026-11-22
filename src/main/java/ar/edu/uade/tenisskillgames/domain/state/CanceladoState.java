package ar.edu.uade.tenisskillgames.domain.state;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;

/**
 * Partido cancelado por el organizador antes de su inicio (CU9).
 * Estado terminal: no admite ninguna operacion.
 */
public class CanceladoState implements PartidoState {

    @Override
    public void postular(Partido ctx, Usuario jugador) {
        throw new IllegalStateException("El partido fue cancelado");
    }

    @Override
    public void confirmar(Partido ctx, Usuario jugador) {
        throw new IllegalStateException("El partido fue cancelado");
    }

    @Override
    public void iniciar(Partido ctx) {
        throw new IllegalStateException("El partido fue cancelado");
    }

    @Override
    public void finalizar(Partido ctx) {
        throw new IllegalStateException("El partido fue cancelado");
    }

    @Override
    public void cancelar(Partido ctx) {
        throw new IllegalStateException("El partido ya está cancelado");
    }

    @Override
    public EstadoPartido getEstado() {
        return EstadoPartido.CANCELADO;
    }
}
