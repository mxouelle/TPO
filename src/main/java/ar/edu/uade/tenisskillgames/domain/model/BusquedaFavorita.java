package ar.edu.uade.tenisskillgames.domain.model;

import ar.edu.uade.tenisskillgames.domain.enums.FormatoTenis;
import ar.edu.uade.tenisskillgames.domain.enums.Superficie;

import java.util.Objects;
import java.util.UUID;

/**
 * Busqueda favorita con suscripcion a alertas (RF2). El usuario se suscribe
 * como Observer: cuando se publica un partido compatible recibe la alerta.
 */
public class BusquedaFavorita {

    private UUID id;
    private Usuario usuario;
    private String nombre;
    private FormatoTenis formato;
    private Superficie superficie;
    private double ntrpMin;
    private double ntrpMax;
    private int radioKm;
    private String zona;
    private boolean activa;

    public BusquedaFavorita() {
        this.id = UUID.randomUUID();
        this.activa = true;
    }

    public BusquedaFavorita(Usuario usuario, String nombre, FormatoTenis formato, Superficie superficie,
                            double ntrpMin, double ntrpMax, int radioKm, String zona) {
        this();
        this.usuario = usuario;
        this.nombre = nombre;
        this.formato = formato;
        this.superficie = superficie;
        this.ntrpMin = ntrpMin;
        this.ntrpMax = ntrpMax;
        this.radioKm = radioKm;
        this.zona = zona;
    }

    public boolean esCompatible(Partido partido) {
        if (!activa) {
            return false;
        }
        boolean formatoOk = formato == null || formato == partido.getFormato();
        boolean superficieOk = superficie == null || superficie == partido.getSuperficie();
        boolean nivelOk = partido.getNtrpMax() >= ntrpMin && partido.getNtrpMin() <= ntrpMax;
        boolean zonaOk = zona == null || zona.isBlank() || partido.getClubSede() == null
                || partido.getClubSede().toLowerCase().contains(zona.toLowerCase())
                || (partido.getUbicacion() != null
                && usuario != null
                && partido.getUbicacion().distanciaKm(usuario.getUbicacion()) <= radioKm);
        return formatoOk && superficieOk && nivelOk && zonaOk;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public FormatoTenis getFormato() {
        return formato;
    }

    public void setFormato(FormatoTenis formato) {
        this.formato = formato;
    }

    public Superficie getSuperficie() {
        return superficie;
    }

    public void setSuperficie(Superficie superficie) {
        this.superficie = superficie;
    }

    public double getNtrpMin() {
        return ntrpMin;
    }

    public void setNtrpMin(double ntrpMin) {
        this.ntrpMin = ntrpMin;
    }

    public double getNtrpMax() {
        return ntrpMax;
    }

    public void setNtrpMax(double ntrpMax) {
        this.ntrpMax = ntrpMax;
    }

    public int getRadioKm() {
        return radioKm;
    }

    public void setRadioKm(int radioKm) {
        this.radioKm = radioKm;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BusquedaFavorita that)) {
            return false;
        }
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
