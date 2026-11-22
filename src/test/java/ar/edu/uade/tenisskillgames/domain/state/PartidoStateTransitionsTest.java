package ar.edu.uade.tenisskillgames.domain.state;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.event.DomainEvent;
import ar.edu.uade.tenisskillgames.domain.event.DomainEventPublisher;
import ar.edu.uade.tenisskillgames.domain.event.PartidoCupoCompletoEvent;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Transiciones validas e invalidas del patron State (RF4 / CA2 / CA3).
 */
class PartidoStateTransitionsTest {

    private final List<DomainEvent> eventos = new ArrayList<>();
    private Usuario organizador;
    private Partido partido;

    @BeforeEach
    void setUp() {
        organizador = TestData.usuario("Organizador", 3.5);
        partido = TestData.partidoSingles(organizador);
        partido.setEventPublisher(eventos::add);
    }

    @Test
    @DisplayName("CA2: al aceptar el segundo jugador el estado pasa a CUPO_COMPLETO y se publica el evento")
    void cupoCompletoAutomatico() {
        Usuario rival = TestData.usuario("Rival", 4.0);

        partido.postular(rival);

        assertThat(partido.getEstado()).isEqualTo(EstadoPartido.CUPO_COMPLETO);
        assertThat(partido.getEstadoActual()).isInstanceOf(CupoCompletoState.class);
        assertThat(partido.getPlazasLibres()).isZero();
        assertThat(eventos)
                .anyMatch(e -> e instanceof PartidoCupoCompletoEvent)
                .anyMatch(e -> e.getTipo() == ar.edu.uade.tenisskillgames.domain.enums.TipoEvento.CUPO_COMPLETO);
    }

    @Test
    @DisplayName("No se puede confirmar con el cupo incompleto")
    void confirmarSinCupoCompleto() {
        assertThatThrownBy(() -> partido.confirmar(organizador))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cupo aún incompleto");
    }

    @Test
    @DisplayName("No se puede iniciar un partido no confirmado")
    void iniciarSinConfirmar() {
        assertThatThrownBy(partido::iniciar)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no está confirmado");
    }

    @Test
    @DisplayName("CA3: cuando todos confirman el estado pasa a CONFIRMADO")
    void confirmacionTotal() {
        Usuario rival = TestData.usuario("Rival", 4.0);
        partido.postular(rival);

        partido.confirmar(organizador);
        assertThat(partido.getEstado()).isEqualTo(EstadoPartido.CUPO_COMPLETO);
        assertThat(partido.confirmacionesPendientes()).isEqualTo(1);

        partido.confirmar(rival);
        assertThat(partido.getEstado()).isEqualTo(EstadoPartido.CONFIRMADO);
        assertThat(partido.getEstadoActual()).isInstanceOf(ConfirmadoState.class);
    }

    @Test
    @DisplayName("Ciclo completo: confirmar -> iniciar -> finalizar")
    void cicloCompleto() {
        Usuario rival = TestData.usuario("Rival", 3.0);
        partido.postular(rival);
        partido.confirmar(organizador);
        partido.confirmar(rival);

        partido.iniciar();
        assertThat(partido.getEstado()).isEqualTo(EstadoPartido.EN_JUEGO);

        partido.finalizar();
        assertThat(partido.getEstado()).isEqualTo(EstadoPartido.FINALIZADO);
        assertThat(partido.getHistorialEstados()).hasSize(4);
    }

    @Test
    @DisplayName("No se puede cancelar un partido en juego (operacion denegada por State)")
    void cancelarEnJuegoDenegado() {
        Usuario rival = TestData.usuario("Rival", 3.0);
        partido.postular(rival);
        partido.confirmar(organizador);
        partido.confirmar(rival);
        partido.iniciar();

        assertThatThrownBy(() -> partido.cancelar("lluvia"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya está en juego");
    }

    @Test
    @DisplayName("Cancelar antes del inicio libera los cupos")
    void cancelarAntesDelInicio() {
        partido.cancelar("Lluvia en polvo de ladrillo");

        assertThat(partido.getEstado()).isEqualTo(EstadoPartido.CANCELADO);
        assertThat(partido.getEstadoActual()).isInstanceOf(CanceladoState.class);
        assertThat(partido.getMotivoCancelacion()).contains("Lluvia");
    }

    @Test
    @DisplayName("Un jugador que llega con el cupo lleno pasa a lista de espera")
    void listaDeEspera() {
        partido.postular(TestData.usuario("Rival 1", 3.5));
        Usuario tercero = TestData.usuario("Rival 2", 4.0);

        partido.postular(tercero);

        assertThat(partido.getEstado()).isEqualTo(EstadoPartido.CUPO_COMPLETO);
        assertThat(partido.getListaEspera()).contains(tercero);
        assertThat(partido.getParticipantes()).hasSize(2);
    }

    @Test
    @DisplayName("Un estado final no admite nuevas operaciones")
    void estadoFinalBloqueaOperaciones() {
        partido.cancelar("sin cancha");

        assertThatThrownBy(() -> partido.postular(TestData.usuario("Otro", 3.5)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cancelado");
        assertThatThrownBy(partido::finalizar).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("El evento de cambio de estado refleja la transicion anterior y nueva")
    void eventoDeTransicion() {
        partido.postular(TestData.usuario("Rival", 4.0));

        assertThat(eventos)
                .filteredOn(e -> e instanceof ar.edu.uade.tenisskillgames.domain.event.PartidoEstadoCambiadoEvent)
                .anySatisfy(e -> {
                    var cambio = (ar.edu.uade.tenisskillgames.domain.event.PartidoEstadoCambiadoEvent) e;
                    assertThat(cambio.estadoAnterior()).isEqualTo(EstadoPartido.BUSCANDO_JUGADORES);
                    assertThat(cambio.estadoNuevo()).isEqualTo(EstadoPartido.CUPO_COMPLETO);
                });
    }

    @Test
    @DisplayName("La fabrica rehidrata cada estado persistido")
    void factoryRehidrataEstados() {
        for (EstadoPartido estado : EstadoPartido.values()) {
            assertThat(PartidoStateFactory.para(estado).getEstado()).isEqualTo(estado);
        }
    }

    @Test
    @DisplayName("Un partido de dobles promueve al siguiente de la lista de espera al liberarse una plaza")
    void promocionDeListaDeEspera() {
        Usuario p2 = TestData.usuario("P2", 3.0);
        Usuario p3 = TestData.usuario("P3", 3.5);
        Usuario p4 = TestData.usuario("P4", 4.0);
        Usuario espera = TestData.usuario("Espera", 3.5);

        Partido dobles = TestData.partidoDobles(organizador);
        dobles.postular(p2);
        dobles.postular(p3);
        dobles.postular(p4);
        dobles.postular(espera);

        assertThat(dobles.getEstado()).isEqualTo(EstadoPartido.CUPO_COMPLETO);
        assertThat(dobles.getListaEspera()).contains(espera);

        dobles.liberarPlaza(p2);
        var promovido = dobles.promoverListaEspera();

        assertThat(promovido).contains(espera);
        assertThat(dobles.esParticipante(espera)).isTrue();
    }
}
