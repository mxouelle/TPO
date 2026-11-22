package ar.edu.uade.tenisskillgames.controller;

import ar.edu.uade.tenisskillgames.controller.dto.Comandos;
import ar.edu.uade.tenisskillgames.controller.dto.Vistas;
import ar.edu.uade.tenisskillgames.service.ModeracionService;
import ar.edu.uade.tenisskillgames.service.NotificacionService;
import ar.edu.uade.tenisskillgames.service.PartidoService;
import ar.edu.uade.tenisskillgames.service.UsuarioService;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Panel de administracion: estadisticas, moderacion y auditoria de envios.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UsuarioService usuarios;
    private final PartidoService partidos;
    private final NotificacionService notificaciones;
    private final ModeracionService moderacion;

    public AdminController(UsuarioService usuarios, PartidoService partidos,
                           NotificacionService notificaciones, ModeracionService moderacion) {
        this.usuarios = usuarios;
        this.partidos = partidos;
        this.notificaciones = notificaciones;
        this.moderacion = moderacion;
    }

    @GetMapping("/usuarios")
    public List<Vistas.Usuario> usuarios() {
        return usuarios.listar();
    }

    @GetMapping("/notificaciones")
    public List<Vistas.Notificacion> notificaciones(@RequestParam(defaultValue = "50") int limite) {
        return notificaciones.ultimas(limite);
    }

    @GetMapping("/reportes")
    public List<Vistas.ReporteVista> reportesPendientes() {
        return moderacion.pendientes();
    }

    @GetMapping("/reportes/usuario/{id}")
    public List<Vistas.ReporteVista> reportesDe(@PathVariable UUID id) {
        return moderacion.deUsuario(id);
    }

    @PostMapping("/reportes/{id}/resolver")
    public Vistas.ReporteVista resolver(@PathVariable UUID id,
                                        @Valid @RequestBody Comandos.ResolucionReporte cmd) {
        return moderacion.resolver(id, cmd);
    }

    @GetMapping("/estadisticas")
    public Vistas.Estadisticas estadisticas() {
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoPartido estado : EstadoPartido.values()) {
            porEstado.put(estado.name(), (long) partidos.buscar(
                    new ar.edu.uade.tenisskillgames.domain.port.FiltroPartido(null, null, null, null,
                            null, null, null, null, null, estado, null, null)).size());
        }
        return new Vistas.Estadisticas(
                usuarios.listar().size(),
                partidos.buscar(new ar.edu.uade.tenisskillgames.domain.port.FiltroPartido(null, null,
                        null, null, null, null, null, null, null, null, null, null)).size(),
                notificaciones.enviadas(),
                notificaciones.fallidas(),
                porEstado);
    }
}
