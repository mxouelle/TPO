package ar.edu.uade.tenisskillgames.integration;

import ar.edu.uade.tenisskillgames.controller.dto.Comandos;
import ar.edu.uade.tenisskillgames.controller.dto.Vistas;
import ar.edu.uade.tenisskillgames.service.AuthService;
import ar.edu.uade.tenisskillgames.service.CalendarioService;
import ar.edu.uade.tenisskillgames.service.ConfirmacionService;
import ar.edu.uade.tenisskillgames.service.NotificacionService;
import ar.edu.uade.tenisskillgames.service.PartidoService;
import ar.edu.uade.tenisskillgames.service.PostulacionService;
import ar.edu.uade.tenisskillgames.service.ResultadoService;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.enums.FormatoTenis;
import ar.edu.uade.tenisskillgames.domain.enums.GolpePreferido;
import ar.edu.uade.tenisskillgames.domain.enums.Lado;
import ar.edu.uade.tenisskillgames.domain.enums.ManoHabil;
import ar.edu.uade.tenisskillgames.domain.enums.Modalidad;
import ar.edu.uade.tenisskillgames.domain.enums.Superficie;
import ar.edu.uade.tenisskillgames.domain.port.PartidoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Flujo completo end-to-end con persistencia H2:
 * registrar -> crear -> postular -> confirmar -> iniciar -> finalizar -> resultado.
 */
@SpringBootTest
@ActiveProfiles("test")
class PartidoLifecycleIntegrationTest {

    private static final double LAT = -34.5453;
    private static final double LON = -58.4498;

    @Autowired
    private AuthService authService;
    @Autowired
    private PartidoService partidoService;
    @Autowired
    private PostulacionService postulacionService;
    @Autowired
    private ConfirmacionService confirmacionService;
    @Autowired
    private ResultadoService resultadoService;
    @Autowired
    private CalendarioService calendarioService;
    @Autowired
    private NotificacionService notificacionService;
    @Autowired
    private PartidoRepository partidoRepository;

    private Vistas.Usuario registrar(String nombre, String email, double ntrp) {
        Vistas.Registro registro = authService.registrar(new Comandos.RegistroUsuario(
                nombre, email, "Password123", ntrp, "Núñez",
                Superficie.POLVO_DE_LADRILLO, ManoHabil.DIESTRO, GolpePreferido.DRIVE,
                LAT, LON, "+5491155" + Math.abs(email.hashCode()),
                "push-" + email, List.of("LUN_18-22", "SAB_09-13")));

        assertThat(registro.usuario().estadoVerificacion()).isEqualTo("PENDIENTE");
        return authService.verificarEmail(registro.tokenVerificacion());
    }

