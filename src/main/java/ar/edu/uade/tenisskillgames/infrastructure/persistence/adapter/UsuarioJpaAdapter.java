package ar.edu.uade.tenisskillgames.infrastructure.persistence.adapter;

import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.port.UsuarioRepository;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoVerificacion;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.UsuarioEntity;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.mapper.UsuarioMapper;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UsuarioJpaAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository repository;
    private final UsuarioMapper mapper;

    public UsuarioJpaAdapter(UsuarioJpaRepository repository, UsuarioMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Usuario guardar(Usuario usuario) {
        UsuarioEntity entity = repository.findById(usuario.getId())
                .orElseGet(() -> mapper.toEntity(usuario));
        mapper.copy(usuario, entity);
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return repository.findByEmailIgnoreCase(email).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> buscarCandidatos(UUID partidoId, int limite) {
        return repository.buscarCandidatos(partidoId, EstadoVerificacion.VERIFICADO, PageRequest.of(0, limite))
                .stream().map(mapper::toDomain).toList();
    }
}
