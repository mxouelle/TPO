package ar.edu.uade.tenisskillgames.infrastructure.persistence.mapper;

import ar.edu.uade.tenisskillgames.domain.enums.Lado;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoPostulacion;
import ar.edu.uade.tenisskillgames.domain.model.Confirmacion;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Pareja;
import ar.edu.uade.tenisskillgames.domain.model.Postulacion;
import ar.edu.uade.tenisskillgames.domain.model.ResultadoPartido;
import ar.edu.uade.tenisskillgames.domain.model.SetMarcador;
import ar.edu.uade.tenisskillgames.domain.model.TransicionEstado;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.state.PartidoStateFactory;
import ar.edu.uade.tenisskillgames.domain.vo.UbicacionGeo;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.ParejaEntity;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.PartidoEntity;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.PostulacionEntity;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.ResultadoPartidoEntity;
import ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa.TransicionEstadoEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Reconstruye el agregado Partido a partir de sus filas JPA. El agregado
 * siempre se hidrata completo (participantes, postulaciones, parejas,
 * transiciones y resultado).
 */
@Component
public class PartidoMapper {

    public Partido toDomain(PartidoEntity e,
                            Usuario organizador,
                            Map<UUID, Usuario> usuarios,
                            List<PostulacionEntity> postulaciones,
                            List<ParejaEntity> parejas,
                            List<TransicionEstadoEntity> transiciones,
                            ResultadoPartidoEntity resultadoEntity) {

        Partido p = new Partido();
        p.setId(e.id);
        p.setOrganizador(organizador);
        p.setFormato(e.formato);
        p.setSuperficie(e.superficie);
        p.setClubSede(e.clubSede);
        p.setDireccion(e.direccion);
        p.setUbicacion(UbicacionGeo.de(e.latitud, e.longitud));
        p.setNtrpMin(e.ntrpMin);
        p.setNtrpMax(e.ntrpMax);
        p.setRadioMaxKm(e.radioMaxKm);
        p.setFechaHora(e.fechaHora);
        p.setDuracionMinutos(e.duracionMinutos);
        p.setModalidad(e.modalidad);
        p.setCupoMaximo(e.cupoMaximo);
        p.setEstado(e.estadoActual);
        p.setEstadoActual(PartidoStateFactory.para(e.estadoActual));
        p.setEstrategiaEmparejamiento(e.estrategia);
        p.setMotivoCancelacion(e.motivoCancelacion);
        p.setFechaCreacion(e.fechaCreacion);
        p.setObservaciones(e.observaciones);
        p.setRecordatorio24Enviado(e.recordatorio24Enviado);
        p.setRecordatorio2Enviado(e.recordatorio2Enviado);

        for (PostulacionEntity pe : postulaciones) {
            Usuario u = usuarios.get(pe.usuarioId);
            if (u == null) {
                continue;
            }
            Postulacion postulacion = new Postulacion(u, p, pe.ladoAsignado, pe.estado);
            postulacion.setId(pe.id);
            postulacion.setFechaPostulacion(pe.fechaPostulacion);
            postulacion.setMotivoRechazo(pe.motivoRechazo);
            p.getPostulaciones().add(postulacion);

            if (pe.estado == EstadoPostulacion.ACEPTADA) {
                p.getParticipantes().add(u);
            }
            if (pe.estado == EstadoPostulacion.LISTA_ESPERA) {
                p.getListaEspera().add(u);
            }
            if (Boolean.TRUE.equals(pe.confirmado)) {
                Confirmacion c = new Confirmacion(u, p, true);
                c.setFechaConfirmacion(pe.fechaConfirmacion);
                p.getConfirmaciones().add(c);
            }
        }

        for (ParejaEntity parejaEntity : parejas) {
            Pareja pareja = new Pareja(p, parejaEntity.lado);
            pareja.setId(parejaEntity.id);
            for (UUID jugadorId : parejaEntity.jugadores) {
                Usuario u = usuarios.get(jugadorId);
                if (u != null) {
                    pareja.agregarJugador(u);
                }
            }
            p.getParejas().add(pareja);
        }

        for (TransicionEstadoEntity t : transiciones) {
            p.agregarTransicion(new TransicionEstado(t.estadoAnterior, t.estadoNuevo, t.fecha, t.motivo));
        }

        if (resultadoEntity != null) {
            ResultadoPartido r = new ResultadoPartido();
            r.setId(resultadoEntity.id);
            r.setPartido(p);
            r.setGanadorLado(resultadoEntity.ganadorLado);
            r.setDuracionRealMinutos(resultadoEntity.duracionMin);
            r.setObservaciones(resultadoEntity.observaciones);
            r.setValidado(resultadoEntity.validado);
            r.setFechaCarga(resultadoEntity.fechaCarga);
            parseSets(resultadoEntity.marcadorSets).forEach(r.getMarcadorSets()::add);
            p.setResultado(r);
        }

        return p;
    }

    public PartidoEntity toEntity(Partido p) {
        PartidoEntity e = new PartidoEntity();
        copy(p, e);
        return e;
    }

    public void copy(Partido p, PartidoEntity e) {
        e.id = p.getId();
        e.organizadorId = p.getOrganizador().getId();
        e.formato = p.getFormato();
        e.superficie = p.getSuperficie();
        e.clubSede = p.getClubSede();
        e.direccion = p.getDireccion();
        e.latitud = p.getUbicacion().getLatitud();
        e.longitud = p.getUbicacion().getLongitud();
        e.ntrpMin = p.getNtrpMin();
        e.ntrpMax = p.getNtrpMax();
        e.radioMaxKm = p.getRadioMaxKm();
        e.fechaHora = p.getFechaHora();
        e.duracionMinutos = p.getDuracionMinutos();
        e.modalidad = p.getModalidad();
        e.cupoMaximo = p.getCupoMaximo();
        e.estadoActual = p.getEstado();
        e.estrategia = p.getEstrategiaEmparejamiento();
        e.motivoCancelacion = p.getMotivoCancelacion();
        e.fechaCreacion = p.getFechaCreacion();
        e.observaciones = p.getObservaciones();
        e.recordatorio24Enviado = p.isRecordatorio24Enviado();
        e.recordatorio2Enviado = p.isRecordatorio2Enviado();
    }

    public List<SetMarcador> parseSets(String marcador) {
        List<SetMarcador> sets = new ArrayList<>();
        if (marcador == null || marcador.isBlank()) {
            return sets;
        }
        for (String token : marcador.trim().split("[\\s,]+")) {
            String[] partes = token.split("-");
            if (partes.length == 2) {
                try {
                    sets.add(new SetMarcador(Integer.parseInt(partes[0].trim()), Integer.parseInt(partes[1].trim())));
                } catch (NumberFormatException ignored) {
                    // token invalido: se descarta al reconstruir
                }
            }
        }
        return sets;
    }
}
