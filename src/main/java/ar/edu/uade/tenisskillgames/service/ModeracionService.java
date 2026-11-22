package ar.edu.uade.tenisskillgames.service;

import ar.edu.uade.tenisskillgames.controller.dto.Comandos;
import ar.edu.uade.tenisskillgames.controller.dto.Vistas;
import ar.edu.uade.tenisskillgames.controller.mapper.VistaMapper;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoReporte;
import ar.edu.uade.tenisskillgames.domain.enums.TipoEvento;
import ar.edu.uade.tenisskillgames.domain.exception.ConflictoException;
import ar.edu.uade.tenisskillgames.domain.exception.OperacionNoPermitidaException;
import ar.edu.uade.tenisskillgames.domain.exception.RecursoNoEncontradoException;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.ReporteConducta;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.port.PartidoRepository;
import ar.edu.uade.tenisskillgames.domain.port.ResultadoRepository;
import ar.edu.uade.tenisskillgames.domain.port.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso CU11: moderacion de reportes y acumulacion de strikes (RF9).
 */
@Service
public class ModeracionService {

    private final ResultadoRepository resultados;
    private final UsuarioRepository usuarios;
    private final PartidoRepository partidos;
    private final NotificacionService notificaciones;
    private final VistaMapper mapper;

    public ModeracionService(ResultadoRepository resultados, UsuarioRepository usuarios,
                             PartidoRepository partidos, NotificacionService notificaciones,
                             VistaMapper mapper) {
        this.resultados = resultados;
        this.usuarios = usuarios;
        this.partidos = partidos;
        this.notificaciones = notificaciones;
        this.mapper = mapper;
    }

    @Transactional
    public Vistas.ReporteVista reportar(UUID partidoId, UUID reportanteId, Comandos.Reporte cmd) {
        Partido partido = partidos.buscarPorId(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Partido no encontrado: " + partidoId));
        if (partido.getEstado() != EstadoPartido.FINALIZADO) {
            throw new OperacionNoPermitidaException("Solo se puede reportar conducta de un partido finalizado");
        }

        Usuario reportante = usuarios.buscarPorId(reportanteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reportante inexistente"));
        Usuario reportado = usuarios.buscarPorId(cmd.reportadoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Reportado inexistente"));

        if (!partido.esParticipante(reportante) || !partido.esParticipante(reportado)) {
            throw new OperacionNoPermitidaException("Ambos jugadores deben haber participado del partido");
        }

        ReporteConducta reporte = new ReporteConducta(partido, reportante, reportado, cmd.motivo(), cmd.descripcion());
        resultados.guardarReporte(reporte);
        return mapper.reporte(reporte);
    }

    /**
     * Resolucion por parte del administrador: confirmar aplica strike (3 = suspension 7 dias).
     */
    @Transactional
    public Vistas.ReporteVista resolver(UUID reporteId, Comandos.ResolucionReporte cmd) {
        ReporteConducta reporte = resultados.buscarReporte(reporteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reporte no encontrado: " + reporteId));

        if (reporte.getEstado() != EstadoReporte.PENDIENTE) {
            throw new ConflictoException("El reporte ya fue resuelto con estado " + reporte.getEstado());
        }

        String resolucion = cmd.resolucion() == null || cmd.resolucion().isBlank()
                ? (cmd.confirmar() ? "Reporte confirmado por administración" : "Reporte desestimado por falta de evidencia")
                : cmd.resolucion();

        if (!cmd.confirmar()) {
            reporte.desestimar(resolucion);
            resultados.guardarReporte(reporte);
            return mapper.reporte(reporte);
        }

        reporte.confirmar(resolucion);
        resultados.guardarReporte(reporte);

        Usuario reportado = usuarios.buscarPorId(reporte.getReportado().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Reportado inexistente"));

        if (reporte.getMotivo() == ar.edu.uade.tenisskillgames.domain.enums.MotivoReporte.NO_SHOW) {
            reportado.registrarAusencia();
        }
        reportado.aplicarStrike();
        usuarios.guardar(reportado);

        notificaciones.enviar(reportado, reporte.getPartido() == null ? null : reporte.getPartido().getId(),
                TipoEvento.REPORTE_CONFIRMADO,
                "Se registró un strike en tu cuenta",
                "Se confirmó un reporte de conducta (" + reporte.getMotivo() + ") en tu contra. "
                        + "Strikes acumulados: " + reportado.getStrikes() + ". "
                        + (reportado.estaSuspendido()
                        ? "Tu cuenta queda suspendida hasta " + reportado.getRehabilitacion() + "."
                        : "Con 3 strikes la cuenta se suspende por 7 días."));

        return mapper.reporte(reporte);
    }

    @Transactional(readOnly = true)
    public List<Vistas.ReporteVista> pendientes() {
        return resultados.reportesPendientes().stream().map(mapper::reporte).toList();
    }

    @Transactional(readOnly = true)
    public List<Vistas.ReporteVista> deUsuario(UUID usuarioId) {
        return resultados.reportesDe(usuarioId).stream().map(mapper::reporte).toList();
    }

    /**
     * Registro automatico de no-show cuando el partido ya finalizo (RF9).
     */
    @Transactional
    public void registrarNoShowAutomatico(UUID partidoId, UUID usuarioId) {
        Partido partido = partidos.buscarPorId(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Partido no encontrado: " + partidoId));
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario inexistente"));
        usuario.registrarAusencia();
        usuarios.guardar(usuario);
        notificaciones.enviar(usuario, partido.getId(), TipoEvento.REPORTE_CONFIRMADO,
                "Ausencia registrada",
                "Se registró una ausencia injustificada en el partido del " + partido.getFechaHora() + ".");
    }
}
