package ar.edu.uade.tenisskillgames.infrastructure.persistence.repository;

import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.EvaluacionJugadorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EvaluacionJugadorJpaRepository extends JpaRepository<EvaluacionJugadorEntity, UUID> {

    List<EvaluacionJugadorEntity> findByEvaluadoId(UUID evaluadoId);

    boolean existsByPartidoIdAndEvaluadorIdAndEvaluadoId(UUID partidoId, UUID evaluadorId, UUID evaluadoId);

    @Query("select avg(e.fairPlay) from EvaluacionJugadorEntity e where e.evaluadoId = :usuarioId")
    Double promedioFairPlay(@Param("usuarioId") UUID usuarioId);

    @Query("select avg(e.puntualidad) from EvaluacionJugadorEntity e where e.evaluadoId = :usuarioId")
    Double promedioPuntualidad(@Param("usuarioId") UUID usuarioId);

    @Query("select count(e) from EvaluacionJugadorEntity e where e.evaluadoId = :usuarioId")
    long contarPorEvaluado(@Param("usuarioId") UUID usuarioId);
}
