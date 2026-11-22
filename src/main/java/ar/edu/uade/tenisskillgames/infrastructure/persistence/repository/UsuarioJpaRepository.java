package ar.edu.uade.tenisskillgames.infrastructure.persistence.repository;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoVerificacion;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.UsuarioEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {

    Optional<UsuarioEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<UsuarioEntity> findByActivoTrueAndEstadoVerificacion(EstadoVerificacion estado);

    @Query("""
            select u from UsuarioEntity u
            where u.activo = true
              and u.estadoVerificacion = :estado
              and u.id not in (
                    select p.usuarioId from PostulacionEntity p where p.partidoId = :partidoId
              )
            order by u.nivelNtrp
            """)
    List<UsuarioEntity> buscarCandidatos(@Param("partidoId") UUID partidoId,
                                         @Param("estado") EstadoVerificacion estado,
                                         Pageable pageable);
}
