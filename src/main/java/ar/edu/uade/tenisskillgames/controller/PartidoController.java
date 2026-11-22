package ar.edu.uade.tenisskillgames.controller;

import ar.edu.uade.tenisskillgames.controller.dto.Comandos;
import ar.edu.uade.tenisskillgames.controller.dto.Vistas;
import ar.edu.uade.tenisskillgames.service.CalendarioService;
import ar.edu.uade.tenisskillgames.service.ConfirmacionService;
import ar.edu.uade.tenisskillgames.service.MatchmakingService;
import ar.edu.uade.tenisskillgames.service.PartidoService;
import ar.edu.uade.tenisskillgames.service.PostulacionService;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.enums.FormatoTenis;
import ar.edu.uade.tenisskillgames.domain.enums.Lado;
import ar.edu.uade.tenisskillgames.domain.enums.Superficie;
import ar.edu.uade.tenisskillgames.domain.port.FiltroPartido;
import ar.edu.uade.tenisskillgames.domain.vo.UbicacionGeo;
import ar.edu.uade.tenisskillgames.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * RF2 / RF3 / RF4 / RF6 / CU4 / CU5 / CU6 / CU10: busqueda, creacion,
 * postulaciones, confirmaciones, emparejamiento y calendario.
 */
@RestController
@RequestMapping("/api/partidos")
public class PartidoController {

    private final PartidoService partidos;
    private final PostulacionService postulaciones;
    private final ConfirmacionService confirmaciones;
    private final MatchmakingService emparejamiento;
    private final CalendarioService calendario;

    public PartidoController(PartidoService partidos, PostulacionService postulaciones,
                             ConfirmacionService confirmaciones, MatchmakingService emparejamiento,
                             CalendarioService calendario) {
        this.partidos = partidos;
        this.postulaciones = postulaciones;
        this.confirmaciones = confirmaciones;
        this.emparejamiento = emparejamiento;
        this.calendario = calendario;
    }

    // ------------------------------------------------------------------ busqueda y detalle

    @GetMapping
    public List<Vistas.Partido> buscar(
            @RequestParam(required = false) FormatoTenis formato,
            @RequestParam(required = false) Superficie superficie,
            @RequestParam(required = false) Double ntrpMin,
            @RequestParam(required = false) Double ntrpMax,
            @RequestParam(required = false) Integer radioKm,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime horaDesde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime horaHasta,
            @RequestParam(required = false) String zona,
            @RequestParam(required = false) EstadoPartido estado,
            @RequestParam(required = false) Double latitud,
            @RequestParam(required = false) Double longitud,
            @AuthenticationPrincipal UsuarioAutenticado actual) {

        UbicacionGeo referencia = (latitud != null && longitud != null)
                ? UbicacionGeo.de(latitud, longitud) : null;

        FiltroPartido filtro = new FiltroPartido(formato, superficie, ntrpMin, ntrpMax, radioKm,
                fecha, horaDesde, horaHasta, zona,
                estado == null ? EstadoPartido.BUSCANDO_JUGADORES : estado,
                referencia, actual == null ? null : actual.id());

        return partidos.buscarCercanos(filtro, actual == null ? null : actual.id());
    }

    @GetMapping("/{id}")
    public Vistas.Partido obtener(@PathVariable UUID id) {
        return partidos.obtener(id);
    }

    @GetMapping("/mios")
    public List<Vistas.Partido> mios(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return partidos.deJugador(actual.id());
    }

    @GetMapping("/organizados")
    public List<Vistas.Partido> organizados(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return partidos.deOrganizador(actual.id());
    }

    // ------------------------------------------------------------------ CU3 creacion

    @PostMapping
    public ResponseEntity<Vistas.Partido> crear(@AuthenticationPrincipal UsuarioAutenticado actual,
                                                @Valid @RequestBody Comandos.CrearPartido cmd) {
        return ResponseEntity.status(HttpStatus.CREATED).body(partidos.crear(actual.id(), cmd));
    }

    // ------------------------------------------------------------------ CU4 postulaciones

