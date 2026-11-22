package ar.edu.uade.tenisskillgames.domain.vo;

import ar.edu.uade.tenisskillgames.domain.exception.DomainValidationException;

import java.util.Objects;

/**
 * Coordenadas geograficas del club o del domicilio del jugador.
 * El calculo de distancia usa la formula de Haversine.
 */
public final class UbicacionGeo {

    private static final double RADIO_TIERRA_KM = 6371.0088;

    private final double latitud;
    private final double longitud;

    public UbicacionGeo(double latitud, double longitud) {
        if (latitud < -90 || latitud > 90) {
            throw new DomainValidationException("Latitud fuera de rango: " + latitud);
        }
        if (longitud < -180 || longitud > 180) {
            throw new DomainValidationException("Longitud fuera de rango: " + longitud);
        }
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public static UbicacionGeo de(double latitud, double longitud) {
        return new UbicacionGeo(latitud, longitud);
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    /**
     * Distancia en kilometros entre dos puntos (Haversine).
     */
    public double distanciaKm(UbicacionGeo otra) {
        Objects.requireNonNull(otra, "La ubicacion destino es obligatoria");
        double dLat = Math.toRadians(otra.latitud - this.latitud);
        double dLon = Math.toRadians(otra.longitud - this.longitud);
        double lat1 = Math.toRadians(this.latitud);
        double lat2 = Math.toRadians(otra.latitud);

        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RADIO_TIERRA_KM * c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UbicacionGeo that)) {
            return false;
        }
        return Double.compare(that.latitud, latitud) == 0 && Double.compare(that.longitud, longitud) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitud, longitud);
    }

    @Override
    public String toString() {
        return "UbicacionGeo{lat=" + latitud + ", lon=" + longitud + '}';
    }
}
