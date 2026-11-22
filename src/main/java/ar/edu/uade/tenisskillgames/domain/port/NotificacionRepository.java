package ar.edu.uade.tenisskillgames.domain.port;

import ar.edu.uade.tenisskillgames.domain.model.Notificacion;

import java.util.List;
import java.util.UUID;

public interface NotificacionRepository {

    Notificacion guardar(Notificacion notificacion);

    List<Notificacion> listarPorUsuario(UUID usuarioId);

    List<Notificacion> listarPorPartido(UUID partidoId);

    List<Notificacion> listarUltimas(int limite);

    long contarPorEstado(ar.edu.uade.tenisskillgames.domain.enums.EstadoEnvio estado);
}
