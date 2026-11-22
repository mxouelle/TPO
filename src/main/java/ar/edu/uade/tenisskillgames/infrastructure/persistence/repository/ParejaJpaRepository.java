package ar.edu.uade.tenisskillgames.infrastructure.persistence.repository;

import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.ParejaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ParejaJpaRepository extends JpaRepository<ParejaEntity, UUID> {

    List<ParejaEntity> findByPartidoId(UUID partidoId);

    void deleteByPartidoId(UUID partidoId);
}
