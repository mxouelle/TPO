package ar.edu.uade.tenisskillgames.domain.state;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;

/**
 * Todos los lugares estan asignados: se aguardan las confirmaciones (CU6).
 */
public class CupoCompletoState implements PartidoState {

    @Override
    public void postular(Partido ctx, Usuario jugador) {
        // El partido ya no tiene plazas: el jugador pasa a lista de espera (RF6).
        ctx.agregarAEspera(jugador);
    }

    @Override
    public void confirmar(Partido ctx, Usuario jugador) {
        ctx.registrarConfirmacion(jugador, true);
        if (ctx.todosConfirmaron()) {
            ctx.cambiarEstado(new ConfirmadoState());
        }
    }

    @Override
    public void iniciar(Partido ctx) {
        throw new IllegalStateException("El partido aún no está confirmado por todos los jugadores");
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
        return EstadoPartido.CUPO_COMPLETO;
    }
}
