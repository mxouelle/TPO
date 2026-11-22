package ar.edu.uade.tenisskillgames.domain.enums;

/**
 * Superficies soportadas por la plataforma (RF11).
 * Las superficies de polvo de ladrillo al aire libre son las mas sensibles a la lluvia.
 */
public enum Superficie {

    POLVO_DE_LADRILLO(true),
    RAPIDA_CEMENTO(false),
    CESPED_SINTETICO(true);

    private final boolean sensibleAlClima;

    Superficie(boolean sensibleAlClima) {
        this.sensibleAlClima = sensibleAlClima;
    }

    public boolean isSensibleAlClima() {
        return sensibleAlClima;
    }
}
