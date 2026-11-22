package ar.edu.uade.tenisskillgames.infrastructure.config;

import ar.edu.uade.tenisskillgames.controller.dto.Comandos;
import ar.edu.uade.tenisskillgames.service.AuthService;
import ar.edu.uade.tenisskillgames.service.PartidoService;
import ar.edu.uade.tenisskillgames.service.PostulacionService;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoVerificacion;
import ar.edu.uade.tenisskillgames.domain.enums.FormatoTenis;
import ar.edu.uade.tenisskillgames.domain.enums.GolpePreferido;
import ar.edu.uade.tenisskillgames.domain.enums.ManoHabil;
import ar.edu.uade.tenisskillgames.domain.enums.Modalidad;
import ar.edu.uade.tenisskillgames.domain.enums.Rol;
import ar.edu.uade.tenisskillgames.domain.enums.Superficie;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.port.UsuarioRepository;
import ar.edu.uade.tenisskillgames.domain.vo.UbicacionGeo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Datos de demostracion para la defensa del TPO (tenis.demo.datos=true).
 * Crea un administrador, cuatro tenistas verificados y un partido abierto.
 */
@Component
@ConditionalOnProperty(name = "tenis.demo.datos", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final double LAT = -34.5453;
    private static final double LON = -58.4498;

    private final UsuarioRepository usuarios;
    private final AuthService authService;
    private final PartidoService partidos;
    private final PostulacionService postulaciones;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(UsuarioRepository usuarios, AuthService authService, PartidoService partidos,
                          PostulacionService postulaciones, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.authService = authService;
        this.partidos = partidos;
        this.postulaciones = postulaciones;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!usuarios.listar().isEmpty()) {
            return;
        }

        Usuario admin = crear("Admin UADE", "admin@tenisskillgames.ar", "Admin1234", 5.0, Rol.ADMIN);
        Usuario nico = crear("Nicolás Méndez", "nico@tenisskillgames.ar", "Password123", 3.5, Rol.ORGANIZADOR);
        Usuario ana = crear("Ana Gómez", "ana@tenisskillgames.ar", "Password123", 4.0, Rol.JUGADOR);
        Usuario bruno = crear("Bruno Díaz", "bruno@tenisskillgames.ar", "Password123", 3.0, Rol.JUGADOR);
        crear("Carla Ruiz", "carla@tenisskillgames.ar", "Password123", 4.5, Rol.JUGADOR);

        var partido = partidos.crear(nico.getId(), new Comandos.CrearPartido(
                FormatoTenis.SINGLES, Superficie.POLVO_DE_LADRILLO, "Club Náutico Núñez",
                "Av. del Libertador 8500", LAT, LON, 3.0, 4.5,
                LocalDateTime.now().plusDays(2).withHour(19).withMinute(0).withSecond(0).withNano(0),
                90, Modalidad.AMISTOSO_RECREATIVO, 15, "NTRP",
                "Partido de demostración del TPO"));

        // Singles completo: queda a la espera de las confirmaciones
        postulaciones.postular(partido.id(), ana.getId(), null);

        // Dobles abierto: sirve para mostrar búsqueda, emparejamiento y postulación
        var dobles = partidos.crear(nico.getId(), new Comandos.CrearPartido(
                FormatoTenis.DOBLES, Superficie.CESPED_SINTETICO, "Club Atlético San Isidro",
                "Av. Centenario 1200", -34.4707, -58.5123, 3.0, 4.0,
                LocalDateTime.now().plusDays(3).withHour(20).withMinute(0).withSecond(0).withNano(0),
                120, Modalidad.AMISTOSO_RECREATIVO, 20, "HISTORIAL",
                "Dobles abiertos para la demo"));

        postulaciones.postular(dobles.id(), bruno.getId(), null);
        postulaciones.postular(dobles.id(), ana.getId(), null);

        LOG.info("Datos demo listos. Admin: admin@tenisskillgames.ar / Admin1234 | "
                + "Jugador: nico@tenisskillgames.ar / Password123");
        LOG.info("Partido singles (CUPO_COMPLETO): {} | Partido dobles (BUSCANDO_JUGADORES): {}",
                partido.id(), dobles.id());
        LOG.info("Usuario admin id={}", admin.getId());
    }

    private Usuario crear(String nombre, String email, String password, double ntrp, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setNivelNTRP(ntrp);
        usuario.setZona("Núñez");
        usuario.setManoHabil(ManoHabil.DIESTRO);
        usuario.setGolpePreferido(rol == Rol.ADMIN ? GolpePreferido.AMBOS : GolpePreferido.DRIVE);
        usuario.setSuperficieFavorita(Superficie.POLVO_DE_LADRILLO);
        usuario.setUbicacion(UbicacionGeo.de(LAT, LON));
        usuario.setTelefono("+54911555" + Math.abs(email.hashCode() % 10000));
        usuario.setPushToken("demo-push-" + Math.abs(email.hashCode()));
        usuario.setFranjasHorarias(List.of("LUN_18-22", "MIE_18-22", "SAB_09-13"));
        usuario.setEstadoVerificacion(EstadoVerificacion.VERIFICADO);
        usuario.setRoles(java.util.EnumSet.of(Rol.JUGADOR, rol));
        return usuarios.guardar(usuario);
    }
}
