package ar.edu.uade.tenisskillgames.domain.enums;

/**
 * Formatos de partido soportados. El cupo maximo se deriva del formato (RF3).
 */
public enum FormatoTenis {

    SINGLES(2),
    DOBLES(4),
    DOBLES_MIXTOS(4);

    private final int jugadoresPorPartido;

    FormatoTenis(int jugadoresPorPartido) {
        this.jugadoresPorPartido = jugadoresPorPartido;
    }

    public int getJugadoresPorPartido() {
        return jugadoresPorPartido;
    }

    public boolean esDobles() {
        return this != SINGLES;
    }
}
