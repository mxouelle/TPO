package ar.edu.uade.tenisskillgames.domain.state;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;

/**
 * Todos los participantes confirmaron. El scheduler lo lleva a EN_JUEGO
 * cuando se alcanza la fecha y hora pactada (CU7).
 */
public class ConfirmadoState implements PartidoState {

    @Override
    public void postular(Partido ctx, Usuario jugador) {
        throw new IllegalStateException("El partido ya está confirmado y no admite nuevas postulaciones");
    }

    @Override
    public void confirmar(Partido ctx, Usuario jugador) {
        if (ctx.estaConfirmado(jugador)) {
            return;
        }
        throw new IllegalStateException("El jugador no participa de este partido");
    }

    @Override
    public void iniciar(Partido ctx) {
        ctx.cambiarEstado(new EnJuegoState());
    }

    @Override
    public void finalizar(Partido ctx) {
        throw new IllegalStateException("El partido no ha iniciado");
    }

    @Override
    public void cancelar(Partido ctx) {
        ctx.cambiarEstado(new CanceladoState());
    }

    @Override
    public EstadoPartido getEstado() {
        return EstadoPartido.CONFIRMADO;
    }
}
