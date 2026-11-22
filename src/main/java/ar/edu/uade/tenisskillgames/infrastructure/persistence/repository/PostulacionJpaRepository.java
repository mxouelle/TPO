package ar.edu.uade.tenisskillgames.infrastructure.persistence.repository;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoPostulacion;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.PostulacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostulacionJpaRepository extends JpaRepository<PostulacionEntity, UUID> {

    List<PostulacionEntity> findByPartidoIdOrderByFechaPostulacionAsc(UUID partidoId);

    List<PostulacionEntity> findByPartidoIdAndEstadoOrderByFechaPostulacionAsc(UUID partidoId, EstadoPostulacion estado);

    Optional<PostulacionEntity> findByPartidoIdAndUsuarioId(UUID partidoId, UUID usuarioId);

    void deleteByPartidoId(UUID partidoId);
}
