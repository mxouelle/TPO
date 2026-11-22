package ar.edu.uade.tenisskillgames.domain.strategy;

import ar.edu.uade.tenisskillgames.domain.enums.GolpePreferido;
import ar.edu.uade.tenisskillgames.domain.exception.DomainValidationException;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.vo.HistorialDeportivo;
import ar.edu.uade.tenisskillgames.domain.vo.UbicacionGeo;
import ar.edu.uade.tenisskillgames.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Estrategias de emparejamiento intercambiables (patron Strategy, RF5).
 */
class MatchmakingStrategiesTest {

    private final PorNivelNTRPStrategy porNivel = new PorNivelNTRPStrategy();
    private final PorCercaniaStrategy porCercania = new PorCercaniaStrategy();
    private final PorHistorialStrategy porHistorial = new PorHistorialStrategy();

    private Partido partido;
    private Usuario organizador;

    @BeforeEach
    void setUp() {
        organizador = TestData.usuario("Organizador", 3.5);
        partido = TestData.builder(organizador, ar.edu.uade.tenisskillgames.domain.enums.FormatoTenis.SINGLES)
                .rangoNTRP(3.0, 4.5)
                .build();
    }

    private Usuario candidato(String nombre, double ntrp, double latitud, double longitud) {
        Usuario u = TestData.usuario(nombre, ntrp);
        u.setUbicacion(UbicacionGeo.de(latitud, longitud));
        return u;
    }

    @Test
    @DisplayName("Filtra fuera de la banda NTRP y ordena por cercania al promedio del partido")
    void filtraYOrdenaPorNivel() {
        List<Usuario> candidatos = List.of(
                candidato("DosCinco", 2.5, TestData.LAT_CLUB, TestData.LON_CLUB),
                candidato("TresCinco", 3.5, TestData.LAT_CLUB, TestData.LON_CLUB),
                candidato("Cuatro", 4.0, TestData.LAT_CLUB, TestData.LON_CLUB),
                candidato("Seis", 6.0, TestData.LAT_CLUB, TestData.LON_CLUB));

        List<Usuario> seleccionados = porNivel.seleccionarCandidatos(candidatos, partido);

        assertThat(seleccionados).hasSize(2);
        assertThat(seleccionados).allSatisfy(u ->
                assertThat(u.getNivelNTRP()).isBetween(3.0, 4.5));
        assertThat(seleccionados.get(0).getNivelNTRP()).isEqualTo(3.5);
        assertThat(seleccionados).noneMatch(u -> u.getNivelNTRP() == 2.5 || u.getNivelNTRP() == 6.0);
    }

    @Test
    @DisplayName("CA1: un jugador 2.5 nunca queda seleccionado para un partido 3.5-4.5")
    void ca1JugadorFueraDeCategoria() {
        Partido banda = TestData.builder(TestData.usuario("Org", 4.0),
                        ar.edu.uade.tenisskillgames.domain.enums.FormatoTenis.SINGLES)
                .rangoNTRP(3.5, 4.5)
                .build();

        List<Usuario> seleccionados = porNivel.seleccionarCandidatos(
                List.of(candidato("Fuera", 2.5, TestData.LAT_CLUB, TestData.LON_CLUB)), banda);

        assertThat(seleccionados).isEmpty();
        assertThat(banda.esCompatibleNivel(candidato("Fuera", 2.5, TestData.LAT_CLUB, TestData.LON_CLUB)))
                .isFalse();
    }

    @Test
    @DisplayName("Nunca devuelve mas candidatos que el cupo maximo del partido")
    void limitadoAlCupo() {
        List<Usuario> candidatos = List.of(
                candidato("A", 3.0, TestData.LAT_CLUB, TestData.LON_CLUB),
                candidato("B", 3.5, TestData.LAT_CLUB, TestData.LON_CLUB),
                candidato("C", 4.0, TestData.LAT_CLUB, TestData.LON_CLUB),
                candidato("D", 4.5, TestData.LAT_CLUB, TestData.LON_CLUB));

        assertThat(porNivel.seleccionarCandidatos(candidatos, partido)).hasSize(2);
    }

    @Test
    @DisplayName("Cercania descarta candidatos fuera del radio del club")
    void cercaniaDescartaFueraDeRadio() {
        // ~30 km al norte del club
        Usuario lejos = candidato("Lejos", 3.5, TestData.LAT_CLUB + 0.27, TestData.LON_CLUB);
        Usuario cerca = candidato("Cerca", 3.5, TestData.LAT_CLUB + 0.01, TestData.LON_CLUB);

        List<Usuario> seleccionados = porCercania.seleccionarCandidatos(List.of(lejos, cerca), partido);

        assertThat(seleccionados).containsExactly(cerca);
        assertThat(partido.distanciaAlClubKm(lejos)).isGreaterThan(partido.getRadioMaxKm());
    }

    @Test
    @DisplayName("Cercania ordena por distancia ascendente")
    void cercaniaOrdenaPorDistancia() {
        Usuario medio = candidato("Medio", 3.5, TestData.LAT_CLUB + 0.05, TestData.LON_CLUB);
        Usuario cerca = candidato("Cerca", 3.5, TestData.LAT_CLUB + 0.005, TestData.LON_CLUB);

        List<Usuario> seleccionados = porCercania.seleccionarCandidatos(List.of(medio, cerca), partido);

        assertThat(seleccionados).containsExactly(cerca, medio);
    }

    @Test
    @DisplayName("Historial prioriza asistencia, fair play y complementariedad de perfil")
    void historialPriorizaBuenComportamiento() {
        Usuario conflictivo = TestData.usuario("Conflictivo", 3.5);
        conflictivo.setHistorial(new HistorialDeportivo(10, 8, 1.5, 1.0));

        Usuario confiable = TestData.usuario("Confiable", 3.5);
        confiable.setHistorial(new HistorialDeportivo(40, 1, 4.9, 4.8));
        confiable.setGolpePreferido(GolpePreferido.REVES);

        List<Usuario> seleccionados = porHistorial.seleccionarCandidatos(
                List.of(conflictivo, confiable), partido);

        assertThat(seleccionados).first().isEqualTo(confiable);
        assertThat(porHistorial.puntaje(confiable, partido))
                .isGreaterThan(porHistorial.puntaje(conflictivo, partido));
    }

    @Test
    @DisplayName("El registry resuelve las tres estrategias por codigo")
    void registryResuelveEstrategias() {
        MatchmakingStrategyRegistry registry = new MatchmakingStrategyRegistry(
                List.of(porNivel, porCercania, porHistorial));

        assertThat(registry.obtener("NTRP")).isInstanceOf(PorNivelNTRPStrategy.class);
        assertThat(registry.obtener("cercania")).isInstanceOf(PorCercaniaStrategy.class);
        assertThat(registry.obtener(ar.edu.uade.tenisskillgames.domain.enums.EstrategiaEmparejamiento.HISTORIAL))
                .isInstanceOf(PorHistorialStrategy.class);
        assertThatThrownBy(() -> registry.obtener("INEXISTENTE"))
                .isInstanceOf(DomainValidationException.class);
        assertThat(registry.disponibles()).hasSize(3);
    }

    @Test
    @DisplayName("Un codigo de estrategia invalido lanza validacion de dominio")
    void codigoInvalido() {
        MatchmakingStrategyRegistry registry = new MatchmakingStrategyRegistry(List.of(porNivel));
        assertThatThrownBy(() -> registry.obtener("OTRA"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("no soportada");
    }
}
