package ar.edu.uade.tenisskillgames.domain.state;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;

/**
 * Interfaz del patron State para el ciclo de vida del Partido (RF4).
 * Cada estado concreto decide si la operacion es valida o lanza
 * {@link IllegalStateException} (transicion denegada).
 */
public interface PartidoState {

    void postular(Partido ctx, Usuario jugador);

    void confirmar(Partido ctx, Usuario jugador);

    void iniciar(Partido ctx);

    void finalizar(Partido ctx);

    void cancelar(Partido ctx);

    EstadoPartido getEstado();
}
