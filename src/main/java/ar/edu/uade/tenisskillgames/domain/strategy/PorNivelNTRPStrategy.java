package ar.edu.uade.tenisskillgames.domain.strategy;

import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Emparejamiento por nivel tecnico (RF5): conserva los candidatos dentro de la
 * banda NTRP del partido y los ordena por cercania al promedio del encuentro.
 */
@Component("NTRP")
public class PorNivelNTRPStrategy implements MatchmakingStrategy {

    @Override
    public List<Usuario> seleccionarCandidatos(List<Usuario> candidatos, Partido p) {
        return candidatos.stream()
                .filter(u -> u.getNivelNTRP() >= p.getNtrpMin() && u.getNivelNTRP() <= p.getNtrpMax())
                .sorted(Comparator.comparingDouble(u -> Math.abs(u.getNivelNTRP() - p.getNtrpPromedio())))
                .limit(p.getCupoMaximo())
                .toList();
    }

    @Override
    public String getCodigo() {
        return "NTRP";
    }
}
