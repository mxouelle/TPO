package ar.edu.uade.tenisskillgames.service;

import ar.edu.uade.tenisskillgames.controller.dto.Comandos;
import ar.edu.uade.tenisskillgames.controller.dto.Vistas;
import ar.edu.uade.tenisskillgames.controller.mapper.VistaMapper;
import ar.edu.uade.tenisskillgames.domain.exception.RecursoNoEncontradoException;
import ar.edu.uade.tenisskillgames.domain.model.BusquedaFavorita;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.port.BusquedaFavoritaRepository;
import ar.edu.uade.tenisskillgames.domain.port.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * RF2: busquedas favoritas y suscripcion a alertas automaticas.
 */
@Service
public class BusquedaFavoritaService {

    private final BusquedaFavoritaRepository busquedas;
    private final UsuarioRepository usuarios;
    private final VistaMapper mapper;

    public BusquedaFavoritaService(BusquedaFavoritaRepository busquedas, UsuarioRepository usuarios,
                                   VistaMapper mapper) {
        this.busquedas = busquedas;
        this.usuarios = usuarios;
        this.mapper = mapper;
    }

    @Transactional
    public Vistas.Busqueda crear(UUID usuarioId, Comandos.BusquedaFavorita cmd) {
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        BusquedaFavorita busqueda = new BusquedaFavorita(usuario,
                cmd.nombre() == null || cmd.nombre().isBlank() ? "Búsqueda guardada" : cmd.nombre(),
                cmd.formato(), cmd.superficie(), cmd.ntrpMin(), cmd.ntrpMax(), cmd.radioKm(), cmd.zona());

        return mapper.busqueda(busquedas.guardar(busqueda));
    }

    @Transactional(readOnly = true)
    public List<Vistas.Busqueda> listar(UUID usuarioId) {
        return busquedas.listarPorUsuario(usuarioId).stream().map(mapper::busqueda).toList();
    }

    @Transactional
    public void eliminar(UUID usuarioId, UUID busquedaId) {
        BusquedaFavorita busqueda = busquedas.buscarPorId(busquedaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Búsqueda no encontrada"));
        if (busqueda.getUsuario() == null || !busqueda.getUsuario().getId().equals(usuarioId)) {
            throw new RecursoNoEncontradoException("Búsqueda no encontrada para el usuario");
        }
        busquedas.eliminar(busquedaId);
    }
}
