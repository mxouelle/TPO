package ar.edu.uade.tenisskillgames.infrastructure.persistence.adapter;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoReporte;
import ar.edu.uade.tenisskillgames.domain.model.EvaluacionJugador;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.ReporteConducta;
import ar.edu.uade.tenisskillgames.domain.model.ResultadoPartido;
import ar.edu.uade.tenisskillgames.domain.model.SetMarcador;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.port.ResultadoRepository;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.EvaluacionJugadorEntity;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.PartidoEntity;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.ReporteConductaEntity;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.ResultadoPartidoEntity;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.mapper.PartidoMapper;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.mapper.UsuarioMapper;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.repository.EvaluacionJugadorJpaRepository;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.repository.PartidoJpaRepository;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.repository.ReporteConductaJpaRepository;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.repository.ResultadoPartidoJpaRepository;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ResultadoJpaAdapter implements ResultadoRepository {

    private final ResultadoPartidoJpaRepository resultados;
    private final EvaluacionJugadorJpaRepository evaluaciones;
    private final ReporteConductaJpaRepository reportes;
    private final UsuarioJpaRepository usuarios;
    private final PartidoJpaRepository partidos;
    private final UsuarioMapper usuarioMapper;
    private final PartidoMapper partidoMapper;

    public ResultadoJpaAdapter(ResultadoPartidoJpaRepository resultados,
                               EvaluacionJugadorJpaRepository evaluaciones,
                               ReporteConductaJpaRepository reportes,
                               UsuarioJpaRepository usuarios,
                               PartidoJpaRepository partidos,
                               UsuarioMapper usuarioMapper,
                               PartidoMapper partidoMapper) {
        this.resultados = resultados;
        this.evaluaciones = evaluaciones;
        this.reportes = reportes;
        this.usuarios = usuarios;
        this.partidos = partidos;
        this.usuarioMapper = usuarioMapper;
        this.partidoMapper = partidoMapper;
    }

    // ------------------------------------------------------------------ resultados

    @Override
    @Transactional
    public ResultadoPartido guardar(ResultadoPartido resultado) {
        ResultadoPartidoEntity e = resultados.findById(resultado.getId()).orElseGet(ResultadoPartidoEntity::new);
        e.id = resultado.getId();
        e.partidoId = resultado.getPartido().getId();
        e.ganadorLado = resultado.getGanadorLado();
        e.marcadorSets = resultado.getMarcadorComoTexto();
        e.duracionMin = resultado.getDuracionRealMinutos();
        e.observaciones = resultado.getObservaciones();
        e.validado = resultado.isValidado();
        e.fechaCarga = resultado.getFechaCarga();
        resultados.save(e);
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResultadoPartido> buscarPorPartido(UUID partidoId) {
        return resultados.findByPartidoId(partidoId).map(e -> {
            ResultadoPartido r = new ResultadoPartido();
            r.setId(e.id);
            r.setPartido(partidoStub(e.partidoId));
            r.setGanadorLado(e.ganadorLado);
            for (SetMarcador set : partidoMapper.parseSets(e.marcadorSets)) {
                r.getMarcadorSets().add(set);
            }
            r.setDuracionRealMinutos(e.duracionMin);
            r.setObservaciones(e.observaciones);
            r.setValidado(e.validado);
            r.setFechaCarga(e.fechaCarga);
            return r;
        });
    }

    // ------------------------------------------------------------------ evaluaciones

    @Override
    @Transactional
    public EvaluacionJugador guardarEvaluacion(EvaluacionJugador evaluacion) {
        EvaluacionJugadorEntity e = new EvaluacionJugadorEntity();
        e.id = evaluacion.getId();
        e.partidoId = evaluacion.getPartido().getId();
        e.evaluadorId = evaluacion.getEvaluador().getId();
        e.evaluadoId = evaluacion.getEvaluado().getId();
        e.fairPlay = evaluacion.getPuntajeFairPlay();
        e.puntualidad = evaluacion.getPuntajePuntualidad();
        e.comentario = evaluacion.getComentario();
        e.moderacion = evaluacion.getEstadoModeracion();
        e.fechaEvaluacion = evaluacion.getFechaEvaluacion();
        evaluaciones.save(e);
        return evaluacion;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluacionJugador> evaluacionesDe(UUID usuarioId) {
        return evaluaciones.findByEvaluadoId(usuarioId).stream().map(this::aEvaluacion).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeEvaluacion(UUID partidoId, UUID evaluadorId, UUID evaluadoId) {
        return evaluaciones.existsByPartidoIdAndEvaluadorIdAndEvaluadoId(partidoId, evaluadorId, evaluadoId);
    }

    public Double promedioFairPlay(UUID usuarioId) {
        return evaluaciones.promedioFairPlay(usuarioId);
    }

    public Double promedioPuntualidad(UUID usuarioId) {
        return evaluaciones.promedioPuntualidad(usuarioId);
    }

    public long cantidadEvaluaciones(UUID usuarioId) {
        return evaluaciones.contarPorEvaluado(usuarioId);
    }

    private EvaluacionJugador aEvaluacion(EvaluacionJugadorEntity e) {
        EvaluacionJugador evaluacion = new EvaluacionJugador();
        evaluacion.setId(e.id);
        evaluacion.setPartido(partidoStub(e.partidoId));
        evaluacion.setEvaluador(usuarioStub(e.evaluadorId));
        evaluacion.setEvaluado(usuarioStub(e.evaluadoId));
        evaluacion.setPuntajeFairPlay(e.fairPlay);
        evaluacion.setPuntajePuntualidad(e.puntualidad);
        evaluacion.setComentario(e.comentario);
        evaluacion.setEstadoModeracion(e.moderacion);
        evaluacion.setFechaEvaluacion(e.fechaEvaluacion);
        return evaluacion;
    }

    // ------------------------------------------------------------------ reportes

    @Override
    @Transactional
    public ReporteConducta guardarReporte(ReporteConducta reporte) {
        ReporteConductaEntity e = new ReporteConductaEntity();
        e.id = reporte.getId();
        e.partidoId = reporte.getPartido() == null ? null : reporte.getPartido().getId();
        e.reportanteId = reporte.getReportante().getId();
        e.reportadoId = reporte.getReportado().getId();
        e.motivo = reporte.getMotivo();
        e.descripcion = reporte.getDescripcion();
        e.estado = reporte.getEstado();
        e.resolucion = reporte.getResolucion();
        e.fechaReporte = reporte.getFechaReporte();
        reportes.save(e);
        return reporte;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReporteConducta> buscarReporte(UUID id) {
        return reportes.findById(id).map(this::aReporte);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteConducta> reportesPendientes() {
        return reportes.findByEstado(EstadoReporte.PENDIENTE).stream().map(this::aReporte).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteConducta> reportesDe(UUID usuarioId) {
        return reportes.findByReportadoIdOrderByFechaReporteDesc(usuarioId).stream().map(this::aReporte).toList();
    }

    private ReporteConducta aReporte(ReporteConductaEntity e) {
        ReporteConducta r = new ReporteConducta();
        r.setId(e.id);
        r.setPartido(e.partidoId == null ? null : partidoStub(e.partidoId));
        r.setReportante(usuarioStub(e.reportanteId));
        r.setReportado(usuarioStub(e.reportadoId));
        r.setMotivo(e.motivo);
        r.setDescripcion(e.descripcion);
        r.setEstado(e.estado);
        r.setResolucion(e.resolucion);
        r.setFechaReporte(e.fechaReporte);
        return r;
    }

    // ------------------------------------------------------------------ helpers

    private Usuario usuarioStub(UUID id) {
        return usuarios.findById(id).map(usuarioMapper::toDomain).orElse(null);
    }

    private Partido partidoStub(UUID id) {
        Partido partido = new Partido();
        partido.setId(id);
        partidos.findById(id).ifPresent((PartidoEntity e) -> {
            partido.setClubSede(e.clubSede);
            partido.setFechaHora(e.fechaHora);
            partido.setFormato(e.formato);
            partido.setSuperficie(e.superficie);
            partido.setEstado(e.estadoActual);
        });
        return partido;
    }
}
