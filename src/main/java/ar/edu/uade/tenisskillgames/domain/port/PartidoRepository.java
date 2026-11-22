package ar.edu.uade.tenisskillgames.domain.port;

import ar.edu.uade.tenisskillgames.domain.model.Partido;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartidoRepository {

    Partido guardar(Partido partido);

    Optional<Partido> buscarPorId(UUID id);

    List<Partido> buscar(FiltroPartido filtro);

    /**
     * Partidos confirmados cuya hora de inicio ya llego (CU7).
     */
    List<Partido> buscarConfirmadosParaIniciar(LocalDateTime referencia);

    /**
     * Partidos en juego cuya duracion estimada expiro (CU8).
     */
    List<Partido> buscarEnJuegoExpirados(LocalDateTime referencia);

    /**
     * Partidos abiertos cuya hora de inicio llego sin quorum (CU7, flujo alternativo).
     */
    List<Partido> buscarSinQuorumVencidos(LocalDateTime referencia);

    List<Partido> buscarPorParticipante(UUID usuarioId);

    List<Partido> buscarPorOrganizador(UUID organizadorId);

    List<Partido> buscarPorEstado(ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido estado);

    /**
     * Detecta solapamiento horario de un jugador (regla CU4).
     */
    boolean existeSolapamiento(UUID usuarioId, LocalDateTime desde, LocalDateTime hasta);

    long contar();
}
