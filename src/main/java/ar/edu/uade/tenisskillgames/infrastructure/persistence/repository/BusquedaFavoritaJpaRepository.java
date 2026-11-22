package ar.edu.uade.tenisskillgames.infrastructure.persistence.repository;

import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.BusquedaFavoritaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BusquedaFavoritaJpaRepository extends JpaRepository<BusquedaFavoritaEntity, UUID> {

    List<BusquedaFavoritaEntity> findByActivaTrue();

    List<BusquedaFavoritaEntity> findByUsuarioId(UUID usuarioId);
}
