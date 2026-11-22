package ar.edu.uade.tenisskillgames.infrastructure.persistence.repository;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoReporte;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.ReporteConductaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReporteConductaJpaRepository extends JpaRepository<ReporteConductaEntity, UUID> {

    List<ReporteConductaEntity> findByEstado(EstadoReporte estado);

    List<ReporteConductaEntity> findByReportadoIdOrderByFechaReporteDesc(UUID reportadoId);
}
