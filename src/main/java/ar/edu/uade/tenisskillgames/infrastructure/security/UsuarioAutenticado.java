package ar.edu.uade.tenisskillgames.infrastructure.security;

import java.util.Set;
import java.util.UUID;

import ar.edu.uade.tenisskillgames.domain.enums.Rol;

/**
 * Principal autenticado a partir del JWT (autenticacion stateless).
 */
public record UsuarioAutenticado(UUID id, String email, String nombre, Set<Rol> roles) {

    public boolean esAdmin() {
        return roles.contains(Rol.ADMIN);
    }

    public boolean esOrganizador() {
        return roles.contains(Rol.ORGANIZADOR) || esAdmin();
    }
}
