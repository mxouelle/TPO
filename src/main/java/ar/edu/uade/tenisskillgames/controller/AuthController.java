package ar.edu.uade.tenisskillgames.controller;

import ar.edu.uade.tenisskillgames.controller.dto.Comandos;
import ar.edu.uade.tenisskillgames.controller.dto.Vistas;
import ar.edu.uade.tenisskillgames.service.AuthService;
import ar.edu.uade.tenisskillgames.service.UsuarioService;
import ar.edu.uade.tenisskillgames.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * RF1 / CU1 / CU2: registro, verificacion y autenticacion.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    public ResponseEntity<Vistas.Registro> registrar(@Valid @RequestBody Comandos.RegistroUsuario cmd) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(cmd));
    }

    @PostMapping("/login")
    public Vistas.Auth login(@Valid @RequestBody Comandos.Login cmd) {
        return authService.login(cmd);
    }

    @PostMapping("/verificar-email")
    public Vistas.Usuario verificar(@Valid @RequestBody Comandos.VerificarEmail cmd) {
        return authService.verificarEmail(cmd.token());
    }

    /**
     * Login delegado OAuth2 (Google). En este entorno se simula el intercambio
     * del authorization code por el email verificado del proveedor.
     */
    @PostMapping("/oauth2/google")
    public Vistas.Auth loginGoogle(@RequestParam String email,
                                   @RequestParam(required = false) String nombre) {
        return authService.loginOAuth2(email, nombre);
    }

    @GetMapping("/me")
    public Vistas.Usuario me(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return usuarioService.obtener(actual.id());
    }
}
