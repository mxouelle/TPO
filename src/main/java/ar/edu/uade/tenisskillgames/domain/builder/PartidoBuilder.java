package ar.edu.uade.tenisskillgames.domain.builder;

import ar.edu.uade.tenisskillgames.domain.enums.FormatoTenis;
import ar.edu.uade.tenisskillgames.domain.enums.Modalidad;
import ar.edu.uade.tenisskillgames.domain.enums.Superficie;
import ar.edu.uade.tenisskillgames.domain.exception.DomainValidationException;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.state.BuscandoJugadoresState;
import ar.edu.uade.tenisskillgames.domain.vo.UbicacionGeo;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Patron Builder: construccion paso a paso con validacion de invariantes
 * antes de instanciar el Partido (RF3 / CU3).
 */
public class PartidoBuilder {

    private final Partido partido = new Partido();

    public PartidoBuilder organizador(Usuario org) {
        partido.setOrganizador(org);
        return this;
    }

    public PartidoBuilder formato(FormatoTenis f) {
        partido.setFormato(f);
        partido.setCupoMaximo(f == FormatoTenis.SINGLES ? 2 : 4);
        return this;
    }

    public PartidoBuilder rangoNTRP(double min, double max) {
        if (min < 1.5 || max > 7.0 || min > max) {
            throw new DomainValidationException("Rango NTRP inválido");
        }
        partido.setNtrpMin(min);
        partido.setNtrpMax(max);
        return this;
    }

    public PartidoBuilder sedeYFecha(String club, Superficie sup, LocalDateTime fecha) {
        if (fecha == null || fecha.isBefore(LocalDateTime.now())) {
            throw new DomainValidationException("Fecha debe ser futura");
        }
        partido.setClubSede(club);
        partido.setSuperficie(sup);
        partido.setFechaHora(fecha);
        return this;
    }

    public PartidoBuilder modalidad(Modalidad modalidad) {
        partido.setModalidad(modalidad);
        return this;
    }

    public PartidoBuilder duracionMinutos(int minutos) {
        if (minutos < 30 || minutos > 300) {
            throw new DomainValidationException("La duración estimada debe estar entre 30 y 300 minutos");
        }
        partido.setDuracionMinutos(minutos);
        return this;
    }

    public PartidoBuilder radioMaxKm(int radioKm) {
        if (radioKm <= 0) {
            throw new DomainValidationException("El radio máximo debe ser mayor a cero");
        }
        partido.setRadioMaxKm(radioKm);
        return this;
    }

    public PartidoBuilder direccion(String direccion) {
        partido.setDireccion(direccion);
        return this;
    }

    public PartidoBuilder ubicacion(double latitud, double longitud) {
        partido.setUbicacion(new UbicacionGeo(latitud, longitud));
        return this;
    }

    public PartidoBuilder estrategia(String estrategia) {
        partido.setEstrategiaEmparejamiento(estrategia);
        return this;
    }

    public PartidoBuilder observaciones(String observaciones) {
        partido.setObservaciones(observaciones);
        return this;
    }

    /**
     * Validaciones finales e instanciacion. El creador queda inscripto
     * automaticamente (CU3) y el partido nace en BUSCANDO_JUGADORES.
     */
    public Partido build() {
        if (partido.getClubSede() == null || partido.getClubSede().isBlank()) {
            throw new DomainValidationException("El club sede es obligatorio");
        }
        if (partido.getFechaHora() == null) {
            throw new DomainValidationException("La fecha y hora son obligatorias");
        }
        if (partido.getNtrpMax() <= 0) {
            throw new DomainValidationException("El rango NTRP es obligatorio");
        }
        if (partido.getCupoMaximo() != partido.getFormato().getJugadoresPorPartido()) {
            throw new DomainValidationException("El cupo máximo no coincide con el formato del partido");
        }

        Objects.requireNonNull(partido.getOrganizador(), "Organizador obligatorio");

        partido.cambiarEstado(new BuscandoJugadoresState());
        partido.agregarParticipante(partido.getOrganizador());
        return partido;
    }
}
