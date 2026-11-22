package ar.edu.uade.tenisskillgames.domain.state;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;

/**
 * Partido cerrado: habilita la carga del marcador por sets y las
 * evaluaciones de fair play (RF8). No admite mas transiciones.
 */
public class FinalizadoState implements PartidoState {

    @Override
    public void postular(Partido ctx, Usuario jugador) {
        throw new IllegalStateException("El partido ya finalizó");
    }

    @Override
    public void confirmar(Partido ctx, Usuario jugador) {
        throw new IllegalStateException("El partido ya finalizó");
    }

    @Override
    public void iniciar(Partido ctx) {
        throw new IllegalStateException("El partido ya finalizó");
    }

    @Override
    public void finalizar(Partido ctx) {
        throw new IllegalStateException("El partido ya está finalizado");
    }

    @Override
    public void cancelar(Partido ctx) {
        throw new IllegalStateException("No se puede cancelar un partido finalizado");
    }

    @Override
    public EstadoPartido getEstado() {
        return EstadoPartido.FINALIZADO;
    }
}
