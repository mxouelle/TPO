package ar.edu.uade.tenisskillgames.service;

import ar.edu.uade.tenisskillgames.controller.dto.Comandos;
import ar.edu.uade.tenisskillgames.controller.dto.Vistas;
import ar.edu.uade.tenisskillgames.controller.mapper.VistaMapper;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoVerificacion;
import ar.edu.uade.tenisskillgames.domain.enums.GolpePreferido;
import ar.edu.uade.tenisskillgames.domain.enums.ManoHabil;
import ar.edu.uade.tenisskillgames.domain.enums.Superficie;
import ar.edu.uade.tenisskillgames.domain.enums.TipoEvento;
import ar.edu.uade.tenisskillgames.domain.exception.ConflictoException;
import ar.edu.uade.tenisskillgames.domain.exception.CredencialesInvalidasException;
import ar.edu.uade.tenisskillgames.domain.exception.RecursoNoEncontradoException;
import ar.edu.uade.tenisskillgames.domain.exception.UsuarioSuspendidoException;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.port.UsuarioRepository;
import ar.edu.uade.tenisskillgames.domain.vo.UbicacionGeo;
import ar.edu.uade.tenisskillgames.infrastructure.security.ControlIntentosFallidos;
import ar.edu.uade.tenisskillgames.infrastructure.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Casos de uso CU1 (registro) y CU2 (autenticacion).
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ControlIntentosFallidos intentosFallidos;
    private final NotificacionService notificaciones;
    private final VistaMapper mapper;

    public AuthService(UsuarioRepository usuarios,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       ControlIntentosFallidos intentosFallidos,
                       NotificacionService notificaciones,
                       VistaMapper mapper) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.intentosFallidos = intentosFallidos;
        this.notificaciones = notificaciones;
        this.mapper = mapper;
    }

    @Transactional
    public Vistas.Registro registrar(Comandos.RegistroUsuario cmd) {
        if (usuarios.existeEmail(cmd.email())) {
            throw new ConflictoException("El email " + cmd.email() + " ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(cmd.nombre());
        usuario.setEmail(cmd.email());
        usuario.setPasswordHash(passwordEncoder.encode(cmd.password()));
        usuario.setNivelNTRP(cmd.nivelNTRP());
        usuario.setZona(cmd.zona());
        usuario.setSuperficieFavorita(cmd.superficieFavorita() == null ? Superficie.POLVO_DE_LADRILLO : cmd.superficieFavorita());
        usuario.setManoHabil(cmd.manoHabil() == null ? ManoHabil.DIESTRO : cmd.manoHabil());
        usuario.setGolpePreferido(cmd.golpePreferido() == null ? GolpePreferido.AMBOS : cmd.golpePreferido());
        usuario.setUbicacion(UbicacionGeo.de(cmd.latitud(), cmd.longitud()));
        usuario.setTelefono(cmd.telefono());
        usuario.setPushToken(cmd.pushToken());
        usuario.setFranjasHorarias(cmd.franjasHorarias());
        usuario.setEstadoVerificacion(EstadoVerificacion.PENDIENTE);

        Usuario guardado = usuarios.guardar(usuario);
        String token = jwtService.generarTokenVerificacion(guardado);

        notificaciones.enviar(guardado, null, TipoEvento.CUENTA_VERIFICADA,
                "Verificá tu cuenta de TenisSkillGames",
                "Hola " + guardado.getNombre() + ", confirmá tu cuenta con este token: " + token);

        return new Vistas.Registro(mapper.usuario(guardado), token,
                "Cuenta creada en estado PENDIENTE. Se despachó el correo de verificación.");
    }

    @Transactional(readOnly = true)
    public Vistas.Auth login(Comandos.Login cmd) {
        intentosFallidos.validarNoBloqueado(cmd.email());

        Usuario usuario = usuarios.buscarPorEmail(cmd.email())
                .orElseThrow(() -> {
                    intentosFallidos.registrarFallo(cmd.email());
                    return new CredencialesInvalidasException("Credenciales inválidas");
                });

        if (!passwordEncoder.matches(cmd.password(), usuario.getPasswordHash())) {
            intentosFallidos.registrarFallo(cmd.email());
            throw new CredencialesInvalidasException("Credenciales inválidas");
        }
        if (usuario.estaSuspendido()) {
            throw new UsuarioSuspendidoException(
                    "Cuenta suspendida por acumulación de strikes hasta " + usuario.getRehabilitacion(),
                    usuario.getRehabilitacion());
        }

        intentosFallidos.limpiar(cmd.email());
        String token = jwtService.generarToken(usuario);
        return new Vistas.Auth(token, "Bearer", jwtService.getExpiracionSegundos(), mapper.usuario(usuario));
    }

    @Transactional
    public Vistas.Usuario verificarEmail(String token) {
        UUID usuarioId = jwtService.validarTokenVerificacion(token);
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario inexistente"));
        usuario.verificarCuenta();
        return mapper.usuario(usuarios.guardar(usuario));
    }

    /**
     * Alta delegada OAuth2 (Google): el email se considera verificado.
     */
    @Transactional
    public Vistas.Auth loginOAuth2(String email, String nombre) {
        Usuario usuario = usuarios.buscarPorEmail(email).orElseGet(() -> {
            Usuario nuevo = new Usuario();
            nuevo.setNombre(nombre == null || nombre.isBlank() ? email : nombre);
            nuevo.setEmail(email);
            nuevo.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            nuevo.setNivelNTRP(3.0);
            nuevo.setZona("Sin definir");
            nuevo.setUbicacion(UbicacionGeo.de(0, 0));
            nuevo.verificarCuenta();
            return usuarios.guardar(nuevo);
        });
        usuario.verificarCuenta();
        usuarios.guardar(usuario);
        String token = jwtService.generarToken(usuario);
        return new Vistas.Auth(token, "Bearer", jwtService.getExpiracionSegundos(), mapper.usuario(usuario));
    }
}
