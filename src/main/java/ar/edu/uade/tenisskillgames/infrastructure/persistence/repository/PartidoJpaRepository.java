package ar.edu.uade.tenisskillgames.infrastructure.persistence.repository;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.PartidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PartidoJpaRepository extends JpaRepository<PartidoEntity, UUID>,
        JpaSpecificationExecutor<PartidoEntity> {

    List<PartidoEntity> findByEstadoActual(EstadoPartido estado);

    @Query("""
            select p from PartidoEntity p
            where p.estadoActual = :estado
              and p.fechaHora <= :referencia
            order by p.fechaHora
            """)
    List<PartidoEntity> buscarPorEstadoEInicioHasta(@Param("estado") EstadoPartido estado,
                                                    @Param("referencia") LocalDateTime referencia);

    @Query("""
            select p from PartidoEntity p
            where p.id in (
                select po.partidoId from PostulacionEntity po
                where po.usuarioId = :usuarioId and po.estado = ar.edu.uade.tenisskillgames.domain.enums.EstadoPostulacion.ACEPTADA
            )
            order by p.fechaHora desc
            """)
    List<PartidoEntity> buscarPorParticipante(@Param("usuarioId") UUID usuarioId);

    List<PartidoEntity> findByOrganizadorIdOrderByFechaHoraDesc(UUID organizadorId);
}
