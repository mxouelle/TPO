package ar.edu.uade.tenisskillgames.infrastructure.persistence.repository;

import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.TransicionEstadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransicionEstadoJpaRepository extends JpaRepository<TransicionEstadoEntity, UUID> {

    List<TransicionEstadoEntity> findByPartidoIdOrderByFechaAsc(UUID partidoId);

    void deleteByPartidoId(UUID partidoId);
}
