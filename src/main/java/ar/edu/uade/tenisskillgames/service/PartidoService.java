package ar.edu.uade.tenisskillgames.service;

import ar.edu.uade.tenisskillgames.controller.dto.Comandos;
import ar.edu.uade.tenisskillgames.controller.dto.Vistas;
import ar.edu.uade.tenisskillgames.controller.mapper.VistaMapper;
import ar.edu.uade.tenisskillgames.domain.builder.PartidoBuilder;
import ar.edu.uade.tenisskillgames.domain.enums.EstrategiaEmparejamiento;
import ar.edu.uade.tenisskillgames.domain.enums.Rol;
import ar.edu.uade.tenisskillgames.domain.event.PartidoCreadoEvent;
import ar.edu.uade.tenisskillgames.domain.exception.OperacionNoPermitidaException;
import ar.edu.uade.tenisskillgames.domain.exception.RecursoNoEncontradoException;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.port.FiltroPartido;
import ar.edu.uade.tenisskillgames.domain.port.PartidoRepository;
import ar.edu.uade.tenisskillgames.domain.port.UsuarioRepository;
import ar.edu.uade.tenisskillgames.domain.vo.UbicacionGeo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Casos de uso CU3 (creacion), CU7 (inicio), CU8 (cierre) y CU9 (cancelacion).
 */
@Service
public class PartidoService {

    private static final long HORAS_CANCELACION_TARDIA = 3;

    private final PartidoRepository partidos;
    private final UsuarioRepository usuarios;
    private final NotificacionService notificaciones;
    private final VistaMapper mapper;

    public PartidoService(PartidoRepository partidos, UsuarioRepository usuarios,
                          NotificacionService notificaciones, VistaMapper mapper) {
        this.partidos = partidos;
        this.usuarios = usuarios;
        this.notificaciones = notificaciones;
        this.mapper = mapper;
    }

    // ------------------------------------------------------------------ CU3

