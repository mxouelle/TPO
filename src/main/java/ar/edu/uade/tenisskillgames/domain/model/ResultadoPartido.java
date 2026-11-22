package ar.edu.uade.tenisskillgames.domain.model;

import ar.edu.uade.tenisskillgames.domain.enums.Lado;
import ar.edu.uade.tenisskillgames.domain.exception.DomainValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Resultado oficial del partido: marcador por sets, duracion real y observaciones (RF8).
 */
public class ResultadoPartido {

    private UUID id;
    private Partido partido;
    private Lado ganadorLado;
    private final List<SetMarcador> marcadorSets = new ArrayList<>();
    private int duracionRealMinutos;
    private String observaciones;
    private boolean validado;
    private LocalDateTime fechaCarga;

    public ResultadoPartido() {
        this.id = UUID.randomUUID();
        this.fechaCarga = LocalDateTime.now();
    }

    public ResultadoPartido(Partido partido, Lado ganadorLado, List<SetMarcador> sets,
                            int duracionRealMinutos, String observaciones) {
        this();
        this.partido = partido;
        this.ganadorLado = ganadorLado;
        if (sets != null) {
            sets.forEach(this::agregarSet);
        }
        if (duracionRealMinutos <= 0) {
            throw new DomainValidationException("La duración real debe ser mayor a cero minutos");
        }
        this.duracionRealMinutos = duracionRealMinutos;
        this.observaciones = observaciones;
    }

    public void agregarSet(SetMarcador set) {
        Objects.requireNonNull(set, "El set es obligatorio");
        set.validar();
        marcadorSets.add(set);
    }

    /**
     * Valida coherencia: al menos un set, sets validos y ganador acorde al marcador.
     */
    public void validar() {
        if (marcadorSets.isEmpty()) {
            throw new DomainValidationException("El resultado debe incluir al menos un set");
        }
        marcadorSets.forEach(SetMarcador::validar);
        if (ganadorLado == null) {
            throw new DomainValidationException("Debe indicarse el lado ganador");
        }
        long setsA = marcadorSets.stream().filter(SetMarcador::ganoLocal).count();
        long setsB = marcadorSets.size() - setsA;
        Lado calculado = setsA > setsB ? Lado.A : Lado.B;
        if (calculado != ganadorLado) {
            throw new DomainValidationException("El ganador declarado no coincide con el marcador cargado");
        }
        if (setsA == setsB) {
            throw new DomainValidationException("El marcador no puede terminar empatado en sets");
        }
    }

    public String getMarcadorComoTexto() {
        return marcadorSets.stream().map(SetMarcador::toString).reduce((a, b) -> a + " " + b).orElse("");
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

    public Lado getGanadorLado() {
        return ganadorLado;
    }

    public void setGanadorLado(Lado ganadorLado) {
        this.ganadorLado = ganadorLado;
    }

    public List<SetMarcador> getMarcadorSets() {
        return marcadorSets;
    }

    public int getDuracionRealMinutos() {
        return duracionRealMinutos;
    }

    public void setDuracionRealMinutos(int duracionRealMinutos) {
        this.duracionRealMinutos = duracionRealMinutos;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public boolean isValidado() {
        return validado;
    }

    public void setValidado(boolean validado) {
        this.validado = validado;
    }

    public LocalDateTime getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(LocalDateTime fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResultadoPartido that)) {
            return false;
        }
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
