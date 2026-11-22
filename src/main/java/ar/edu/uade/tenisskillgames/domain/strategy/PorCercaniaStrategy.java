package ar.edu.uade.tenisskillgames.domain.strategy;

import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Emparejamiento por cercania geografica (RF5): prioriza jugadores dentro del
 * radio maximo respecto del club sede del encuentro.
 */
@Component("CERCANIA")
public class PorCercaniaStrategy implements MatchmakingStrategy {

    @Override
    public List<Usuario> seleccionarCandidatos(List<Usuario> candidatos, Partido partido) {
        return candidatos.stream()
                .filter(u -> partido.distanciaAlClubKm(u) <= partido.getRadioMaxKm())
                .sorted(Comparator.comparingDouble(partido::distanciaAlClubKm))
                .limit(partido.getCupoMaximo())
                .toList();
    }

    @Override
    public String getCodigo() {
        return "CERCANIA";
    }
}
