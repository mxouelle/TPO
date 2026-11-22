package ar.edu.uade.tenisskillgames.domain.strategy;

import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Emparejamiento por historial y compatibilidad (RF5): combina tasa de
 * asistencia efectiva, fair play, cercania al club y complementariedad de
 * perfil (drive/reves) cuando el partido es de dobles.
 */
@Component("HISTORIAL")
public class PorHistorialStrategy implements MatchmakingStrategy {

    private static final double PESO_ASISTENCIA = 0.40;
    private static final double PESO_FAIR_PLAY = 0.30;
    private static final double PESO_CERCANIA = 0.15;
    private static final double PESO_COMPLEMENTO = 0.15;

    @Override
    public List<Usuario> seleccionarCandidatos(List<Usuario> candidatos, Partido partido) {
        return candidatos.stream()
                .filter(u -> u.getNivelNTRP() >= partido.getNtrpMin() && u.getNivelNTRP() <= partido.getNtrpMax())
                .sorted(Comparator.comparingDouble((Usuario u) -> puntaje(u, partido)).reversed())
                .limit(partido.getCupoMaximo())
                .toList();
    }

    /**
     * Puntaje de compatibilidad entre 0 y 1 (mayor es mejor).
     */
    public double puntaje(Usuario candidato, Partido partido) {
        double asistencia = candidato.getHistorial().tasaAsistencia();
        double fairPlay = candidato.getHistorial().getFairPlayPromedio() / 5.0;

        double cercania = 1.0;
        if (partido.getRadioMaxKm() > 0) {
            double distancia = partido.distanciaAlClubKm(candidato);
            cercania = Math.max(0, 1 - (distancia / partido.getRadioMaxKm()));
        }

        double complemento = 1.0;
        if (partido.getFormato().esDobles() && !partido.getParticipantes().isEmpty()) {
            boolean complementa = partido.getParticipantes().stream()
                    .anyMatch(p -> p.getGolpePreferido().complementaA(candidato.getGolpePreferido()));
            complemento = complementa ? 1.0 : 0.4;
        }

        return asistencia * PESO_ASISTENCIA
                + fairPlay * PESO_FAIR_PLAY
                + cercania * PESO_CERCANIA
                + complemento * PESO_COMPLEMENTO;
    }

    @Override
    public String getCodigo() {
        return "HISTORIAL";
    }
}
