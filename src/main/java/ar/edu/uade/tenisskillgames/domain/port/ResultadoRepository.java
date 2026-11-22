package ar.edu.uade.tenisskillgames.domain.port;

import ar.edu.uade.tenisskillgames.domain.model.EvaluacionJugador;
import ar.edu.uade.tenisskillgames.domain.model.ReporteConducta;
import ar.edu.uade.tenisskillgames.domain.model.ResultadoPartido;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de resultados, evaluaciones y reportes de conducta.
 */
public interface ResultadoRepository {

    ResultadoPartido guardar(ResultadoPartido resultado);

    Optional<ResultadoPartido> buscarPorPartido(UUID partidoId);

    EvaluacionJugador guardarEvaluacion(EvaluacionJugador evaluacion);

    List<EvaluacionJugador> evaluacionesDe(UUID usuarioId);

    boolean existeEvaluacion(UUID partidoId, UUID evaluadorId, UUID evaluadoId);

    ReporteConducta guardarReporte(ReporteConducta reporte);

    Optional<ReporteConducta> buscarReporte(UUID id);

    List<ReporteConducta> reportesPendientes();

    List<ReporteConducta> reportesDe(UUID usuarioId);

    Double promedioFairPlay(UUID usuarioId);

    Double promedioPuntualidad(UUID usuarioId);

    long cantidadEvaluaciones(UUID usuarioId);
}
