package ar.edu.uade.tenisskillgames.service;

import ar.edu.uade.tenisskillgames.controller.dto.Vistas;
import ar.edu.uade.tenisskillgames.controller.mapper.VistaMapper;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.enums.Lado;
import ar.edu.uade.tenisskillgames.domain.enums.TipoEvento;
import ar.edu.uade.tenisskillgames.domain.exception.ConflictoException;
import ar.edu.uade.tenisskillgames.domain.exception.DomainValidationException;
import ar.edu.uade.tenisskillgames.domain.exception.OperacionNoPermitidaException;
import ar.edu.uade.tenisskillgames.domain.exception.RecursoNoEncontradoException;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.port.PartidoRepository;
import ar.edu.uade.tenisskillgames.domain.port.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Caso de uso CU4: postulacion a un partido, con validacion de categoria,
 * distancia, superposicion horaria y derivacion a lista de espera (RF6).
 */
@Service
public class PostulacionService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PartidoRepository partidos;
    private final UsuarioRepository usuarios;
    private final NotificacionService notificaciones;
    private final VistaMapper mapper;

    public PostulacionService(PartidoRepository partidos, UsuarioRepository usuarios,
                              NotificacionService notificaciones, VistaMapper mapper) {
        this.partidos = partidos;
        this.usuarios = usuarios;
        this.notificaciones = notificaciones;
        this.mapper = mapper;
    }

    /**
     * Postula al usuario autenticado evaluando compatibilidad (CA1).
     */
    @Transactional
    public Vistas.Partido postular(java.util.UUID partidoId, java.util.UUID usuarioId, Lado ladoDeseado) {
        Usuario jugador = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        jugador.validarPuedePostular();

        Partido partido = partidos.buscarPorId(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Partido no encontrado: " + partidoId));

        if (partido.getEstado().esEstadoFinal()) {
            throw new OperacionNoPermitidaException("El partido está " + partido.getEstado()
                    + " y no admite postulaciones");
        }
        if (!partido.esCompatibleNivel(jugador)) {
            throw new DomainValidationException("Incompatibilidad de categoría: NTRP " + jugador.getNivelNTRP()
                    + " fuera del rango " + partido.getNtrpMin() + "-" + partido.getNtrpMax() + " del partido");
        }
        if (!partido.dentroDelRadio(jugador)) {
            throw new DomainValidationException("El jugador está a "
                    + Math.round(partido.distanciaAlClubKm(jugador)) + " km del club, fuera del radio de "
                    + partido.getRadioMaxKm() + " km");
        }
        if (partidos.existeSolapamiento(usuarioId, partido.getFechaHora(), partido.getFechaHoraFin())) {
            throw new ConflictoException("El jugador ya tiene otro partido en ese horario");
        }

        boolean habiaCupo = partido.getPlazasLibres() > 0;
        partido.postular(jugador);
        Lado lado = ladoDeseado == null ? Lado.INDIFERENTE : ladoDeseado;
        partido.getPostulaciones().stream()
                .filter(p -> p.getUsuario().getId().equals(jugador.getId()))
                .forEach(p -> p.setLadoDeseado(lado));
        Vistas.Partido vista = mapper.partido(partidos.guardar(partido));

        if (!habiaCupo) {
            notificaciones.enviar(jugador, partido.getId(), TipoEvento.CUPO_COMPLETO,
                    "Quedaste en lista de espera",
                    "El partido del " + partido.getFechaHora().format(FECHA) + " en " + partido.getClubSede()
                            + " no tiene cupo. Te avisaremos si se libera una plaza.");
        }
        return vista;
    }

    /**
     * Baja de un participante antes del inicio (RF6): libera la plaza y
     * promueve al siguiente candidato de la lista de espera.
     */
    @Transactional
    public Vistas.Partido darDeBaja(java.util.UUID partidoId, java.util.UUID usuarioId, String motivo) {
        Partido partido = partidos.buscarPorId(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Partido no encontrado: " + partidoId));
        Usuario jugador = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        partido.liberarPlaza(jugador);
        Optional<Usuario> promovido = partido.promoverListaEspera();
        Vistas.Partido vista = mapper.partido(partidos.guardar(partido));

        promovido.ifPresent(u -> notificaciones.enviar(u, partido.getId(), TipoEvento.CUPO_COMPLETO,
                "Se liberó una plaza: entraste al partido",
                "Hola " + u.getNombre() + ": se liberó una plaza en el partido del "
                        + partido.getFechaHora().format(FECHA) + " en " + partido.getClubSede()
                        + ". Ya estás inscripto, confirmá tu asistencia."));

        return vista;
    }

    /**
     * Elimina a un participante de la lista de espera.
     */
    @Transactional
    public Vistas.Partido abandonarListaEspera(java.util.UUID partidoId, java.util.UUID usuarioId) {
        Partido partido = partidos.buscarPorId(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Partido no encontrado: " + partidoId));
        Usuario jugador = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (!partido.getListaEspera().removeIf(u -> u.getId().equals(jugador.getId()))) {
            throw new OperacionNoPermitidaException("El jugador no está en la lista de espera");
        }
        partido.getPostulaciones().stream()
                .filter(p -> p.getUsuario().getId().equals(usuarioId))
                .forEach(p -> p.rechazar("Baja voluntaria de la lista de espera"));

        return mapper.partido(partidos.guardar(partido));
    }

    @Transactional(readOnly = true)
    public boolean esParticipante(Partido partido, java.util.UUID usuarioId) {
        return partido.getParticipantes().stream().anyMatch(u -> u.getId().equals(usuarioId))
                && partido.getEstado() != EstadoPartido.CANCELADO;
    }
}
