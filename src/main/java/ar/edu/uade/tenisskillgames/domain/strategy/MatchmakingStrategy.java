package ar.edu.uade.tenisskillgames.domain.strategy;

import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;

import java.util.List;

/**
 * Interfaz del patron Strategy: criterio intercambiable de seleccion de
 * tenistas candidatos (RF5).
 */
public interface MatchmakingStrategy {

    List<Usuario> seleccionarCandidatos(List<Usuario> candidatos, Partido partido);

    /**
     * Codigo usado por el endpoint /emparejar?estrategia=...
     */
    default String getCodigo() {
        return getClass().getSimpleName();
    }
}
