package ar.edu.uade.tenisskillgames.controller.dto;

import ar.edu.uade.tenisskillgames.domain.enums.CanalNotificacion;
import ar.edu.uade.tenisskillgames.domain.enums.FormatoTenis;
import ar.edu.uade.tenisskillgames.domain.enums.GolpePreferido;
import ar.edu.uade.tenisskillgames.domain.enums.Lado;
import ar.edu.uade.tenisskillgames.domain.enums.ManoHabil;
import ar.edu.uade.tenisskillgames.domain.enums.Modalidad;
import ar.edu.uade.tenisskillgames.domain.enums.MotivoReporte;
import ar.edu.uade.tenisskillgames.domain.enums.Superficie;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Comandos de entrada de la capa web hacia los servicios de aplicacion.
 */
public final class Comandos {

    private Comandos() {
    }

    public record RegistroUsuario(
            @NotBlank @Size(max = 80) String nombre,
            @NotBlank @Email @Size(max = 120) String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @DecimalMin("1.5") @DecimalMax("7.0") double nivelNTRP,
            @NotBlank @Size(max = 60) String zona,
            Superficie superficieFavorita,
            ManoHabil manoHabil,
            GolpePreferido golpePreferido,
            @DecimalMin("-90") @DecimalMax("90") double latitud,
            @DecimalMin("-180") @DecimalMax("180") double longitud,
            @Size(max = 30) String telefono,
            @Size(max = 200) String pushToken,
            List<String> franjasHorarias) {
    }

    public record Login(@NotBlank @Email String email, @NotBlank String password) {
    }

    public record VerificarEmail(@NotBlank String token) {
    }

    public record ActualizarPerfil(
            @DecimalMin("1.5") @DecimalMax("7.0") Double nivelNTRP,
            @Size(max = 60) String zona,
            Superficie superficieFavorita,
            ManoHabil manoHabil,
            GolpePreferido golpePreferido,
            Lado ladoPreferido,
            @DecimalMin("-90") @DecimalMax("90") Double latitud,
            @DecimalMin("-180") @DecimalMax("180") Double longitud,
            @Size(max = 30) String telefono,
            @Size(max = 200) String pushToken,
            List<String> franjasHorarias,
            Set<CanalNotificacion> canalesHabilitados) {
    }

    public record CrearPartido(
            @NotNull FormatoTenis formato,
            @NotNull Superficie superficie,
            @NotBlank @Size(max = 100) String clubSede,
            @Size(max = 160) String direccion,
            @DecimalMin("-90") @DecimalMax("90") double latitud,
            @DecimalMin("-180") @DecimalMax("180") double longitud,
            @DecimalMin("1.5") @DecimalMax("7.0") double ntrpMin,
            @DecimalMin("1.5") @DecimalMax("7.0") double ntrpMax,
            @NotNull @Future LocalDateTime fechaHora,
            @Min(30) @Max(300) int duracionMinutos,
            @NotNull Modalidad modalidad,
            @Min(1) @Max(100) int radioMaxKm,
            @Size(max = 20) String estrategia,
            @Size(max = 500) String observaciones) {
    }

    public record Postular(Lado ladoDeseado) {
    }

    public record Confirmacion(@NotNull Boolean confirmado, @Size(max = 200) String motivo) {
    }

    public record SetComando(@Min(0) @Max(30) int local, @Min(0) @Max(30) int visitante) {
    }

    public record Resultado(
            @NotNull Lado ganadorLado,
            @NotEmpty List<SetComando> sets,
            @Min(1) @Max(600) int duracionRealMinutos,
            @Size(max = 500) String observaciones) {
    }

    public record Evaluacion(
            @NotNull UUID evaluadoId,
            @Min(1) @Max(5) int fairPlay,
            @Min(1) @Max(5) int puntualidad,
            @Size(max = 500) String comentario) {
    }

    public record Reporte(
            @NotNull UUID reportadoId,
            @NotNull MotivoReporte motivo,
            @Size(max = 500) String descripcion) {
    }

    public record ResolucionReporte(boolean confirmar, @Size(max = 300) String resolucion) {
    }

    public record Cancelacion(@NotBlank @Size(max = 200) String motivo) {
    }

    public record AsignarPareja(@NotNull Lado lado, @NotEmpty List<UUID> jugadores) {
    }

    public record BusquedaFavorita(
            @Size(max = 80) String nombre,
            FormatoTenis formato,
            Superficie superficie,
            @DecimalMin("1.5") @DecimalMax("7.0") double ntrpMin,
            @DecimalMin("1.5") @DecimalMax("7.0") double ntrpMax,
            @Min(1) @Max(100) int radioKm,
            @Size(max = 60) String zona) {
    }
}
