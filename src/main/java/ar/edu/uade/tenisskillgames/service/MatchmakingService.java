package ar.edu.uade.tenisskillgames.service;

import ar.edu.uade.tenisskillgames.controller.dto.Vistas;
import ar.edu.uade.tenisskillgames.controller.mapper.VistaMapper;
import ar.edu.uade.tenisskillgames.domain.enums.Rol;
import ar.edu.uade.tenisskillgames.domain.exception.OperacionNoPermitidaException;
import ar.edu.uade.tenisskillgames.domain.exception.RecursoNoEncontradoException;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.port.PartidoRepository;
import ar.edu.uade.tenisskillgames.domain.port.UsuarioRepository;
import ar.edu.uade.tenisskillgames.domain.strategy.MatchmakingStrategy;
import ar.edu.uade.tenisskillgames.domain.strategy.MatchmakingStrategyRegistry;
import ar.edu.uade.tenisskillgames.domain.strategy.PorCercaniaStrategy;
import ar.edu.uade.tenisskillgames.domain.strategy.PorHistorialStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso CU5: ejecuta la estrategia de emparejamiento seleccionada y,
 * opcionalmente, inscribe a los mejores candidatos equilibrando parejas.
 */
@Service
public class MatchmakingService {

    private static final Logger LOG = LoggerFactory.getLogger(MatchmakingService.class);
    private static final int LIMITE_CANDIDATOS = 500;

    private final PartidoRepository partidos;
    private final UsuarioRepository usuarios;
    private final MatchmakingStrategyRegistry estrategias;
    private final VistaMapper mapper;

    public MatchmakingService(PartidoRepository partidos, UsuarioRepository usuarios,
                              MatchmakingStrategyRegistry estrategias, VistaMapper mapper) {
        this.partidos = partidos;
        this.usuarios = usuarios;
        this.estrategias = estrategias;
        this.mapper = mapper;
    }

    @Transactional
    public Vistas.Emparejamiento emparejar(UUID partidoId, String estrategia, boolean autoInscribir, UUID usuarioId) {
        Partido partido = partidos.buscarPorId(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Partido no encontrado: " + partidoId));

        validarOrganizador(partido, usuarioId);

        if (!partido.getEstado().admiteNuevosJugadores()) {
            throw new OperacionNoPermitidaException("El partido está " + partido.getEstado()
                    + " y no admite emparejamiento");
        }

        String codigo = (estrategia == null || estrategia.isBlank())
                ? partido.getEstrategiaEmparejamiento() : estrategia;
        MatchmakingStrategy strategy = estrategias.obtener(codigo);

        List<Usuario> pool = usuarios.buscarCandidatos(partido.getId(), LIMITE_CANDIDATOS);
        long inicio = System.nanoTime();
        List<Usuario> seleccionados = strategy.seleccionarCandidatos(pool, partido);
        long duracionMs = (System.nanoTime() - inicio) / 1_000_000;
        LOG.debug("Estrategia {} evaluo {} candidatos en {} ms", strategy.getCodigo(), pool.size(), duracionMs);

        if (autoInscribir) {
            for (Usuario candidato : seleccionados) {
                if (partido.getPlazasLibres() <= 0) {
                    break;
                }
                try {
                    partido.postular(candidato);
                } catch (RuntimeException ex) {
                    LOG.debug("Candidato {} descartado por el dominio: {}", candidato.getId(), ex.getMessage());
                }
            }
            if (partido.getFormato().esDobles() && !partido.getParticipantes().isEmpty()) {
                partido.reasignarParejas();
            }
            partidos.guardar(partido);
        }

        List<Vistas.Candidato> candidatos = seleccionados.stream()
                .map(u -> mapper.candidato(u, partido, puntaje(strategy, u, partido), strategy.getCodigo()))
                .toList();

        Vistas.Partido vista = mapper.partido(partido);
        return new Vistas.Emparejamiento(strategy.getCodigo(), candidatos, vista.parejas(),
                vista.participantes().size(), vista.plazasLibres());
    }

    private void validarOrganizador(Partido partido, UUID usuarioId) {
        if (partido.getOrganizador() != null && partido.getOrganizador().getId().equals(usuarioId)) {
            return;
        }
        boolean esAdmin = usuarios.buscarPorId(usuarioId).map(u -> u.tieneRol(Rol.ADMIN)).orElse(false);
        if (!esAdmin) {
            throw new OperacionNoPermitidaException(
                    "Solo el organizador o un administrador puede ejecutar el emparejamiento");
        }
    }

    /**
     * Puntaje de afinidad mostrado en la respuesta (0 a 1).
     */
    private double puntaje(MatchmakingStrategy strategy, Usuario candidato, Partido partido) {
        if (strategy instanceof PorHistorialStrategy historial) {
            return historial.puntaje(candidato, partido);
        }
        if (strategy instanceof PorCercaniaStrategy) {
            int radio = partido.getRadioMaxKm();
            if (radio <= 0) {
                return 1.0;
            }
            return Math.max(0, 1 - (partido.distanciaAlClubKm(candidato) / radio));
        }
        double delta = Math.abs(candidato.getNivelNTRP() - partido.getNtrpPromedio());
        return Math.max(0, 1 - (delta / 2.0));
    }
}
