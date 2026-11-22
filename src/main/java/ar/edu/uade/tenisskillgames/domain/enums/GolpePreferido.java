package ar.edu.uade.tenisskillgames.domain.enums;

/**
 * Golpe / posicion preferida en dobles (RF1). Se usa para la
 * complementariedad de perfil de la estrategia por historial (RF5).
 */
public enum GolpePreferido {

    DRIVE,
    REVES,
    AMBOS;

    public boolean complementaA(GolpePreferido otro) {
        if (this == AMBOS || otro == null || otro == AMBOS) {
            return true;
        }
        return this != otro;
    }
}
