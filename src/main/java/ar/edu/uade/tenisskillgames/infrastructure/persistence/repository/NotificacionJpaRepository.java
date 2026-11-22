package ar.edu.uade.tenisskillgames.infrastructure.persistence.repository;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoEnvio;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.NotificacionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificacionJpaRepository extends JpaRepository<NotificacionEntity, UUID> {

    List<NotificacionEntity> findByUsuarioIdOrderByFechaCreacionDesc(UUID usuarioId);

    List<NotificacionEntity> findByPartidoId(UUID partidoId);

    List<NotificacionEntity> findAllByOrderByFechaCreacionDesc(Pageable pageable);

    long countByEstadoEnvio(EstadoEnvio estado);
}
