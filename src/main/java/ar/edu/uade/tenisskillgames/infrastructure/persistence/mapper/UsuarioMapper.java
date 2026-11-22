package ar.edu.uade.tenisskillgames.infrastructure.persistence.mapper;

import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.vo.HistorialDeportivo;
import ar.edu.uade.tenisskillgames.domain.vo.UbicacionGeo;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.UsuarioEntity;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * Traduce entre el agregado de dominio Usuario y su proyeccion JPA.
 */
@Component
public class UsuarioMapper {

    public Usuario toDomain(UsuarioEntity e) {
        if (e == null) {
            return null;
        }
        Usuario u = new Usuario();
        u.setId(e.id);
        u.setNombre(e.nombre);
        u.setEmail(e.email);
        u.setPasswordHash(e.passwordHash);
        u.setNivelNTRP(e.nivelNtrp);
        u.setManoHabil(e.manoHabil);
        u.setGolpePreferido(e.golpePreferido);
        u.setLadoPreferido(e.ladoPreferido);
        u.setSuperficieFavorita(e.superficieFavorita);
        u.setZona(e.zona);
        u.setUbicacion(UbicacionGeo.de(e.latitud, e.longitud));
        u.setStrikes(e.strikes);
        u.setEstadoVerificacion(e.estadoVerificacion);
        u.setRoles(e.roles == null || e.roles.isEmpty() ? new HashSet<>() : new HashSet<>(e.roles));
        u.setCanalesHabilitados(e.canales == null ? null : new HashSet<>(e.canales));
        u.setHistorial(new HistorialDeportivo(e.partidosJugados, e.ausencias,
                e.fairPlayPromedio, e.puntualidadPromedio));
        u.setFechaRegistro(e.fechaRegistro);
        u.setSuspendidoHasta(e.suspendidoHasta);
        u.setActivo(e.activo);
        u.setTelefono(e.telefono);
        u.setPushToken(e.pushToken);
        if (e.franjasHorarias != null) {
            u.setFranjasHorarias(new java.util.ArrayList<>(e.franjasHorarias));
        }
        return u;
    }

    public UsuarioEntity toEntity(Usuario u) {
        UsuarioEntity e = new UsuarioEntity();
        e.id = u.getId();
        copy(u, e);
        return e;
    }

    public void copy(Usuario u, UsuarioEntity e) {
        e.id = u.getId();
        e.nombre = u.getNombre();
        e.email = u.getEmail();
        e.passwordHash = u.getPasswordHash();
        e.nivelNtrp = u.getNivelNTRP();
        e.manoHabil = u.getManoHabil();
        e.golpePreferido = u.getGolpePreferido();
        e.ladoPreferido = u.getLadoPreferido();
        e.superficieFavorita = u.getSuperficieFavorita();
        e.zona = u.getZona();
        e.latitud = u.getUbicacion().getLatitud();
        e.longitud = u.getUbicacion().getLongitud();
        e.strikes = u.getStrikes();
        e.estadoVerificacion = u.getEstadoVerificacion();
        e.roles = new HashSet<>(u.getRoles());
        e.canales = new HashSet<>(u.getCanalesHabilitados());
        e.franjasHorarias = new HashSet<>(u.getFranjasHorarias());
        e.partidosJugados = u.getHistorial().getPartidosJugados();
        e.ausencias = u.getHistorial().getAusencias();
        e.fairPlayPromedio = u.getHistorial().getFairPlayPromedio();
        e.puntualidadPromedio = u.getHistorial().getPuntualidadPromedio();
        e.fechaRegistro = u.getFechaRegistro();
        e.suspendidoHasta = u.getSuspendidoHasta();
        e.activo = u.isActivo();
        e.telefono = u.getTelefono();
        e.pushToken = u.getPushToken();
    }
}