    @PostMapping("/{id}/postulaciones")
    public Vistas.Partido postular(@PathVariable UUID id,
                                   @AuthenticationPrincipal UsuarioAutenticado actual,
                                   @RequestBody(required = false) Comandos.Postular cmd) {
        Lado lado = cmd == null || cmd.ladoDeseado() == null ? Lado.INDIFERENTE : cmd.ladoDeseado();
        return postulaciones.postular(id, actual.id(), lado);
    }

    @DeleteMapping("/{id}/postulaciones")
    public Vistas.Partido darseDeBaja(@PathVariable UUID id,
                                      @AuthenticationPrincipal UsuarioAutenticado actual,
                                      @RequestParam(required = false) String motivo) {
        return postulaciones.darDeBaja(id, actual.id(), motivo);
    }

    @DeleteMapping("/{id}/lista-espera")
    public Vistas.Partido abandonarEspera(@PathVariable UUID id,
                                          @AuthenticationPrincipal UsuarioAutenticado actual) {
        return postulaciones.abandonarListaEspera(id, actual.id());
    }

    // ------------------------------------------------------------------ CU6 confirmaciones

    @PostMapping("/{id}/confirmaciones")
    public Vistas.Partido confirmar(@PathVariable UUID id,
                                    @AuthenticationPrincipal UsuarioAutenticado actual,
                                    @Valid @RequestBody Comandos.Confirmacion cmd) {
        return confirmaciones.registrar(id, actual.id(), cmd.confirmado(), cmd.motivo());
    }

    // ------------------------------------------------------------------ CU5 emparejamiento

    @PostMapping("/{id}/emparejar")
    public Vistas.Emparejamiento emparejar(@PathVariable UUID id,
                                           @RequestParam(defaultValue = "NTRP") String estrategia,
                                           @RequestParam(defaultValue = "true") boolean autoInscribir,
                                           @AuthenticationPrincipal UsuarioAutenticado actual) {
        return emparejamiento.emparejar(id, estrategia, autoInscribir, actual.id());
    }

    // ------------------------------------------------------------------ CU9 / CU7 / CU8

    @PostMapping("/{id}/cancelar")
    public Vistas.Partido cancelar(@PathVariable UUID id,
                                   @AuthenticationPrincipal UsuarioAutenticado actual,
                                   @Valid @RequestBody Comandos.Cancelacion cmd) {
        return partidos.cancelar(id, actual.id(), cmd.motivo());
    }

    @PostMapping("/{id}/iniciar")
    public Vistas.Partido iniciar(@PathVariable UUID id,
                                  @AuthenticationPrincipal UsuarioAutenticado actual) {
        return partidos.iniciar(id, actual.id());
    }

    @PostMapping("/{id}/finalizar")
    public Vistas.Partido finalizar(@PathVariable UUID id,
                                    @AuthenticationPrincipal UsuarioAutenticado actual) {
        return partidos.finalizar(id, actual.id());
    }

    // ------------------------------------------------------------------ RF6 parejas

    @PostMapping("/{id}/parejas")
    public Vistas.Partido asignarPareja(@PathVariable UUID id,
                                        @AuthenticationPrincipal UsuarioAutenticado actual,
                                        @Valid @RequestBody Comandos.AsignarPareja cmd) {
        return partidos.asignarPareja(id, actual.id(), cmd);
    }

    @PostMapping("/{id}/parejas/rearmar")
    public Vistas.Partido rearmarParejas(@PathVariable UUID id,
                                         @AuthenticationPrincipal UsuarioAutenticado actual) {
        return partidos.rearmarParejas(id, actual.id());
    }

    // ------------------------------------------------------------------ RF10 calendario

    @GetMapping(value = "/{id}/calendario.ics", produces = "text/calendar")
    public ResponseEntity<byte[]> calendario(@PathVariable UUID id) {
        Vistas.Archivo archivo = calendario.exportar(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(archivo.nombre()).build().toString())
                .contentType(MediaType.parseMediaType("text/calendar; charset=utf-8"))
                .body(archivo.contenido());
    }
}
