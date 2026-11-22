package ar.edu.uade.tenisskillgames.domain.enums;

/**
 * Lado de la cancha / equipo. En singles se usa A y B indistintamente.
 */
public enum Lado {

    A,
    B,
    INDIFERENTE;

    public Lado opuesto() {
        return switch (this) {
            case A -> B;
            case B -> A;
            case INDIFERENTE -> INDIFERENTE;
        };
    }
}
