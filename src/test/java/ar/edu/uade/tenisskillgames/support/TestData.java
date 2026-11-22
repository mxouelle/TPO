package ar.edu.uade.tenisskillgames.support;

import ar.edu.uade.tenisskillgames.domain.builder.PartidoBuilder;
import ar.edu.uade.tenisskillgames.domain.enums.FormatoTenis;
import ar.edu.uade.tenisskillgames.domain.enums.GolpePreferido;
import ar.edu.uade.tenisskillgames.domain.enums.ManoHabil;
import ar.edu.uade.tenisskillgames.domain.enums.Modalidad;
import ar.edu.uade.tenisskillgames.domain.enums.Superficie;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.domain.vo.UbicacionGeo;

import java.time.LocalDateTime;

/**
 * Fabrica de datos para las pruebas.
 */
public final class TestData {

    public static final double LAT_CLUB = -34.5453;
    public static final double LON_CLUB = -58.4498;

    private TestData() {
    }

    public static Usuario usuario(String nombre, double ntrp) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(nombre.toLowerCase().replace(" ", ".") + "@test.com");
        u.setPasswordHash("$2a$10$hashdeprueba");
        u.setNivelNTRP(ntrp);
        u.setManoHabil(ManoHabil.DIESTRO);
        u.setGolpePreferido(GolpePreferido.DRIVE);
        u.setSuperficieFavorita(Superficie.POLVO_DE_LADRILLO);
        u.setZona("Nunez");
        u.setUbicacion(UbicacionGeo.de(LAT_CLUB, LON_CLUB));
        u.verificarCuenta();
        return u;
    }

    public static Usuario usuarioVerificado(String nombre, String email, double ntrp) {
        Usuario u = usuario(nombre, ntrp);
        u.setEmail(email);
        return u;
    }

    public static Partido partidoSingles(Usuario organizador) {
        return builder(organizador, FormatoTenis.SINGLES).build();
    }

    public static Partido partidoDobles(Usuario organizador) {
        return builder(organizador, FormatoTenis.DOBLES).build();
    }

    public static PartidoBuilder builder(Usuario organizador, FormatoTenis formato) {
        return new PartidoBuilder()
                .organizador(organizador)
                .formato(formato)
                .rangoNTRP(3.0, 4.5)
                .sedeYFecha("Club Náutico Núñez", Superficie.POLVO_DE_LADRILLO,
                        LocalDateTime.now().plusDays(3).withHour(19).withMinute(0).withSecond(0).withNano(0))
                .direccion("Av. del Libertador 8500")
                .ubicacion(LAT_CLUB, LON_CLUB)
                .duracionMinutos(90)
                .radioMaxKm(15)
                .modalidad(Modalidad.AMISTOSO_RECREATIVO);
    }
}
