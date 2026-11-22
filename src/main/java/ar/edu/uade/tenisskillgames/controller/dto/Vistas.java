package ar.edu.uade.tenisskillgames.controller.dto;

import ar.edu.uade.tenisskillgames.domain.enums.CanalNotificacion;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoEnvio;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoModeracion;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoReporte;
import ar.edu.uade.tenisskillgames.domain.enums.FormatoTenis;
import ar.edu.uade.tenisskillgames.domain.enums.GolpePreferido;
import ar.edu.uade.tenisskillgames.domain.enums.Lado;
import ar.edu.uade.tenisskillgames.domain.enums.ManoHabil;
import ar.edu.uade.tenisskillgames.domain.enums.Modalidad;
import ar.edu.uade.tenisskillgames.domain.enums.MotivoReporte;
import ar.edu.uade.tenisskillgames.domain.enums.Rol;
import ar.edu.uade.tenisskillgames.domain.enums.Superficie;
import ar.edu.uade.tenisskillgames.domain.enums.TipoEvento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Vistas (DTO de salida) de la API REST.
 */
public final class Vistas {

    private Vistas() {
    }

    public record Auth(String token, String tipo, long expiraEnSegundos, Usuario usuario) {
    }

    public record Registro(Usuario usuario, String tokenVerificacion, String mensaje) {
    }

    public record Usuario(UUID id,
                          String nombre,
                          String email,
                          double nivelNTRP,
                          ManoHabil manoHabil,
                          GolpePreferido golpePreferido,
                          Superficie superficieFavorita,
                          String zona,
                          double latitud,
                          double longitud,
                          int strikes,
                          String estadoVerificacion,
                          Set<Rol> roles,
                          Set<CanalNotificacion> canalesHabilitados,
                          LocalDate rehabilitacion,
                          double fairPlayPromedio,
                          double tasaAsistencia,
                          int partidosJugados) {
    }

    public record Participante(UUID id, String nombre, double nivelNTRP, Lado lado, boolean confirmado) {
    }

    public record ParejaVista(Lado lado, List<Participante> jugadores, double promedioNTRP) {
    }

    public record ResultadoVista(Lado ganadorLado, String marcador, int duracionRealMinutos,
                                String observaciones, boolean validado) {
    }

    public record TransicionVista(EstadoPartido estadoAnterior, EstadoPartido estadoNuevo,
                                  LocalDateTime fecha, String motivo) {
    }

    public record Partido(UUID id,
                          String clubSede,
                          String direccion,
                          FormatoTenis formato,
                          Superficie superficie,
                          Modalidad modalidad,
                          EstadoPartido estado,
                          LocalDateTime fechaHora,
                          int duracionMinutos,
                          double ntrpMin,
                          double ntrpMax,
                          int radioMaxKm,
                          int cupoMaximo,
                          int plazasLibres,
                          UUID organizadorId,
                          String organizador,
                          String estrategia,
                          List<Participante> participantes,
                          List<Participante> listaEspera,
                          List<ParejaVista> parejas,
                          ResultadoVista resultado,
                          List<TransicionVista> historialEstados,
                          String motivoCancelacion) {
    }

    public record Candidato(UUID id,
                            String nombre,
                            double nivelNTRP,
                            double distanciaKm,
                            double fairPlay,
                            double tasaAsistencia,
                            double puntaje,
                            String estrategia) {
    }

    public record Emparejamiento(String estrategia,
                                 List<Candidato> candidatos,
                                 List<ParejaVista> parejas,
                                 int jugadoresInscriptos,
                                 int plazasLibres) {
    }

    public record Notificacion(UUID id,
                               CanalNotificacion canal,
                               TipoEvento tipoEvento,
                               String asunto,
                               String contenido,
                               EstadoEnvio estadoEnvio,
                               int intentos,
                               LocalDateTime fechaCreacion,
                               String destinatario) {
    }

    public record EvaluacionVista(UUID id,
                                  UUID evaluadoId,
                                  String evaluado,
                                  int fairPlay,
                                  int puntualidad,
                                  String comentario,
                                  EstadoModeracion moderacion,
                                  LocalDateTime fecha) {
    }

    public record ReporteVista(UUID id,
                               UUID partidoId,
                               String reportante,
                               String reportado,
                               MotivoReporte motivo,
                               String descripcion,
                               EstadoReporte estado,
                               String resolucion,
                               LocalDateTime fecha) {
    }

    public record Busqueda(UUID id,
                           String nombre,
                           FormatoTenis formato,
                           Superficie superficie,
                           double ntrpMin,
                           double ntrpMax,
                           int radioKm,
                           String zona,
                           boolean activa) {
    }

    public record Estadisticas(long usuarios,
                               long partidos,
                               long notificacionesEnviadas,
                               long notificacionesFallidas,
                               Map<String, Long> partidosPorEstado) {
    }

    public record Mensaje(String mensaje) {
    }

    public record Archivo(String nombre, byte[] contenido) {
    }

    public record Error(int status,
                        String error,
                        String mensaje,
                        String path,
                        LocalDateTime timestamp,
                        Map<String, String> detalles) {
    }
}
