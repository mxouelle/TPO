package ar.edu.uade.tenisskillgames.domain.model;

import ar.edu.uade.tenisskillgames.domain.exception.DomainValidationException;

import java.util.Objects;

/**
 * Marcador de un set. Valida las reglas basicas del tenis (RF8):
 * 6-0..6-4, 7-5, 7-6 con tie-break y super tie-break 10-x con diferencia de 2.
 */
public final class SetMarcador {

    private final int gamesLocal;
    private final int gamesVisitante;
    private final boolean tieBreak;

    public SetMarcador(int gamesLocal, int gamesVisitante) {
        this(gamesLocal, gamesVisitante, Math.max(gamesLocal, gamesVisitante) == 7
                && Math.min(gamesLocal, gamesVisitante) == 6);
    }

    public SetMarcador(int gamesLocal, int gamesVisitante, boolean tieBreak) {
        if (gamesLocal < 0 || gamesVisitante < 0) {
            throw new DomainValidationException("Los games de un set no pueden ser negativos");
        }
        if (gamesLocal == gamesVisitante) {
            throw new DomainValidationException("Un set no puede terminar empatado: " + gamesLocal + "-" + gamesVisitante);
        }
        this.gamesLocal = gamesLocal;
        this.gamesVisitante = gamesVisitante;
        this.tieBreak = tieBreak;
    }

    public int getGamesLocal() {
        return gamesLocal;
    }

    public int getGamesVisitante() {
        return gamesVisitante;
    }

    public boolean isTieBreak() {
        return tieBreak;
    }

    public int getGanadorGames() {
        return Math.max(gamesLocal, gamesVisitante);
    }

    public int getPerdedorGames() {
        return Math.min(gamesLocal, gamesVisitante);
    }

    public boolean ganoLocal() {
        return gamesLocal > gamesVisitante;
    }

    /**
     * Reglas de puntuacion aceptadas por la plataforma.
     */
    public boolean esValido() {
        int ganador = getGanadorGames();
        int perdedor = getPerdedorGames();
        int diferencia = ganador - perdedor;

        if (ganador == 6 && perdedor <= 4) {
            return true;
        }
        if (ganador == 7 && (perdedor == 5 || perdedor == 6)) {
            return true;
        }
        // Super tie-break / set corto
        return ganador >= 10 && diferencia >= 2;
    }

    public void validar() {
        if (!esValido()) {
            throw new DomainValidationException(
                    "Marcador de set invalido: " + gamesLocal + "-" + gamesVisitante);
        }
    }

    @Override
    public String toString() {
        return gamesLocal + "-" + gamesVisitante;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SetMarcador that)) {
            return false;
        }
        return gamesLocal == that.gamesLocal
                && gamesVisitante == that.gamesVisitante
                && tieBreak == that.tieBreak;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gamesLocal, gamesVisitante, tieBreak);
    }
}
