package ar.edu.uade.tenisskillgames.domain.model;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoPostulacion;
import ar.edu.uade.tenisskillgames.domain.enums.FormatoTenis;
import ar.edu.uade.tenisskillgames.domain.enums.Lado;
import ar.edu.uade.tenisskillgames.domain.enums.Modalidad;
import ar.edu.uade.tenisskillgames.domain.enums.Superficie;
import ar.edu.uade.tenisskillgames.domain.event.DomainEvent;
import ar.edu.uade.tenisskillgames.domain.event.DomainEventPublisher;
import ar.edu.uade.tenisskillgames.domain.event.PartidoCanceladoEvent;
import ar.edu.uade.tenisskillgames.domain.event.PartidoEstadoCambiadoEvent;
import ar.edu.uade.tenisskillgames.domain.exception.DomainValidationException;
import ar.edu.uade.tenisskillgames.domain.exception.OperacionNoPermitidaException;
import ar.edu.uade.tenisskillgames.domain.state.BuscandoJugadoresState;
import ar.edu.uade.tenisskillgames.domain.state.PartidoState;
import ar.edu.uade.tenisskillgames.domain.vo.UbicacionGeo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Agregado raiz Partido. Context del patron State (RF4) y sujeto de los
 * eventos de dominio (Observer). No depende de Spring ni de JPA.
 */
public class Partido {

    private UUID id;
    private Usuario organizador;
    private FormatoTenis formato;
    private Superficie superficie;
    private String clubSede;
    private String direccion;
    private UbicacionGeo ubicacion;
    private double ntrpMin;
    private double ntrpMax;
    private int radioMaxKm;
    private LocalDateTime fechaHora;
    private int duracionMinutos;
    private Modalidad modalidad;
    private int cupoMaximo;
    private EstadoPartido estado;
    private PartidoState estadoActual;
    private String estrategiaEmparejamiento;
    private String motivoCancelacion;
    private LocalDateTime fechaCreacion;
    private String observaciones;
    private boolean recordatorio24Enviado;
    private boolean recordatorio2Enviado;

    private final List<Usuario> participantes = new ArrayList<>();
    private final List<Usuario> listaEspera = new ArrayList<>();
    private final List<Postulacion> postulaciones = new ArrayList<>();
    private final List<Confirmacion> confirmaciones = new ArrayList<>();
    private final List<Pareja> parejas = new ArrayList<>();
    private final List<TransicionEstado> historialEstados = new ArrayList<>();

    private ResultadoPartido resultado;
    private DomainEventPublisher eventPublisher = DomainEventPublisher.NO_OP;

    public Partido() {
        this.id = UUID.randomUUID();
        this.formato = FormatoTenis.SINGLES;
        this.superficie = Superficie.POLVO_DE_LADRILLO;
        this.modalidad = Modalidad.AMISTOSO_RECREATIVO;
        this.cupoMaximo = FormatoTenis.SINGLES.getJugadoresPorPartido();
        this.estado = EstadoPartido.BUSCANDO_JUGADORES;
        this.estadoActual = new BuscandoJugadoresState();
        this.fechaCreacion = LocalDateTime.now();
        this.ubicacion = UbicacionGeo.de(0, 0);
        this.radioMaxKm = 10;
        this.duracionMinutos = 90;
        this.estrategiaEmparejamiento = "NTRP";
    }

    // ------------------------------------------------------------------ ciclo de vida (State)

    public void postular(Usuario jugador) {
        Objects.requireNonNull(jugador, "El jugador es obligatorio");
        if (esParticipante(jugador)) {
            throw new OperacionNoPermitidaException("El jugador ya está inscripto en este partido");
        }
        if (estaEnEspera(jugador)) {
            throw new OperacionNoPermitidaException("El jugador ya está en lista de espera");
        }
        if (jugador.estaSuspendido()) {
            throw new OperacionNoPermitidaException(
                    "El jugador está suspendido hasta " + jugador.getRehabilitacion());
        }
        estadoActual.postular(this, jugador);
    }

    public void confirmar(Usuario jugador) {
        Objects.requireNonNull(jugador, "El jugador es obligatorio");
        estadoActual.confirmar(this, jugador);
    }