    @Test
    @DisplayName("Ciclo de vida completo del partido con eventos, notificaciones y auditoria")
    void cicloDeVidaCompleto() {
        Vistas.Usuario organizador = registrar("Nico Mendez", "nico.it@test.com", 3.5);
        Vistas.Usuario rival = registrar("Ana Gomez", "ana.it@test.com", 4.0);

        Vistas.Partido creado = partidoService.crear(organizador.id(), new Comandos.CrearPartido(
                FormatoTenis.SINGLES, Superficie.POLVO_DE_LADRILLO, "Club Náutico Núñez",
                "Av. del Libertador 8500", LAT, LON, 3.0, 4.5,
                LocalDateTime.now().plusDays(2).withHour(19).withMinute(0).withSecond(0).withNano(0),
                90, Modalidad.AMISTOSO_RECREATIVO, 15, "NTRP", "Partido de prueba E2E"));

        assertThat(creado.estado()).isEqualTo(EstadoPartido.BUSCANDO_JUGADORES);
        assertThat(creado.plazasLibres()).isEqualTo(1);

        // CA2: el segundo jugador completa el cupo
        Vistas.Partido conCupo = postulacionService.postular(creado.id(), rival.id(), Lado.B);
        assertThat(conCupo.estado()).isEqualTo(EstadoPartido.CUPO_COMPLETO);
        assertThat(conCupo.plazasLibres()).isZero();

        // CA3: confirmacion total
        confirmacionService.registrar(creado.id(), organizador.id(), true, null);
        Vistas.Partido confirmado = confirmacionService.registrar(creado.id(), rival.id(), true, null);
        assertThat(confirmado.estado()).isEqualTo(EstadoPartido.CONFIRMADO);

        // RF10: invitacion iCal habilitada
        String ics = new String(calendarioService.exportar(creado.id()).contenido(),
                java.nio.charset.StandardCharsets.UTF_8);
        assertThat(ics).contains("BEGIN:VCALENDAR").contains("SUMMARY:Tenis SINGLES");

        // CU7: el scheduler inicia al llegar la hora pactada (se simula el paso del tiempo)
        var entidad = partidoRepository.buscarPorId(creado.id()).orElseThrow();
        entidad.setFechaHora(LocalDateTime.now().minusMinutes(10));
        partidoRepository.guardar(entidad);

        Vistas.Partido enJuego = partidoService.iniciar(creado.id(), organizador.id());
        assertThat(enJuego.estado()).isEqualTo(EstadoPartido.EN_JUEGO);

        // CU8: cierre y carga del marcador por sets
        Vistas.Partido finalizado = partidoService.finalizar(creado.id(), organizador.id());
        assertThat(finalizado.estado()).isEqualTo(EstadoPartido.FINALIZADO);

        Vistas.Partido conResultado = resultadoService.cargarResultado(creado.id(), organizador.id(),
                new Comandos.Resultado(Lado.A,
                        List.of(new Comandos.SetComando(6, 4), new Comandos.SetComando(7, 6)),
                        105, "Cancha 3, polvo de ladrillo en buen estado"));

        assertThat(conResultado.resultado()).isNotNull();
        assertThat(conResultado.resultado().marcador()).isEqualTo("6-4 7-6");
        assertThat(conResultado.resultado().ganadorLado()).isEqualTo(Lado.A);
        assertThat(conResultado.historialEstados())
                .extracting(Vistas.TransicionVista::estadoNuevo)
                .contains(EstadoPartido.CUPO_COMPLETO, EstadoPartido.CONFIRMADO,
                        EstadoPartido.EN_JUEGO, EstadoPartido.FINALIZADO);

        // RF8: evaluacion de fair play
        Vistas.EvaluacionVista evaluacion = resultadoService.evaluar(creado.id(), rival.id(),
                new Comandos.Evaluacion(organizador.id(), 5, 4, "Muy buen partido"));
        assertThat(evaluacion.fairPlay()).isEqualTo(5);

        // RF7: se persistieron notificaciones multicanal para el rival
        assertThat(notificacionService.listarPorUsuario(rival.id())).isNotEmpty();
        assertThat(notificacionService.listarPorPartido(creado.id())).isNotEmpty();
        assertThat(notificacionService.enviadas()).isGreaterThan(0);

        // RNF: auditoria de transiciones persistida
        assertThat(partidoRepository.buscarPorId(creado.id()).orElseThrow().getHistorialEstados())
                .hasSizeGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("Un jugador fuera de la banda NTRP no puede postularse (CA1)")
    void postulacionRechazadaPorCategoria() {
        Vistas.Usuario organizador = registrar("Organizador CA1", "org.ca1@test.com", 4.0);
        Vistas.Usuario principiante = registrar("Principiante CA1", "pri.ca1@test.com", 2.5);

        Vistas.Partido partido = partidoService.crear(organizador.id(), new Comandos.CrearPartido(
                FormatoTenis.SINGLES, Superficie.RAPIDA_CEMENTO, "Club CA1",
                "Calle 1", LAT, LON, 3.5, 4.5,
                LocalDateTime.now().plusDays(4).withHour(18).withMinute(0),
                60, Modalidad.DESAFIO_POR_PUNTOS, 10, "NTRP", null));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> postulacionService.postular(partido.id(), principiante.id(), Lado.INDIFERENTE))
                .isInstanceOf(ar.edu.uade.tenisskillgames.domain.exception.DomainValidationException.class)
                .hasMessageContaining("Incompatibilidad de categoría");
    }

    @Test
    @DisplayName("Un partido sin quorum se cancela automaticamente al llegar la hora")
    void cancelacionPorFaltaDeQuorum() {
        Vistas.Usuario organizador = registrar("Organizador Quorum", "org.quorum@test.com", 3.5);

        Vistas.Partido partido = partidoService.crear(organizador.id(), new Comandos.CrearPartido(
                FormatoTenis.SINGLES, Superficie.CESPED_SINTETICO, "Club Quorum",
                "Calle 2", LAT, LON, 3.0, 4.0,
                LocalDateTime.now().plusDays(5).withHour(20).withMinute(0),
                60, Modalidad.AMISTOSO_RECREATIVO, 10, "NTRP", null));

        var entidad = partidoRepository.buscarPorId(partido.id()).orElseThrow();
        entidad.setFechaHora(LocalDateTime.now().minusMinutes(5));
        partidoRepository.guardar(entidad);

        ar.edu.uade.tenisskillgames.infrastructure.scheduler.PartidoScheduler scheduler =
                new ar.edu.uade.tenisskillgames.infrastructure.scheduler.PartidoScheduler(
                        partidoRepository, notificacionService);
        scheduler.cancelarSinQuorum(LocalDateTime.now());

        assertThat(partidoRepository.buscarPorId(partido.id()).orElseThrow().getEstado())
                .isEqualTo(EstadoPartido.CANCELADO);
    }
}
