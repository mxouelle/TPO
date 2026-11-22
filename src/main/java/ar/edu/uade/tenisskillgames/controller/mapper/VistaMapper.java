package ar.edu.uade.tenisskillgames.controller.mapper;

import ar.edu.uade.tenisskillgames.controller.dto.Vistas;
import ar.edu.uade.tenisskillgames.domain.enums.Lado;
import ar.edu.uade.tenisskillgames.domain.model.BusquedaFavorita;
import ar.edu.uade.tenisskillgames.domain.model.EvaluacionJugador;
import ar.edu.uade.tenisskillgames.domain.model.Notificacion;
import ar.edu.uade.tenisskillgames.domain.model.Pareja;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.ReporteConducta;
import ar.edu.uade.tenisskillgames.domain.model.ResultadoPartido;
import ar.edu.uade.tenisskillgames.domain.model.TransicionEstado;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Traduce agregados de dominio a las vistas de la API REST.
 */
@Component
public class VistaMapper {

    public Vistas.Usuario usuario(Usuario u) {
        return new Vistas.Usuario(
                u.getId(),
                u.getNombre(),
                u.getEmail(),
                u.getNivelNTRP(),
                u.getManoHabil(),
                u.getGolpePreferido(),
                u.getSuperficieFavorita(),
                u.getZona(),
                u.getUbicacion().getLatitud(),
                u.getUbicacion().getLongitud(),
                u.getStrikes(),
                u.getEstadoVerificacion().name(),
                u.getRoles(),
                u.getCanalesHabilitados(),
                u.estaSuspendido() ? u.getRehabilitacion() : null,
                u.getHistorial().getFairPlayPromedio(),
                u.getHistorial().tasaAsistencia(),
                u.getHistorial().getPartidosJugados());
    }

    public Vistas.Partido partido(Partido p) {
        List<Vistas.Participante> participantes = p.getParticipantes().stream()
                .map(u -> participante(u, ladoDe(p, u), p.estaConfirmado(u)))
                .toList();

        List<Vistas.Participante> espera = p.getListaEspera().stream()
                .map(u -> participante(u, Lado.INDIFERENTE, false))
                .toList();

        List<Vistas.ParejaVista> parejas = p.getParejas().stream()
                .map(pareja -> new Vistas.ParejaVista(
                        pareja.getLado(),
                        pareja.getJugadores().stream()
                                .map(j -> participante(j, pareja.getLado(), p.estaConfirmado(j)))
                                .toList(),
                        redondear(pareja.getPromedioNTRP())))
                .toList();

        Vistas.ResultadoVista resultado = p.getResultado() == null ? null : resultado(p.getResultado());

        List<Vistas.TransicionVista> historial = p.getHistorialEstados().stream()
                .map(this::transicion)
                .toList();

        return new Vistas.Partido(
                p.getId(),
                p.getClubSede(),
                p.getDireccion(),
                p.getFormato(),
                p.getSuperficie(),
                p.getModalidad(),
                p.getEstado(),
                p.getFechaHora(),
                p.getDuracionMinutos(),
                p.getNtrpMin(),
                p.getNtrpMax(),
                p.getRadioMaxKm(),
                p.getCupoMaximo(),
                p.getPlazasLibres(),
                p.getOrganizador() == null ? null : p.getOrganizador().getId(),
                p.getOrganizador() == null ? null : p.getOrganizador().getNombre(),
                p.getEstrategiaEmparejamiento(),
                participantes,
                espera,
                parejas,
                resultado,
                historial,
                p.getMotivoCancelacion());
    }

    public Vistas.Participante participante(Usuario u, Lado lado, boolean confirmado) {
        return new Vistas.Participante(u.getId(), u.getNombre(), u.getNivelNTRP(), lado, confirmado);
    }

    public Vistas.ResultadoVista resultado(ResultadoPartido r) {
        return new Vistas.ResultadoVista(
                r.getGanadorLado(),
                r.getMarcadorComoTexto(),
                r.getDuracionRealMinutos(),
                r.getObservaciones(),
                r.isValidado());
    }

    public Vistas.TransicionVista transicion(TransicionEstado t) {
        return new Vistas.TransicionVista(t.estadoAnterior(), t.estadoNuevo(), t.fecha(), t.motivo());
    }

    public Vistas.Notificacion notificacion(Notificacion n) {
        return new Vistas.Notificacion(
                n.getId(),
                n.getCanal(),
                n.getTipoEvento(),
                n.getAsunto(),
                n.getContenido(),
                n.getEstadoEnvio(),
                n.getIntentos(),
                n.getFechaCreacion(),
                n.getDestinatario() == null ? null : n.getDestinatario().getNombre());
    }

    public Vistas.EvaluacionVista evaluacion(EvaluacionJugador e) {
        return new Vistas.EvaluacionVista(
                e.getId(),
                e.getEvaluado().getId(),
                e.getEvaluado().getNombre(),
                e.getPuntajeFairPlay(),
                e.getPuntajePuntualidad(),
                e.getComentario(),
                e.getEstadoModeracion(),
                e.getFechaEvaluacion());
    }

    public Vistas.ReporteVista reporte(ReporteConducta r) {
        return new Vistas.ReporteVista(
                r.getId(),
                r.getPartido() == null ? null : r.getPartido().getId(),
                r.getReportante() == null ? null : r.getReportante().getNombre(),
                r.getReportado() == null ? null : r.getReportado().getNombre(),
                r.getMotivo(),
                r.getDescripcion(),
                r.getEstado(),
                r.getResolucion(),
                r.getFechaReporte());
    }

    public Vistas.Busqueda busqueda(BusquedaFavorita b) {
        return new Vistas.Busqueda(
                b.getId(),
                b.getNombre(),
                b.getFormato(),
                b.getSuperficie(),
                b.getNtrpMin(),
                b.getNtrpMax(),
                b.getRadioKm(),
                b.getZona(),
                b.isActiva());
    }

    public Vistas.Candidato candidato(Usuario u, Partido p, double puntaje, String estrategia) {
        return new Vistas.Candidato(
                u.getId(),
                u.getNombre(),
                u.getNivelNTRP(),
                redondear(p.distanciaAlClubKm(u)),
                u.getHistorial().getFairPlayPromedio(),
                u.getHistorial().tasaAsistencia(),
                redondear(puntaje),
                estrategia);
    }

    private Lado ladoDe(Partido p, Usuario u) {
        return p.getParejas().stream()
                .filter(pareja -> pareja.getJugadores().stream().anyMatch(j -> j.equals(u)))
                .map(Pareja::getLado)
                .findFirst()
                .orElse(Lado.INDIFERENTE);
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
