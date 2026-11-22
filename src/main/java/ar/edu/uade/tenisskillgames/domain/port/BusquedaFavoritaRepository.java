package ar.edu.uade.tenisskillgames.domain.port;

import ar.edu.uade.tenisskillgames.domain.model.BusquedaFavorita;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusquedaFavoritaRepository {

    BusquedaFavorita guardar(BusquedaFavorita busqueda);

    Optional<BusquedaFavorita> buscarPorId(UUID id);

    List<BusquedaFavorita> listarActivas();

    List<BusquedaFavorita> listarPorUsuario(UUID usuarioId);

    void eliminar(UUID id);
}