    @Transactional
    public Vistas.Partido crear(UUID organizadorId, Comandos.CrearPartido cmd) {
        Usuario organizador = usuarios.buscarPorId(organizadorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        organizador.validarPuedePostular();

        Partido partido = new PartidoBuilder()
                .organizador(organizador)
                .formato(cmd.formato())
                .rangoNTRP(cmd.ntrpMin(), cmd.ntrpMax())
                .sedeYFecha(cmd.clubSede(), cmd.superficie(), cmd.fechaHora())
                .direccion(cmd.direccion())
                .ubicacion(cmd.latitud(), cmd.longitud())
                .duracionMinutos(cmd.duracionMinutos())
                .radioMaxKm(cmd.radioMaxKm())
                .modalidad(cmd.modalidad())
                .estrategia(cmd.estrategia() == null || cmd.estrategia().isBlank()
                        ? EstrategiaEmparejamiento.NTRP.name() : cmd.estrategia().toUpperCase())
                .observaciones(cmd.observaciones())
                .build();

        Partido guardado = partidos.guardar(partido);
        guardado.publicarEvento(new PartidoCreadoEvent(guardado.getId(),
                organizador.getId(), guardado.getClubSede(),
                guardado.getSuperficie().name(), guardado.getFechaHora()));
        return mapper.partido(guardado);
    }

    // ------------------------------------------------------------------ consultas

    @Transactional(readOnly = true)
    public List<Vistas.Partido> buscar(FiltroPartido filtro) {
        return partidos.buscar(filtro).stream().map(mapper::partido).toList();
    }

    @Transactional(readOnly = true)
    public Partido buscarEntidad(UUID id) {
        return partidos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Partido no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public Vistas.Partido obtener(UUID id) {
        return mapper.partido(buscarEntidad(id));
    }

    @Transactional(readOnly = true)
    public List<Vistas.Partido> deJugador(UUID usuarioId) {
        return partidos.buscarPorParticipante(usuarioId).stream().map(mapper::partido).toList();
    }

    @Transactional(readOnly = true)
    public List<Vistas.Partido> deOrganizador(UUID organizadorId) {
        return partidos.buscarPorOrganizador(organizadorId).stream().map(mapper::partido).toList();
    }

    /**
     * Filtro dinamico con radio respecto a la ubicacion del jugador autenticado.
     */
    @Transactional(readOnly = true)
    public List<Vistas.Partido> buscarCercanos(FiltroPartido filtro, UUID usuarioId) {
        FiltroPartido efectivo = filtro;
        if (usuarioId != null && filtro.radioKm() != null && filtro.ubicacionReferencia() == null) {
            UbicacionGeo ubicacion = usuarios.buscarPorId(usuarioId)
                    .map(Usuario::getUbicacion)
                    .orElse(null);
            efectivo = new FiltroPartido(filtro.formato(), filtro.superficie(), filtro.ntrpMin(),
                    filtro.ntrpMax(), filtro.radioKm(), filtro.fecha(), filtro.horaDesde(),
                    filtro.horaHasta(), filtro.zona(), filtro.estado(), ubicacion, usuarioId);
        }
        return buscar(efectivo);
    }

    // ------------------------------------------------------------------ CU7 / CU8

    @Transactional
    public Vistas.Partido iniciar(UUID partidoId, UUID usuarioId) {
        Partido partido = buscarEntidad(partidoId);
        validarOrganizadorOAdmin(partido, usuarioId, "iniciar");
        if (LocalDateTime.now().isBefore(partido.getFechaHora())) {
            throw new OperacionNoPermitidaException("Todavía no llegó la hora de inicio del partido");
        }
        partido.iniciar();
        return mapper.partido(partidos.guardar(partido));
    }

    /**
     * Cierre manual del organizador (CU8).
     */
    @Transactional
    public Vistas.Partido finalizar(UUID partidoId, UUID usuarioId) {
        Partido partido = buscarEntidad(partidoId);
        validarOrganizadorOAdmin(partido, usuarioId, "finalizar");
        partido.finalizar();
        return mapper.partido(partidos.guardar(partido));
    }

    @Transactional
    public Vistas.Partido cancelar(UUID partidoId, UUID usuarioId, String motivo) {
        Partido partido = buscarEntidad(partidoId);
        Usuario solicitante = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        boolean esOrganizador = partido.getOrganizador() != null
                && partido.getOrganizador().getId().equals(usuarioId);
        if (!esOrganizador && !solicitante.tieneRol(Rol.ADMIN)) {
            throw new OperacionNoPermitidaException("Solo el organizador o un administrador puede cancelar el partido");
        }
        if (!partido.getEstado().permiteCancelacion()) {
            throw new OperacionNoPermitidaException("No se puede cancelar un partido en estado " + partido.getEstado());
        }

        boolean tardia = Duration.between(LocalDateTime.now(), partido.getFechaHora()).toMinutes()
                < HORAS_CANCELACION_TARDIA * 60;

        partido.cancelar(motivo);

        if (tardia && !esMotivoClimatico(motivo)) {
            solicitante.aplicarStrike();
            usuarios.guardar(solicitante);
        }

        Vistas.Partido vista = mapper.partido(partidos.guardar(partido));
        notificaciones.enviar(solicitante, partido.getId(),
                ar.edu.uade.tenisskillgames.domain.enums.TipoEvento.PARTIDO_CANCELADO,
                "Cancelaste un partido", "El partido en " + partido.getClubSede() + " fue cancelado. Motivo: " + motivo);
        return vista;
    }

    private boolean esMotivoClimatico(String motivo) {
        String texto = motivo == null ? "" : motivo.toLowerCase();
        return texto.contains("lluvia") || texto.contains("clima") || texto.contains("tormenta")
                || texto.contains("viento");
    }

    // ------------------------------------------------------------------ parejas (RF6)

    @Transactional
    public Vistas.Partido asignarPareja(UUID partidoId, UUID usuarioId, Comandos.AsignarPareja cmd) {
        Partido partido = buscarEntidad(partidoId);
        validarOrganizadorOAdmin(partido, usuarioId, "reasignar parejas");
        if (partido.getEstado() == ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido.CONFIRMADO
                || partido.getEstado().esEstadoFinal()) {
            throw new OperacionNoPermitidaException("Las parejas solo se pueden reasignar antes de la confirmación definitiva");
        }

        List<Usuario> jugadores = cmd.jugadores().stream()
                .map(id -> partido.getParticipantes().stream()
                        .filter(p -> p.getId().equals(id))
                        .findFirst()
                        .orElseThrow(() -> new RecursoNoEncontradoException("El jugador " + id + " no participa del partido")))
                .toList();

        partido.reasignarPareja(cmd.lado(), jugadores);
        partido.validarEquilibrioParejas(0.5);
        return mapper.partido(partidos.guardar(partido));
    }

    @Transactional
    public Vistas.Partido rearmarParejas(UUID partidoId, UUID usuarioId) {
        Partido partido = buscarEntidad(partidoId);
        validarOrganizadorOAdmin(partido, usuarioId, "rearmar parejas");
        partido.reasignarParejas();
        return mapper.partido(partidos.guardar(partido));
    }

    // ------------------------------------------------------------------ helpers

    private void validarOrganizadorOAdmin(Partido partido, UUID usuarioId, String accion) {
        boolean esOrganizador = partido.getOrganizador() != null
                && partido.getOrganizador().getId().equals(usuarioId);
        if (esOrganizador) {
            return;
        }
        boolean esAdmin = usuarios.buscarPorId(usuarioId)
                .map(u -> u.tieneRol(Rol.ADMIN))
                .orElse(false);
        if (!esAdmin) {
            throw new OperacionNoPermitidaException("Solo el organizador o un administrador puede " + accion + " el partido");
        }
    }
}
