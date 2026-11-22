package ar.edu.uade.tenisskillgames.domain.builder;

import ar.edu.uade.tenisskillgames.domain.enums.FormatoTenis;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.enums.Superficie;
import ar.edu.uade.tenisskillgames.domain.exception.DomainValidationException;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Invariantes de construccion del patron Builder (RF3 / CU3).
 */
class PartidoBuilderTest {

    private final Usuario organizador = TestData.usuario("Nico Mendez", 3.5);

    @Test
    @DisplayName("Singles fija el cupo en 2 jugadores y nace en BUSCANDO_JUGADORES")
    void singlesFijaCupoDos() {
        Partido partido = TestData.builder(organizador, FormatoTenis.SINGLES).build();

        assertThat(partido.getCupoMaximo()).isEqualTo(2);
        assertThat(partido.getEstado()).isEqualTo(EstadoPartido.BUSCANDO_JUGADORES);
        assertThat(partido.getParticipantes()).hasSize(1);
        assertThat(partido.getOrganizador()).isEqualTo(organizador);
    }

    @Test
    @DisplayName("Dobles fija el cupo en 4 jugadores")
    void doblesFijaCupoCuatro() {
        Partido partido = TestData.builder(organizador, FormatoTenis.DOBLES).build();

        assertThat(partido.getCupoMaximo()).isEqualTo(4);
        assertThat(partido.getPlazasLibres()).isEqualTo(3);
    }

    @Test
    @DisplayName("Un rango NTRP fuera de 1.5-7.0 o invertido lanza excepcion de validacion")
    void rangoNtrpInvalido() {
        assertThatThrownBy(() -> TestData.builder(organizador, FormatoTenis.SINGLES).rangoNTRP(0.5, 4.0))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Rango NTRP inválido");

        assertThatThrownBy(() -> TestData.builder(organizador, FormatoTenis.SINGLES).rangoNTRP(5.0, 3.0))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("Una fecha pasada no permite construir el partido")
    void fechaPasadaInvalida() {
        assertThatThrownBy(() -> new PartidoBuilder()
                .organizador(organizador)
                .formato(FormatoTenis.SINGLES)
                .rangoNTRP(3.0, 4.0)
                .sedeYFecha("Club", Superficie.RAPIDA_CEMENTO, LocalDateTime.now().minusDays(1)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Fecha debe ser futura");
    }

    @Test
    @DisplayName("Sin organizador el build falla")
    void organizadorObligatorio() {
        PartidoBuilder builder = new PartidoBuilder()
                .formato(FormatoTenis.SINGLES)
                .rangoNTRP(3.0, 4.0)
                .sedeYFecha("Club", Superficie.RAPIDA_CEMENTO, LocalDateTime.now().plusDays(1));

        assertThatThrownBy(builder::build)
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Organizador obligatorio");
    }

    @Test
    @DisplayName("El creador queda inscripto automaticamente al construir el partido")
    void creadorAutoInscripto() {
        Partido partido = TestData.partidoSingles(organizador);

        assertThat(partido.esParticipante(organizador)).isTrue();
        assertThat(partido.getPostulaciones())
                .anySatisfy(postulacion -> {
                    assertThat(postulacion.getUsuario()).isEqualTo(organizador);
                    assertThat(postulacion.getEstado())
                            .isEqualTo(ar.edu.uade.tenisskillgames.domain.enums.EstadoPostulacion.ACEPTADA);
                });
    }

    @Test
    @DisplayName("La duracion estimada se valida entre 30 y 300 minutos")
    void duracionFueraDeRango() {
        assertThatThrownBy(() -> TestData.builder(organizador, FormatoTenis.SINGLES).duracionMinutos(10))
                .isInstanceOf(DomainValidationException.class);
    }
}
