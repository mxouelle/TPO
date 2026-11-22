package ar.edu.uade.tenisskillgames.domain.model;

import ar.edu.uade.tenisskillgames.domain.enums.Lado;
import ar.edu.uade.tenisskillgames.domain.exception.DomainValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Pareja / equipo de un partido de dobles (RF6).
 */
public class Pareja {

    public static final int MAXIMO_JUGADORES = 2;

    private UUID id;
    private Partido partido;
    private Lado lado;
    private final List<Usuario> jugadores = new ArrayList<>();

    public Pareja() {
        this.id = UUID.randomUUID();
        this.lado = Lado.A;
    }

    public Pareja(Partido partido, Lado lado) {
        this();
        this.partido = partido;
        this.lado = lado;
    }

    public void agregarJugador(Usuario jugador) {
        if (jugadores.size() >= MAXIMO_JUGADORES) {
            throw new DomainValidationException("Una pareja no puede tener mas de 2 jugadores");
        }
        if (!jugadores.contains(jugador)) {
            jugadores.add(jugador);
        }
    }

    public void quitarJugador(Usuario jugador) {
        jugadores.remove(jugador);
    }

    public boolean estaVacia() {
        return jugadores.isEmpty();
    }

    public double getPromedioNTRP() {
        return jugadores.stream().mapToDouble(Usuario::getNivelNTRP).average().orElse(0d);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Partido getPartido() {
        return partido;
    }

    public void setPartido(Partido partido) {
        this.partido = partido;
    }

    public Lado getLado() {
        return lado;
    }

    public void setLado(Lado lado) {
        this.lado = lado;
    }

    public List<Usuario> getJugadores() {
        return jugadores;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pareja pareja)) {
            return false;
        }
        return id != null && id.equals(pareja.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