    public void iniciar() {
        estadoActual.iniciar(this);
    }

    public void finalizar() {
        estadoActual.finalizar(this);
    }

    public void cancelar(String motivo) {
        this.motivoCancelacion = motivo;
        estadoActual.cancelar(this);
        publicarEvento(new PartidoCanceladoEvent(id, motivo));
    }

    public void cambiarEstado(PartidoState nuevo) {
        Objects.requireNonNull(nuevo, "El estado destino es obligatorio");
        EstadoPartido anterior = this.estado;
        EstadoPartido destino = nuevo.getEstado();
        this.estadoActual = nuevo;
        this.estado = destino;
        if (anterior != destino) {
            this.historialEstados.add(new TransicionEstado(anterior, destino, LocalDateTime.now(), motivoCancelacion));
            // Auditoria + notificaciones: el suscriptor de notificaciones ignora CUPO_COMPLETO
            // y CANCELADO porque esos flujos publican eventos dedicados.
            publicarEvento(new PartidoEstadoCambiadoEvent(id, anterior, destino));
        }
    }

    // ------------------------------------------------------------------ participantes

    public void agregarParticipante(Usuario jugador) {
        if (!participantes.contains(jugador)) {
            participantes.add(jugador);
        }
        listaEspera.remove(jugador);
        registrarPostulacion(jugador, EstadoPostulacion.ACEPTADA, Lado.INDIFERENTE);
        if (formato.esDobles()) {
            reasignarParejas();
        }
    }

    private void registrarPostulacion(Usuario jugador, EstadoPostulacion estado, Lado lado) {
        postulaciones.stream()
                .filter(p -> p.getUsuario().equals(jugador))
                .findFirst()
                .ifPresentOrElse(
                        p -> p.cambiarEstado(estado),
                        () -> postulaciones.add(new Postulacion(jugador, this, lado, estado)));
    }

    public void agregarAEspera(Usuario jugador) {
        if (!listaEspera.contains(jugador) && !participantes.contains(jugador)) {
            listaEspera.add(jugador);
            postulaciones.add(new Postulacion(jugador, this, Lado.INDIFERENTE, EstadoPostulacion.LISTA_ESPERA));
        }
    }

    /**
     * RF6: libera la plaza de un jugador que rechaza o cancela su participacion.
     */
    public void liberarPlaza(Usuario jugador) {
        if (!participantes.remove(jugador)) {
            throw new OperacionNoPermitidaException("El jugador no participa de este partido");
        }
        confirmaciones.removeIf(c -> c.getUsuario().equals(jugador));
        parejas.forEach(p -> p.quitarJugador(jugador));
        parejas.removeIf(Pareja::estaVacia);
        postulaciones.stream()
                .filter(p -> p.getUsuario().equals(jugador))
                .forEach(p -> p.cambiarEstado(EstadoPostulacion.RECHAZADA));
        if (estado == EstadoPartido.CUPO_COMPLETO || estado == EstadoPartido.CONFIRMADO) {
            cambiarEstado(new BuscandoJugadoresState());
        }
    }

    /**
     * Promueve al primer candidato de la lista de espera y devuelve el jugador promovido.
     */
    public Optional<Usuario> promoverListaEspera() {
        if (listaEspera.isEmpty() || estado.esEstadoFinal() || getPlazasLibres() == 0) {
            return Optional.empty();
        }
        Usuario promovido = listaEspera.remove(0);
        agregarParticipante(promovido);
        if (estaCupoCompleto()) {
            cambiarEstado(new ar.edu.uade.tenisskillgames.domain.state.CupoCompletoState());
        }
        return Optional.of(promovido);
    }

    public void registrarConfirmacion(Usuario jugador, boolean confirmado) {
        if (!esParticipante(jugador)) {
            throw new OperacionNoPermitidaException("El jugador no participa de este partido");
        }
        confirmaciones.stream()
                .filter(c -> c.getUsuario().equals(jugador))
                .findFirst()
                .ifPresentOrElse(
                        c -> c.actualizar(confirmado),
                        () -> confirmaciones.add(new Confirmacion(jugador, this, confirmado)));
    }

