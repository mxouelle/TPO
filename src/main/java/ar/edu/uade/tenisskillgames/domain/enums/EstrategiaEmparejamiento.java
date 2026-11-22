package ar.edu.uade.tenisskillgames.domain.enums;

/**
 * Estrategias de emparejamiento seleccionables en tiempo de ejecucion (RF5).
 */
public enum EstrategiaEmparejamiento {

    NTRP,
    CERCANIA,
    HISTORIAL;

    public static EstrategiaEmparejamiento desde(String valor) {
        if (valor == null || valor.isBlank()) {
            return NTRP;
        }
        return EstrategiaEmparejamiento.valueOf(valor.trim().toUpperCase());
    }
}
