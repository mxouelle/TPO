package ar.edu.uade.tenisskillgames.domain.state;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.event.PartidoCupoCompletoEvent;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;

/**
 * Estado inicial: existen cupos vacantes (RF4 / CU4).
 * Al completarse el cupo maximo transiciona automaticamente a CUPO_COMPLETO.
 */
public class BuscandoJugadoresState implements PartidoState {

    @Override
    public void postular(Partido ctx, Usuario jugador) {
        ctx.agregarParticipante(jugador);
        if (ctx.estaCupoCompleto()) {
            ctx.cambiarEstado(new CupoCompletoState());
            ctx.publicarEvento(new PartidoCupoCompletoEvent(ctx.getId()));
        }
    }

    @Override
    public void confirmar(Partido ctx, Usuario jugador) {
        throw new IllegalStateException("Cupo aún incompleto");
    }

    @Override
    public void iniciar(Partido ctx) {
        throw new IllegalStateException("El partido no está confirmado");
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
        return EstadoPartido.BUSCANDO_JUGADORES;
    }
}
