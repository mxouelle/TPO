package ar.edu.uade.tenisskillgames.service;

import ar.edu.uade.tenisskillgames.controller.dto.Comandos;
import ar.edu.uade.tenisskillgames.controller.dto.Vistas;
import ar.edu.uade.tenisskillgames.controller.mapper.VistaMapper;
import ar.edu.uade.tenisskillgames.domain.enums.Rol;
import ar.edu.uade.tenisskillgames.domain.exception.RecursoNoEncontradoException;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.port.UsuarioRepository;
import ar.edu.uade.tenisskillgames.domain.vo.UbicacionGeo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final VistaMapper mapper;

    public UsuarioService(UsuarioRepository usuarios, VistaMapper mapper) {
        this.usuarios = usuarios;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Usuario buscar(UUID id) {
        return usuarios.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public Vistas.Usuario obtener(UUID id) {
        return mapper.usuario(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<Vistas.Usuario> listar() {
        return usuarios.listar().stream().map(mapper::usuario).toList();
    }

    @Transactional
    public Vistas.Usuario actualizarPerfil(UUID id, Comandos.ActualizarPerfil cmd) {
        Usuario usuario = buscar(id);

        if (cmd.nivelNTRP() != null) {
            usuario.setNivelNTRP(cmd.nivelNTRP());
        }
        if (cmd.zona() != null) {
            usuario.setZona(cmd.zona());
        }
        if (cmd.superficieFavorita() != null) {
            usuario.setSuperficieFavorita(cmd.superficieFavorita());
        }
        if (cmd.manoHabil() != null) {
            usuario.setManoHabil(cmd.manoHabil());
        }
        if (cmd.golpePreferido() != null) {
            usuario.setGolpePreferido(cmd.golpePreferido());
        }
        if (cmd.ladoPreferido() != null) {
            usuario.setLadoPreferido(cmd.ladoPreferido());
        }
        if (cmd.latitud() != null && cmd.longitud() != null) {
            usuario.setUbicacion(UbicacionGeo.de(cmd.latitud(), cmd.longitud()));
        }
        if (cmd.telefono() != null) {
            usuario.setTelefono(cmd.telefono());
        }
        if (cmd.pushToken() != null) {
            usuario.setPushToken(cmd.pushToken());
        }
        if (cmd.franjasHorarias() != null) {
            usuario.setFranjasHorarias(cmd.franjasHorarias());
        }
        if (cmd.canalesHabilitados() != null) {
            usuario.setCanalesHabilitados(cmd.canalesHabilitados());
        }

        return mapper.usuario(usuarios.guardar(usuario));
    }

    /**
     * Promueve la cuenta a organizador (capitan de club).
     */
    @Transactional
    public Vistas.Usuario convertirEnOrganizador(UUID id) {
        Usuario usuario = buscar(id);
        usuario.agregarRol(Rol.ORGANIZADOR);
        return mapper.usuario(usuarios.guardar(usuario));
    }
}
