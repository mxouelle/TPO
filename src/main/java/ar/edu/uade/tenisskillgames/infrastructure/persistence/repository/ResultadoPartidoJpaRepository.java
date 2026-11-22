package ar.edu.uade.tenisskillgames.infrastructure.persistence.repository;

import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.ResultadoPartidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResultadoPartidoJpaRepository extends JpaRepository<ResultadoPartidoEntity, UUID> {

    Optional<ResultadoPartidoEntity> findByPartidoId(UUID partidoId);
}
