package ar.edu.uade.tenisskillgames.domain.vo;

/**
 * Historial deportivo consolidado de un jugador. Alimenta la estrategia
 * de emparejamiento por historial (RF5) y el calculo de reputacion (RF8).
 */
public final class HistorialDeportivo {

    private final int partidosJugados;
    private final int ausencias;
    private final double fairPlayPromedio;
    private final double puntualidadPromedio;

    public HistorialDeportivo(int partidosJugados, int ausencias, double fairPlayPromedio, double puntualidadPromedio) {
        this.partidosJugados = Math.max(partidosJugados, 0);
        this.ausencias = Math.max(ausencias, 0);
        this.fairPlayPromedio = fairPlayPromedio;
        this.puntualidadPromedio = puntualidadPromedio;
    }

    public static HistorialDeportivo nuevo() {
        return new HistorialDeportivo(0, 0, 5.0, 5.0);
    }

    public int getPartidosJugados() {
        return partidosJugados;
    }

    public int getAusencias() {
        return ausencias;
    }

    public double getFairPlayPromedio() {
        return fairPlayPromedio;
    }

    public double getPuntualidadPromedio() {
        return puntualidadPromedio;
    }

    /**
     * Tasa de asistencia efectiva entre 0 y 1 (1 = nunca falto sin avisar).
     */
    public double tasaAsistencia() {
        int total = partidosJugados + ausencias;
        if (total == 0) {
            return 1.0;
        }
        return (double) partidosJugados / total;
    }
}