    public boolean estaConfirmado(Usuario jugador) {
        return confirmaciones.stream()
                .anyMatch(c -> c.getUsuario().equals(jugador) && c.isConfirmado());
    }

    public boolean todosConfirmaron() {
        if (participantes.isEmpty()) {
            return false;
        }
        return participantes.stream().allMatch(this::estaConfirmado);
    }

    public long confirmacionesPendientes() {
        return participantes.stream().filter(p -> !estaConfirmado(p)).count();
    }

    public boolean esParticipante(Usuario jugador) {
        return participantes.stream().anyMatch(p -> p.equals(jugador));
    }

    public boolean estaEnEspera(Usuario jugador) {
        return listaEspera.stream().anyMatch(p -> p.equals(jugador));
    }

    public boolean estaCupoCompleto() {
        return participantes.size() >= cupoMaximo;
    }

    public int getPlazasLibres() {
        return Math.max(0, cupoMaximo - participantes.size());
    }

    // ------------------------------------------------------------------ parejas (dobles)

    /**
     * Arma las parejas ordenando por NTRP y cruzando extremos para equilibrar
     * el promedio de cada equipo (CU5).
     */
    public void reasignarParejas() {
        parejas.clear();
        if (!formato.esDobles()) {
            return;
        }
        List<Usuario> ordenados = new ArrayList<>(participantes);
        ordenados.sort(Comparator.comparingDouble(Usuario::getNivelNTRP));
        Pareja equipoA = new Pareja(this, Lado.A);
        Pareja equipoB = new Pareja(this, Lado.B);
        for (int i = 0; i < ordenados.size(); i++) {
            if (i % 2 == 0) {
                equipoA.agregarJugador(ordenados.get(i));
            } else {
                equipoB.agregarJugador(ordenados.get(i));
            }
        }
        parejas.add(equipoA);
        parejas.add(equipoB);
    }

    public double getNtrpPromedioPareja(Lado lado) {
        return parejas.stream()
                .filter(p -> p.getLado() == lado)
                .findFirst()
                .map(Pareja::getPromedioNTRP)
                .orElse(0d);
    }

    /**
     * Regla CU5: la diferencia de NTRP promedio entre parejas no debe exceder 0.5.
     */
    public void validarEquilibrioParejas(double deltaMaximo) {
        if (!formato.esDobles() || parejas.size() < 2) {
            return;
        }
        double diferencia = Math.abs(getNtrpPromedioPareja(Lado.A) - getNtrpPromedioPareja(Lado.B));
        if (diferencia > deltaMaximo) {
            throw new DomainValidationException(
                    "Las parejas no están equilibradas: diferencia de NTRP promedio " + String.format("%.2f", diferencia)
                            + " (máximo " + deltaMaximo + ")");
        }
    }

    public void reasignarPareja(Lado lado, List<Usuario> jugadores) {
        if (!formato.esDobles()) {
            throw new OperacionNoPermitidaException("Solo los partidos de dobles tienen parejas");
        }
        if (jugadores == null || jugadores.isEmpty() || jugadores.size() > 2) {
            throw new DomainValidationException("Una pareja debe tener 1 o 2 jugadores");
        }
        jugadores.forEach(j -> {
            if (!esParticipante(j)) {
                throw new OperacionNoPermitidaException(j.getNombre() + " no participa del partido");
            }
        });
        parejas.removeIf(p -> p.getLado() == lado);
        Pareja pareja = new Pareja(this, lado);
        jugadores.forEach(pareja::agregarJugador);
        parejas.add(pareja);
    }

    // ------------------------------------------------------------------ resultados

    public void cargarResultado(ResultadoPartido resultado) {
        if (estado != EstadoPartido.FINALIZADO) {
            throw new OperacionNoPermitidaException("Solo se puede cargar el resultado de un partido finalizado");
        }
        if (this.resultado != null) {
            throw new OperacionNoPermitidaException("El resultado del partido ya fue cargado");
        }
        this.resultado = Objects.requireNonNull(resultado, "El resultado es obligatorio");
    }

    // ------------------------------------------------------------------ consultas de negocio

    public double getNtrpPromedio() {
        if (participantes.isEmpty()) {
            return (ntrpMin + ntrpMax) / 2;
        }
        return participantes.stream().mapToDouble(Usuario::getNivelNTRP).average().orElse((ntrpMin + ntrpMax) / 2);
    }

