package ar.edu.uade.tenisskillgames.infrastructure.persistence.adapter;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoEnvio;
import ar.edu.uade.tenisskillgames.domain.model.Notificacion;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.port.NotificacionRepository;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.NotificacionEntity;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.mapper.UsuarioMapper;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.repository.NotificacionJpaRepository;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
public class NotificacionJpaAdapter implements NotificacionRepository {

    private final NotificacionJpaRepository repository;
    private final UsuarioJpaRepository usuarios;
    private final UsuarioMapper usuarioMapper;

    public NotificacionJpaAdapter(NotificacionJpaRepository repository,
                                  UsuarioJpaRepository usuarios,
                                  UsuarioMapper usuarioMapper) {
        this.repository = repository;
        this.usuarios = usuarios;
        this.usuarioMapper = usuarioMapper;
    }

    @Override
    @Transactional
    public Notificacion guardar(Notificacion n) {
        NotificacionEntity e = repository.findById(n.getId()).orElseGet(NotificacionEntity::new);
        e.id = n.getId();
        e.usuarioId = n.getDestinatario() == null ? null : n.getDestinatario().getId();
        e.partidoId = n.getPartidoId();
        e.tipoEvento = n.getTipoEvento();
        e.canal = n.getCanal();
        e.asunto = n.getAsunto();
        e.contenido = n.getContenido();
        e.estadoEnvio = n.getEstadoEnvio();
        e.intentos = n.getIntentos();
        e.ultimoError = truncar(n.getUltimoError(), 300);
        e.fechaCreacion = n.getFechaCreacion();
        e.fechaEnvio = n.getFechaEnvio();
        repository.save(e);
        return n;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> listarPorUsuario(UUID usuarioId) {
        return mapear(repository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> listarPorPartido(UUID partidoId) {
        return mapear(repository.findByPartidoId(partidoId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> listarUltimas(int limite) {
        return mapear(repository.findAllByOrderByFechaCreacionDesc(PageRequest.of(0, limite)));
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorEstado(EstadoEnvio estado) {
        return repository.countByEstadoEnvio(estado);
    }

    private List<Notificacion> mapear(List<NotificacionEntity> entidades) {
        Map<UUID, Usuario> cache = new HashMap<>();
        return entidades.stream().map(e -> {
            Notificacion n = new Notificacion();
            n.setId(e.id);
            if (e.usuarioId != null) {
                Usuario u = cache.computeIfAbsent(e.usuarioId,
                        id -> usuarios.findById(id).map(usuarioMapper::toDomain).orElse(null));
                n.setDestinatario(u);
            }
            n.setPartidoId(e.partidoId);
            n.setTipoEvento(e.tipoEvento);
            n.setCanal(e.canal);
            n.setAsunto(e.asunto);
            n.setContenido(e.contenido);
            n.setEstadoEnvio(e.estadoEnvio);
            n.setIntentos(e.intentos);
            n.setUltimoError(e.ultimoError);
            n.setFechaCreacion(e.fechaCreacion);
            n.setFechaEnvio(e.fechaEnvio);
            return n;
        }).filter(Objects::nonNull).toList();
    }

    private String truncar(String valor, int maximo) {
        if (valor == null || valor.length() <= maximo) {
            return valor;
        }
        return valor.substring(0, maximo);
    }
}
