package ar.edu.uade.tenisskillgames.infrastructure.persistence.adapter;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoPostulacion;
import ar.edu.uade.tenisskillgames.domain.event.DomainEventPublisher;
import ar.edu.uade.tenisskillgames.domain.model.Confirmacion;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Pareja;
import ar.edu.uade.tenisskillgames.domain.model.Postulacion;
import ar.edu.uade.tenisskillgames.domain.model.ResultadoPartido;
import ar.edu.uade.tenisskillgames.domain.model.SetMarcador;
import ar.edu.uade.tenisskillgames.domain.model.TransicionEstado;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.port.FiltroPartido;
import ar.edu.uade.tenisskillgames.domain.port.PartidoRepository;
import ar.edu.uade.tenisskillgames.domain.vo.UbicacionGeo;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.ParejaEntity;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.PartidoEntity;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.PostulacionEntity;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.ResultadoPartidoEntity;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.TransicionEstadoEntity;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.mapper.PartidoMapper;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.mapper.UsuarioMapper;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.repository.ParejaJpaRepository;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.repository.PartidoJpaRepository;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.repository.PostulacionJpaRepository;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.repository.ResultadoPartidoJpaRepository;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.repository.TransicionEstadoJpaRepository;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.repository.UsuarioJpaRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class PartidoJpaAdapter implements PartidoRepository {

    private final PartidoJpaRepository partidos;
    private final PostulacionJpaRepository postulaciones;
    private final ParejaJpaRepository parejas;
    private final TransicionEstadoJpaRepository transiciones;
    private final ResultadoPartidoJpaRepository resultados;
    private final UsuarioJpaRepository usuarios;
    private final UsuarioMapper usuarioMapper;
    private final PartidoMapper partidoMapper;
    private final DomainEventPublisher publicador;

    public PartidoJpaAdapter(PartidoJpaRepository partidos,
                             PostulacionJpaRepository postulaciones,
                             ParejaJpaRepository parejas,
                             TransicionEstadoJpaRepository transiciones,
                             ResultadoPartidoJpaRepository resultados,
                             UsuarioJpaRepository usuarios,
                             UsuarioMapper usuarioMapper,
                             PartidoMapper partidoMapper,
                             @org.springframework.beans.factory.annotation.Qualifier("publicadorEventosTransaccional")
                             DomainEventPublisher publicador) {
        this.partidos = partidos;
        this.postulaciones = postulaciones;
        this.parejas = parejas;
        this.transiciones = transiciones;
        this.resultados = resultados;
        this.usuarios = usuarios;
        this.usuarioMapper = usuarioMapper;
        this.partidoMapper = partidoMapper;
        this.publicador = publicador;
    }

    // ------------------------------------------------------------------ escritura

    @Override
    @Transactional
    public Partido guardar(Partido partido) {
        UUID id = partido.getId();
        PartidoEntity entity = partidos.findById(id).orElseGet(() -> partidoMapper.toEntity(partido));
        partidoMapper.copy(partido, entity);
        partidos.save(entity);

        postulaciones.deleteByPartidoId(id);
        postulaciones.flush();
        postulaciones.saveAll(aPostulaciones(partido));

        parejas.deleteByPartidoId(id);
        parejas.flush();
        parejas.saveAll(aParejas(partido));

        transiciones.deleteByPartidoId(id);
        transiciones.flush();
        transiciones.saveAll(aTransiciones(partido));

        if (partido.getResultado() != null) {
            resultados.findByPartidoId(id)
                    .map(existente -> {
                        mapearResultado(partido.getResultado(), existente);
                        return resultados.save(existente);
                    })
                    .orElseGet(() -> resultados.save(mapearResultado(partido.getResultado(), new ResultadoPartidoEntity())));
        }

        partido.setEventPublisher(publicador);
        return partido;
    }

    private List<PostulacionEntity> aPostulaciones(Partido partido) {
        Map<UUID, Boolean> confirmados = new HashMap<>();
        Map<UUID, LocalDateTime> fechasConfirmacion = new HashMap<>();
        for (Confirmacion c : partido.getConfirmaciones()) {
            confirmados.put(c.getUsuario().getId(), c.isConfirmado());
            fechasConfirmacion.put(c.getUsuario().getId(), c.getFechaConfirmacion());
        }

        List<PostulacionEntity> lista = new ArrayList<>();
        Set<UUID> agregados = new HashSet<>();
        for (Postulacion p : partido.getPostulaciones()) {
            PostulacionEntity e = new PostulacionEntity();
            e.id = p.getId();
            e.usuarioId = p.getUsuario().getId();
            e.partidoId = partido.getId();
            e.ladoAsignado = p.getLadoDeseado();
            e.estado = p.getEstado();
            e.fechaPostulacion = p.getFechaPostulacion();
            e.motivoRechazo = p.getMotivoRechazo();
            e.confirmado = confirmados.get(e.usuarioId);
            e.fechaConfirmacion = fechasConfirmacion.get(e.usuarioId);
            lista.add(e);
            agregados.add(e.usuarioId);
        }
        // Participantes que no tuvieran postulacion persistida (por ejemplo, creador auto-inscripto)
        for (Usuario u : partido.getParticipantes()) {
            if (agregados.add(u.getId())) {
                PostulacionEntity e = new PostulacionEntity();
                e.id = UUID.randomUUID();
                e.usuarioId = u.getId();
                e.partidoId = partido.getId();
                e.estado = EstadoPostulacion.ACEPTADA;
                e.fechaPostulacion = partido.getFechaCreacion();
                e.confirmado = confirmados.get(u.getId());
                e.fechaConfirmacion = fechasConfirmacion.get(u.getId());
                lista.add(e);
            }
        }
        return lista;
    }

    private List<ParejaEntity> aParejas(Partido partido) {
        List<ParejaEntity> lista = new ArrayList<>();
        for (Pareja p : partido.getParejas()) {
            ParejaEntity e = new ParejaEntity();
            e.id = p.getId();
            e.partidoId = partido.getId();
            e.lado = p.getLado();
            p.getJugadores().forEach(j -> e.jugadores.add(j.getId()));
            lista.add(e);
        }
        return lista;
    }

    private List<TransicionEstadoEntity> aTransiciones(Partido partido) {
        List<TransicionEstadoEntity> lista = new ArrayList<>();
        for (TransicionEstado t : partido.getHistorialEstados()) {
            TransicionEstadoEntity e = new TransicionEstadoEntity();
            e.id = UUID.randomUUID();
            e.partidoId = partido.getId();
            e.estadoAnterior = t.estadoAnterior();
            e.estadoNuevo = t.estadoNuevo();
            e.fecha = t.fecha();
            e.motivo = t.motivo();
            lista.add(e);
        }
        return lista;
    }

    private ResultadoPartidoEntity mapearResultado(ResultadoPartido r, ResultadoPartidoEntity e) {
        e.id = r.getId();
        e.partidoId = r.getPartido().getId();
        e.ganadorLado = r.getGanadorLado();
        e.marcadorSets = r.getMarcadorComoTexto();
        e.duracionMin = r.getDuracionRealMinutos();
        e.observaciones = r.getObservaciones();
        e.validado = r.isValidado();
        e.fechaCarga = r.getFechaCarga();
        return e;
    }

    // ------------------------------------------------------------------ lectura

    @Override
    @Transactional(readOnly = true)
    public Optional<Partido> buscarPorId(UUID id) {
        return partidos.findById(id).map(this::hidratar);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partido> buscar(FiltroPartido filtro) {
        Specification<PartidoEntity> spec = (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            if (filtro.formato() != null) {
                predicados.add(cb.equal(root.get("formato"), filtro.formato()));
            }
            if (filtro.superficie() != null) {
                predicados.add(cb.equal(root.get("superficie"), filtro.superficie()));
            }
            if (filtro.ntrpMin() != null) {
                predicados.add(cb.greaterThanOrEqualTo(root.get("ntrpMax"), filtro.ntrpMin()));
            }
            if (filtro.ntrpMax() != null) {
                predicados.add(cb.lessThanOrEqualTo(root.get("ntrpMin"), filtro.ntrpMax()));
            }
            if (filtro.estado() != null) {
                predicados.add(cb.equal(root.get("estadoActual"), filtro.estado()));
            }
            if (filtro.fecha() != null) {
                LocalDateTime desde = filtro.fecha().atStartOfDay();
                LocalDateTime hasta = desde.plusDays(1);
                predicados.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), desde));
                predicados.add(cb.lessThan(root.get("fechaHora"), hasta));
            }
            if (filtro.zona() != null && !filtro.zona().isBlank()) {
                predicados.add(cb.like(cb.lower(root.get("clubSede")), "%" + filtro.zona().toLowerCase() + "%"));
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };

        List<Partido> resultado = partidos.findAll(spec, org.springframework.data.domain.Sort.by("fechaHora"))
                .stream().map(this::hidratar).toList();

        List<Partido> filtrados = new ArrayList<>(resultado);
        if (filtro.horaDesde() != null) {
            filtrados.removeIf(p -> p.getFechaHora().toLocalTime().isBefore(filtro.horaDesde()));
        }
        if (filtro.horaHasta() != null) {
            filtrados.removeIf(p -> p.getFechaHora().toLocalTime().isAfter(filtro.horaHasta()));
        }
        if (filtro.tieneFiltroGeografico()) {
            UbicacionGeo referencia = filtro.ubicacionReferencia();
            int radio = filtro.radioKm();
            filtrados.removeIf(p -> referencia.distanciaKm(p.getUbicacion()) > radio);
        }
        return filtrados;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partido> buscarConfirmadosParaIniciar(LocalDateTime referencia) {
        return partidos.buscarPorEstadoEInicioHasta(EstadoPartido.CONFIRMADO, referencia)
                .stream().map(this::hidratar).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partido> buscarEnJuegoExpirados(LocalDateTime referencia) {
        return partidos.findByEstadoActual(EstadoPartido.EN_JUEGO).stream()
                .map(this::hidratar)
                .filter(p -> p.getFechaHoraFin().isBefore(referencia))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partido> buscarSinQuorumVencidos(LocalDateTime referencia) {
        List<PartidoEntity> entidades = new ArrayList<>(partidos.buscarPorEstadoEInicioHasta(
                EstadoPartido.BUSCANDO_JUGADORES, referencia));
        entidades.addAll(partidos.buscarPorEstadoEInicioHasta(EstadoPartido.CUPO_COMPLETO, referencia));
        return entidades.stream().map(this::hidratar).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partido> buscarPorParticipante(UUID usuarioId) {
        return partidos.buscarPorParticipante(usuarioId).stream().map(this::hidratar).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partido> buscarPorOrganizador(UUID organizadorId) {
        return partidos.findByOrganizadorIdOrderByFechaHoraDesc(organizadorId).stream().map(this::hidratar).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partido> buscarPorEstado(EstadoPartido estado) {
        return partidos.findByEstadoActual(estado).stream().map(this::hidratar).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeSolapamiento(UUID usuarioId, LocalDateTime desde, LocalDateTime hasta) {
        return buscarPorParticipante(usuarioId).stream()
                .filter(p -> p.getEstado().admiteNuevosJugadores() || p.getEstado() == EstadoPartido.EN_JUEGO)
                .anyMatch(p -> p.getFechaHora().isBefore(hasta) && p.getFechaHoraFin().isAfter(desde));
    }

    @Override
    @Transactional(readOnly = true)
    public long contar() {
        return partidos.count();
    }

    private Partido hidratar(PartidoEntity entity) {
        List<PostulacionEntity> postulacionesEntity = postulaciones.findByPartidoIdOrderByFechaPostulacionAsc(entity.id);
        List<ParejaEntity> parejasEntity = parejas.findByPartidoId(entity.id);
        List<TransicionEstadoEntity> transicionesEntity = transiciones.findByPartidoIdOrderByFechaAsc(entity.id);
        ResultadoPartidoEntity resultadoEntity = resultados.findByPartidoId(entity.id).orElse(null);

        Set<UUID> idsUsuarios = new HashSet<>();
        idsUsuarios.add(entity.organizadorId);
        postulacionesEntity.forEach(p -> idsUsuarios.add(p.usuarioId));
        parejasEntity.forEach(p -> idsUsuarios.addAll(p.jugadores));

        Map<UUID, Usuario> mapa = new HashMap<>();
        usuarios.findAllById(idsUsuarios).forEach(u -> mapa.put(u.id, usuarioMapper.toDomain(u)));

        Partido partido = partidoMapper.toDomain(entity, mapa.get(entity.organizadorId), mapa,
                postulacionesEntity, parejasEntity, transicionesEntity, resultadoEntity);
        partido.setEventPublisher(publicador);
        return partido;
    }
}