    public boolean esCompatibleNivel(Usuario jugador) {
        return jugador.getNivelNTRP() >= ntrpMin && jugador.getNivelNTRP() <= ntrpMax;
    }

    public double distanciaAlClubKm(Usuario jugador) {
        return ubicacion.distanciaKm(jugador.getUbicacion());
    }

    public boolean dentroDelRadio(Usuario jugador) {
        return distanciaAlClubKm(jugador) <= radioMaxKm;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHora.plusMinutes(duracionMinutos);
    }

    public Duration tiempoHastaInicio() {
        return Duration.between(LocalDateTime.now(), fechaHora);
    }

    public boolean puedeCancelarse() {
        return estado.permiteCancelacion();
    }

    public void publicarEvento(DomainEvent evento) {
        if (eventPublisher != null) {
            eventPublisher.publish(evento);
        }
    }

    public void agregarTransicion(TransicionEstado transicion) {
        this.historialEstados.add(transicion);
    }

    // ------------------------------------------------------------------ getters / setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Usuario getOrganizador() {
        return organizador;
    }

    public void setOrganizador(Usuario organizador) {
        this.organizador = organizador;
    }

    public FormatoTenis getFormato() {
        return formato;
    }

    public void setFormato(FormatoTenis formato) {
        this.formato = formato;
        this.cupoMaximo = formato.getJugadoresPorPartido();
    }

    public Superficie getSuperficie() {
        return superficie;
    }

    public void setSuperficie(Superficie superficie) {
        this.superficie = superficie;
    }

    public String getClubSede() {
        return clubSede;
    }

    public void setClubSede(String clubSede) {
        this.clubSede = clubSede;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public UbicacionGeo getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(UbicacionGeo ubicacion) {
        this.ubicacion = Objects.requireNonNullElse(ubicacion, UbicacionGeo.de(0, 0));
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

    public int getRadioMaxKm() {
        return radioMaxKm;
    }

    public void setRadioMaxKm(int radioMaxKm) {
        this.radioMaxKm = radioMaxKm;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(Modalidad modalidad) {
        this.modalidad = modalidad;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(int cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public EstadoPartido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPartido estado) {
        this.estado = estado;
    }

    public PartidoState getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(PartidoState estadoActual) {
        this.estadoActual = estadoActual;
    }

    public List<Usuario> getParticipantes() {
        return participantes;
    }

    public List<Usuario> getListaEspera() {
        return listaEspera;
    }

    public List<Postulacion> getPostulaciones() {
        return postulaciones;
    }

    public List<Confirmacion> getConfirmaciones() {
        return confirmaciones;
    }

    public List<Pareja> getParejas() {
        return parejas;
    }

    public List<TransicionEstado> getHistorialEstados() {
        return historialEstados;
    }

    public ResultadoPartido getResultado() {
        return resultado;
    }

    public void setResultado(ResultadoPartido resultado) {
        this.resultado = resultado;
    }

    public String getEstrategiaEmparejamiento() {
        return estrategiaEmparejamiento;
    }

    public void setEstrategiaEmparejamiento(String estrategiaEmparejamiento) {
        this.estrategiaEmparejamiento = estrategiaEmparejamiento;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public boolean isRecordatorio24Enviado() {
        return recordatorio24Enviado;
    }

    public void setRecordatorio24Enviado(boolean recordatorio24Enviado) {
        this.recordatorio24Enviado = recordatorio24Enviado;
    }

    public boolean isRecordatorio2Enviado() {
        return recordatorio2Enviado;
    }

    public void setRecordatorio2Enviado(boolean recordatorio2Enviado) {
        this.recordatorio2Enviado = recordatorio2Enviado;
    }

    public DomainEventPublisher getEventPublisher() {
        return eventPublisher;
    }

    public void setEventPublisher(DomainEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher == null ? DomainEventPublisher.NO_OP : eventPublisher;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Partido partido)) {
            return false;
        }
        return id != null && id.equals(partido.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Partido{" + clubSede + ", " + formato + ", " + fechaHora + ", " + estado + '}';
    }
}
