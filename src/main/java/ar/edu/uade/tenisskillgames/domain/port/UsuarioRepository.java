package ar.edu.uade.tenisskillgames.domain.port;

import ar.edu.uade.tenisskillgames.domain.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de usuarios. La implementacion JPA vive en
 * infrastructure, manteniendo el dominio desacoplado.
 */
public interface UsuarioRepository {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(UUID id);

    Optional<Usuario> buscarPorEmail(String email);

    boolean existeEmail(String email);

    List<Usuario> listar();

    /**
     * Pool de candidatos para emparejamiento: cuentas activas y verificadas.
     */
    List<Usuario> buscarCandidatos(UUID partidoId, int limite);
}
