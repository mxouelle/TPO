package ar.edu.uade.tenisskillgames.domain.state;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;

/**
 * Partido en curso. Solo admite el cierre (manual del organizador o por
 * expiracion de la duracion estimada) y la carga del resultado (CU8).
 */
public class EnJuegoState implements PartidoState {

    @Override
    public void postular(Partido ctx, Usuario jugador) {
        throw new IllegalStateException("El partido está en juego: no admite nuevas postulaciones");
    }

    @Override
    public void confirmar(Partido ctx, Usuario jugador) {
        throw new IllegalStateException("El partido ya está en juego");
    }

    @Override
    public void iniciar(Partido ctx) {
        throw new IllegalStateException("El partido ya está en juego");
    }

    @Override
    public void finalizar(Partido ctx) {
        ctx.cambiarEstado(new FinalizadoState());
    }

    @Override
    public void cancelar(Partido ctx) {
        throw new IllegalStateException("No se puede cancelar un partido que ya está en juego");
    }

    @Override
    public EstadoPartido getEstado() {
        return EstadoPartido.EN_JUEGO;
    }
}
