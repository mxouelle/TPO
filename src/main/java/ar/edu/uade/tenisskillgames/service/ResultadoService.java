package ar.edu.uade.tenisskillgames.service;

import ar.edu.uade.tenisskillgames.controller.dto.Comandos;
import ar.edu.uade.tenisskillgames.controller.dto.Vistas;
import ar.edu.uade.tenisskillgames.controller.mapper.VistaMapper;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.event.ResultadoCargadoEvent;
import ar.edu.uade.tenisskillgames.domain.exception.ConflictoException;
import ar.edu.uade.tenisskillgames.domain.exception.OperacionNoPermitidaException;
import ar.edu.uade.tenisskillgames.domain.exception.RecursoNoEncontradoException;
import ar.edu.uade.tenisskillgames.domain.model.EvaluacionJugador;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.ResultadoPartido;
import ar.edu.uade.tenisskillgames.domain.model.SetMarcador;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.port.PartidoRepository;
import ar.edu.uade.tenisskillgames.domain.port.ResultadoRepository;
import ar.edu.uade.tenisskillgames.domain.port.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso CU8: carga de marcador por sets y evaluacion de fair play (RF8).
 */
@Service
public class ResultadoService {

    private final PartidoRepository partidos;
    private final UsuarioRepository usuarios;
    private final ResultadoRepository resultados;
    private final VistaMapper mapper;

    public ResultadoService(PartidoRepository partidos, UsuarioRepository usuarios,
                            ResultadoRepository resultados, VistaMapper mapper) {
        this.partidos = partidos;
        this.usuarios = usuarios;
        this.resultados = resultados;
        this.mapper = mapper;
    }

    @Transactional
    public Vistas.Partido cargarResultado(UUID partidoId, UUID usuarioId, Comandos.Resultado cmd) {
        Partido partido = partidos.buscarPorId(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Partido no encontrado: " + partidoId));

        boolean autorizado = partido.esParticipante(usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado")))
                || (partido.getOrganizador() != null && partido.getOrganizador().getId().equals(usuarioId));
        if (!autorizado) {
            throw new OperacionNoPermitidaException("Solo los participantes o el organizador pueden cargar el resultado");
        }
        if (partido.getEstado() != EstadoPartido.FINALIZADO) {
            throw new OperacionNoPermitidaException("Solo se puede cargar el resultado de un partido finalizado");
        }

        List<SetMarcador> sets = cmd.sets().stream()
                .map(s -> new SetMarcador(s.local(), s.visitante()))
                .toList();

        ResultadoPartido resultado = new ResultadoPartido(partido, cmd.ganadorLado(), sets,
                cmd.duracionRealMinutos(), cmd.observaciones());
        resultado.validar();

        partido.cargarResultado(resultado);
        Vistas.Partido vista = mapper.partido(partidos.guardar(partido));
        partido.publicarEvento(new ResultadoCargadoEvent(partido.getId()));
        return vista;
    }

    @Transactional(readOnly = true)
    public Vistas.Partido obtenerResultado(UUID partidoId) {
        return mapper.partido(partidos.buscarPorId(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Partido no encontrado: " + partidoId)));
    }

    /**
     * Evaluacion de deportividad y puntualidad entre rivales (1 a 5 estrellas).
     */
    @Transactional
    public Vistas.EvaluacionVista evaluar(UUID partidoId, UUID evaluadorId, Comandos.Evaluacion cmd) {
        Partido partido = partidos.buscarPorId(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Partido no encontrado: " + partidoId));
        if (partido.getEstado() != EstadoPartido.FINALIZADO) {
            throw new OperacionNoPermitidaException("Solo se puede evaluar un partido finalizado");
        }

        Usuario evaluador = usuarios.buscarPorId(evaluadorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evaluador inexistente"));
        Usuario evaluado = usuarios.buscarPorId(cmd.evaluadoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Evaluado inexistente"));

        if (!partido.esParticipante(evaluador) || !partido.esParticipante(evaluado)) {
            throw new OperacionNoPermitidaException("Ambos jugadores deben haber participado del partido");
        }
        if (resultados.existeEvaluacion(partidoId, evaluadorId, cmd.evaluadoId())) {
            throw new ConflictoException("Ya evaluaste a este jugador en este partido");
        }

        EvaluacionJugador evaluacion = new EvaluacionJugador(partido, evaluador, evaluado,
                cmd.fairPlay(), cmd.puntualidad(), cmd.comentario());
        resultados.guardarEvaluacion(evaluacion);

        evaluado.registrarPartidoJugado();
        Double fairPlay = resultados.promedioFairPlay(evaluado.getId());
        Double puntualidad = resultados.promedioPuntualidad(evaluado.getId());
        long cantidad = resultados.cantidadEvaluaciones(evaluado.getId());
        if (fairPlay != null && puntualidad != null) {
            evaluado.actualizarReputacion((int) cantidad, fairPlay, puntualidad);
        }
        usuarios.guardar(evaluado);

        return mapper.evaluacion(evaluacion);
    }

    @Transactional(readOnly = true)
    public List<Vistas.EvaluacionVista> evaluacionesDe(UUID usuarioId) {
        return resultados.evaluacionesDe(usuarioId).stream().map(mapper::evaluacion).toList();
    }
}
