package ar.edu.uade.tenisskillgames.service;

import ar.edu.uade.tenisskillgames.controller.dto.Vistas;
import ar.edu.uade.tenisskillgames.controller.mapper.VistaMapper;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.enums.TipoEvento;
import ar.edu.uade.tenisskillgames.domain.exception.OperacionNoPermitidaException;
import ar.edu.uade.tenisskillgames.domain.exception.RecursoNoEncontradoException;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.port.PartidoRepository;
import ar.edu.uade.tenisskillgames.domain.port.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso CU6: confirmacion de asistencia. Cuando el 100% confirma, el
 * estado pasa a CONFIRMADO y se habilita la exportacion iCal (CA3).
 */
@Service
public class ConfirmacionService {

    private static final int HORAS_MINIMAS_ANTICIPACION = 2;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PartidoRepository partidos;
    private final UsuarioRepository usuarios;
    private final NotificacionService notificaciones;
    private final VistaMapper mapper;

    public ConfirmacionService(PartidoRepository partidos, UsuarioRepository usuarios,
                               NotificacionService notificaciones, VistaMapper mapper) {
        this.partidos = partidos;
        this.usuarios = usuarios;
        this.notificaciones = notificaciones;
        this.mapper = mapper;
    }

    @Transactional
    public Vistas.Partido registrar(UUID partidoId, UUID usuarioId, boolean confirmado, String motivo) {
        Partido partido = partidos.buscarPorId(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Partido no encontrado: " + partidoId));
        Usuario jugador = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (!partido.esParticipante(jugador)) {
            throw new OperacionNoPermitidaException("El jugador no participa de este partido");
        }

        if (!confirmado) {
            return rechazar(partido, jugador, motivo);
        }

        if (partido.getEstado() != EstadoPartido.CUPO_COMPLETO && partido.getEstado() != EstadoPartido.CONFIRMADO) {
            throw new OperacionNoPermitidaException("El partido está " + partido.getEstado()
                    + ": todavía no está habilitada la confirmación");
        }
        LocalDateTime limite = partido.getFechaHora().minusHours(HORAS_MINIMAS_ANTICIPACION);
        if (LocalDateTime.now().isAfter(limite)) {
            throw new OperacionNoPermitidaException(
                    "La confirmación debe realizarse al menos " + HORAS_MINIMAS_ANTICIPACION
                            + " horas antes del inicio del turno");
        }

        partido.confirmar(jugador);
        Vistas.Partido vista = mapper.partido(partidos.guardar(partido));

        notificaciones.enviar(jugador, partido.getId(), TipoEvento.CUPO_COMPLETO,
                "Confirmación registrada",
                "Registramos tu asistencia al partido del " + partido.getFechaHora().format(FECHA)
                        + " en " + partido.getClubSede() + ".");

        return vista;
    }

    private Vistas.Partido rechazar(Partido partido, Usuario jugador, String motivo) {
        if (partido.getEstado().esEstadoFinal()) {
            throw new OperacionNoPermitidaException("El partido está " + partido.getEstado());
        }
        partido.liberarPlaza(jugador);
        Optional<Usuario> promovido = partido.promoverListaEspera();
        Vistas.Partido vista = mapper.partido(partidos.guardar(partido));

        String motivoTexto = motivo == null || motivo.isBlank() ? "sin motivo informado" : motivo;
        notificaciones.enviar(partido.getOrganizador(), partido.getId(), TipoEvento.CUPO_COMPLETO,
                "Un jugador rechazó la participación",
                jugador.getNombre() + " rechazó su participación en el partido del "
                        + partido.getFechaHora().format(FECHA) + " (" + motivoTexto + "). "
                        + "Se liberó la plaza y se convocó a la lista de espera.");

        promovido.ifPresent(u -> notificaciones.enviar(u, partido.getId(), TipoEvento.CUPO_COMPLETO,
                "Entraste al partido por lista de espera",
                "Hola " + u.getNombre() + ": se liberó una plaza y quedaste inscripto en el partido del "
                        + partido.getFechaHora().format(FECHA) + " en " + partido.getClubSede() + "."));

        return vista;
    }
}
