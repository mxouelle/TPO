package ar.edu.uade.tenisskillgames.controller;

import ar.edu.uade.tenisskillgames.controller.dto.Comandos;
import ar.edu.uade.tenisskillgames.controller.dto.Vistas;
import ar.edu.uade.tenisskillgames.service.BusquedaFavoritaService;
import ar.edu.uade.tenisskillgames.service.NotificacionService;
import ar.edu.uade.tenisskillgames.service.PartidoService;
import ar.edu.uade.tenisskillgames.service.ResultadoService;
import ar.edu.uade.tenisskillgames.service.UsuarioService;
import ar.edu.uade.tenisskillgames.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Perfil tenistico, notificaciones, historial, reputacion y busquedas favoritas.
 */
@RestController
@RequestMapping("/api/usuarios")
public class PerfilController {

    private final UsuarioService usuarios;
    private final PartidoService partidos;
    private final NotificacionService notificaciones;
    private final ResultadoService resultados;
    private final BusquedaFavoritaService busquedas;

    public PerfilController(UsuarioService usuarios, PartidoService partidos,
                            NotificacionService notificaciones, ResultadoService resultados,
                            BusquedaFavoritaService busquedas) {
        this.usuarios = usuarios;
        this.partidos = partidos;
        this.notificaciones = notificaciones;
        this.resultados = resultados;
        this.busquedas = busquedas;
    }

    @GetMapping("/me")
    public Vistas.Usuario me(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return usuarios.obtener(actual.id());
    }

    @PutMapping("/me")
    public Vistas.Usuario actualizar(@AuthenticationPrincipal UsuarioAutenticado actual,
                                     @Valid @RequestBody Comandos.ActualizarPerfil cmd) {
        return usuarios.actualizarPerfil(actual.id(), cmd);
    }

    @GetMapping("/{id}")
    public Vistas.Usuario obtener(@PathVariable UUID id) {
        return usuarios.obtener(id);
    }

    @PostMapping("/me/organizador")
    public Vistas.Usuario convertirEnOrganizador(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return usuarios.convertirEnOrganizador(actual.id());
    }

    @GetMapping("/me/partidos")
    public List<Vistas.Partido> misPartidos(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return partidos.deJugador(actual.id());
    }

    @GetMapping("/me/notificaciones")
    public List<Vistas.Notificacion> misNotificaciones(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return notificaciones.listarPorUsuario(actual.id());
    }

    @GetMapping("/me/evaluaciones")
    public List<Vistas.EvaluacionVista> misEvaluaciones(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return resultados.evaluacionesDe(actual.id());
    }

    @GetMapping("/{id}/evaluaciones")
    public List<Vistas.EvaluacionVista> evaluacionesDe(@PathVariable UUID id) {
        return resultados.evaluacionesDe(id);
    }

    @GetMapping("/me/busquedas")
    public List<Vistas.Busqueda> misBusquedas(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return busquedas.listar(actual.id());
    }

    @PostMapping("/me/busquedas")
    public ResponseEntity<Vistas.Busqueda> crearBusqueda(@AuthenticationPrincipal UsuarioAutenticado actual,
                                                         @Valid @RequestBody Comandos.BusquedaFavorita cmd) {
        return ResponseEntity.status(HttpStatus.CREATED).body(busquedas.crear(actual.id(), cmd));
    }

    @DeleteMapping("/me/busquedas/{id}")
    public ResponseEntity<Void> eliminarBusqueda(@AuthenticationPrincipal UsuarioAutenticado actual,
                                                 @PathVariable UUID id) {
        busquedas.eliminar(actual.id(), id);
        return ResponseEntity.noContent().build();
    }
}
