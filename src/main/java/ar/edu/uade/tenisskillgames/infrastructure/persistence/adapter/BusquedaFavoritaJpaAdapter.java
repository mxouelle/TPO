package ar.edu.uade.tenisskillgames.infrastructure.persistence.adapter;

import ar.edu.uade.tenisskillgames.domain.model.BusquedaFavorita;
import ar.edu.uade.tenisskillgames.domain.port.BusquedaFavoritaRepository;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.BusquedaFavoritaEntity;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.mapper.UsuarioMapper;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.repository.BusquedaFavoritaJpaRepository;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BusquedaFavoritaJpaAdapter implements BusquedaFavoritaRepository {

    private final BusquedaFavoritaJpaRepository repository;
    private final UsuarioJpaRepository usuarios;
    private final UsuarioMapper usuarioMapper;

    public BusquedaFavoritaJpaAdapter(BusquedaFavoritaJpaRepository repository,
                                      UsuarioJpaRepository usuarios,
                                      UsuarioMapper usuarioMapper) {
        this.repository = repository;
        this.usuarios = usuarios;
        this.usuarioMapper = usuarioMapper;
    }

    @Override
    @Transactional
    public BusquedaFavorita guardar(BusquedaFavorita busqueda) {
        BusquedaFavoritaEntity e = repository.findById(busqueda.getId()).orElseGet(BusquedaFavoritaEntity::new);
        e.id = busqueda.getId();
        e.usuarioId = busqueda.getUsuario().getId();
        e.nombre = busqueda.getNombre();
        e.formato = busqueda.getFormato();
        e.superficie = busqueda.getSuperficie();
        e.ntrpMin = busqueda.getNtrpMin();
        e.ntrpMax = busqueda.getNtrpMax();
        e.radioKm = busqueda.getRadioKm();
        e.zona = busqueda.getZona();
        e.activa = busqueda.isActiva();
        repository.save(e);
        return busqueda;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BusquedaFavorita> buscarPorId(UUID id) {
        return repository.findById(id).map(this::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusquedaFavorita> listarActivas() {
        return repository.findByActivaTrue().stream().map(this::aDominio).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusquedaFavorita> listarPorUsuario(UUID usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream().map(this::aDominio).toList();
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        repository.deleteById(id);
    }

    private BusquedaFavorita aDominio(BusquedaFavoritaEntity e) {
        BusquedaFavorita b = new BusquedaFavorita();
        b.setId(e.id);
        b.setUsuario(usuarios.findById(e.usuarioId).map(usuarioMapper::toDomain).orElse(null));
        b.setNombre(e.nombre);
        b.setFormato(e.formato);
        b.setSuperficie(e.superficie);
        b.setNtrpMin(e.ntrpMin);
        b.setNtrpMax(e.ntrpMax);
        b.setRadioKm(e.radioKm);
        b.setZona(e.zona);
        b.setActiva(e.activa);
        return b;
    }
}
