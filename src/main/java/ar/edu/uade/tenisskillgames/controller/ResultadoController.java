package ar.edu.uade.tenisskillgames.controller;

import ar.edu.uade.tenisskillgames.controller.dto.Comandos;
import ar.edu.uade.tenisskillgames.controller.dto.Vistas;
import ar.edu.uade.tenisskillgames.service.ModeracionService;
import ar.edu.uade.tenisskillgames.service.ResultadoService;
import ar.edu.uade.tenisskillgames.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * CU8 / CU11: marcador por sets, evaluaciones de fair play y reportes de conducta.
 */
@RestController
@RequestMapping("/api/partidos")
public class ResultadoController {

    private final ResultadoService resultados;
    private final ModeracionService moderacion;

    public ResultadoController(ResultadoService resultados, ModeracionService moderacion) {
        this.resultados = resultados;
        this.moderacion = moderacion;
    }

    @PostMapping("/{id}/resultados")
    public Vistas.Partido cargarResultado(@PathVariable UUID id,
                                          @AuthenticationPrincipal UsuarioAutenticado actual,
                                          @Valid @RequestBody Comandos.Resultado cmd) {
        return resultados.cargarResultado(id, actual.id(), cmd);
    }

    @GetMapping("/{id}/resultados")
    public Vistas.Partido obtenerResultado(@PathVariable UUID id) {
        return resultados.obtenerResultado(id);
    }

    @PostMapping("/{id}/evaluaciones")
    public ResponseEntity<Vistas.EvaluacionVista> evaluar(@PathVariable UUID id,
                                                          @AuthenticationPrincipal UsuarioAutenticado actual,
                                                          @Valid @RequestBody Comandos.Evaluacion cmd) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resultados.evaluar(id, actual.id(), cmd));
    }

    @PostMapping("/{id}/reportes")
    public ResponseEntity<Vistas.ReporteVista> reportar(@PathVariable UUID id,
                                                        @AuthenticationPrincipal UsuarioAutenticado actual,
                                                        @Valid @RequestBody Comandos.Reporte cmd) {
        return ResponseEntity.status(HttpStatus.CREATED).body(moderacion.reportar(id, actual.id(), cmd));
    }
}
