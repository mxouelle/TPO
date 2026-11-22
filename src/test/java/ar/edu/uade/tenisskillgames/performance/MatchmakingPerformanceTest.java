package ar.edu.uade.tenisskillgames.performance;

import ar.edu.uade.tenisskillgames.domain.enums.FormatoTenis;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.strategy.MatchmakingStrategy;
import ar.edu.uade.tenisskillgames.domain.strategy.MatchmakingStrategyRegistry;
import ar.edu.uade.tenisskillgames.domain.strategy.PorCercaniaStrategy;
import ar.edu.uade.tenisskillgames.domain.strategy.PorHistorialStrategy;
import ar.edu.uade.tenisskillgames.domain.strategy.PorNivelNTRPStrategy;
import ar.edu.uade.tenisskillgames.domain.vo.HistorialDeportivo;
import ar.edu.uade.tenisskillgames.domain.vo.UbicacionGeo;
import ar.edu.uade.tenisskillgames.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Requerimiento no funcional de rendimiento: la seleccion sobre 500
 * candidatos debe resolverse en menos de 2000 ms.
 */
class MatchmakingPerformanceTest {

    private static final int CANDIDATOS = 500;
    private static final long LIMITE_MS = 2000;

    private List<Usuario> generarCandidatos() {
        Random random = new Random(42);
        List<Usuario> candidatos = new ArrayList<>(CANDIDATOS);
        for (int i = 0; i < CANDIDATOS; i++) {
            double ntrp = 1.5 + (random.nextInt(12) * 0.5);
            Usuario u = TestData.usuario("Candidato" + i, ntrp);
            u.setUbicacion(UbicacionGeo.de(
                    TestData.LAT_CLUB + (random.nextDouble() - 0.5) * 0.4,
                    TestData.LON_CLUB + (random.nextDouble() - 0.5) * 0.4));
            u.setHistorial(new HistorialDeportivo(
                    random.nextInt(60), random.nextInt(10),
                    1 + random.nextDouble() * 4, 1 + random.nextDouble() * 4));
            candidatos.add(u);
        }
        return candidatos;
    }

    private Partido partidoDobles() {
        return TestData.builder(TestData.usuario("Organizador", 3.5), FormatoTenis.DOBLES)
                .rangoNTRP(3.0, 5.0)
                .radioMaxKm(30)
                .build();
    }

    private long medir(MatchmakingStrategy estrategia, List<Usuario> candidatos, Partido partido) {
        long inicio = System.nanoTime();
        List<Usuario> seleccionados = estrategia.seleccionarCandidatos(candidatos, partido);
        long duracionMs = (System.nanoTime() - inicio) / 1_000_000;
        assertThat(seleccionados).hasSizeLessThanOrEqualTo(partido.getCupoMaximo());
        return duracionMs;
    }

    @Test
    @DisplayName("PorNivelNTRP procesa 500 candidatos en menos de 2 segundos")
    void porNivelRendimiento() {
        List<Usuario> candidatos = generarCandidatos();
        Partido partido = partidoDobles();

        long duracion = medir(new PorNivelNTRPStrategy(), candidatos, partido);

        assertThat(duracion).isLessThan(LIMITE_MS);
        System.out.printf("[PERF] NTRP: %d candidatos en %d ms%n", CANDIDATOS, duracion);
    }

    @Test
    @DisplayName("PorCercania procesa 500 candidatos en menos de 2 segundos")
    void porCercaniaRendimiento() {
        List<Usuario> candidatos = generarCandidatos();
        Partido partido = partidoDobles();

        long duracion = medir(new PorCercaniaStrategy(), candidatos, partido);

        assertThat(duracion).isLessThan(LIMITE_MS);
        System.out.printf("[PERF] Cercania: %d candidatos en %d ms%n", CANDIDATOS, duracion);
    }

    @Test
    @DisplayName("PorHistorial procesa 500 candidatos en menos de 2 segundos")
    void porHistorialRendimiento() {
        List<Usuario> candidatos = generarCandidatos();
        Partido partido = partidoDobles();

        long duracion = medir(new PorHistorialStrategy(), candidatos, partido);

        assertThat(duracion).isLessThan(LIMITE_MS);
        System.out.printf("[PERF] Historial: %d candidatos en %d ms%n", CANDIDATOS, duracion);
    }

    @Test
    @DisplayName("El registry resuelve la estrategia sin impacto medible")
    void registryRendimiento() {
        MatchmakingStrategyRegistry registry = new MatchmakingStrategyRegistry(
                List.of(new PorNivelNTRPStrategy(), new PorCercaniaStrategy(), new PorHistorialStrategy()));
        List<Usuario> candidatos = generarCandidatos();
        Partido partido = partidoDobles();

        long inicio = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            registry.obtener("NTRP").seleccionarCandidatos(candidatos, partido);
        }
        long duracion = (System.nanoTime() - inicio) / 1_000_000;

        assertThat(duracion).isLessThan(LIMITE_MS * 2);
        System.out.printf("[PERF] 100 selecciones sobre %d candidatos en %d ms%n", CANDIDATOS, duracion);
    }
}
