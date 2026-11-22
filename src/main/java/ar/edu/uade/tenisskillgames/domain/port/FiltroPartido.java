package ar.edu.uade.tenisskillgames.domain.port;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.enums.FormatoTenis;
import ar.edu.uade.tenisskillgames.domain.enums.Superficie;
import ar.edu.uade.tenisskillgames.domain.vo.UbicacionGeo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Criterios dinamicos de busqueda de partidos (RF2).
 */
public record FiltroPartido(FormatoTenis formato,
                            Superficie superficie,
                            Double ntrpMin,
                            Double ntrpMax,
                            Integer radioKm,
                            LocalDate fecha,
                            LocalTime horaDesde,
                            LocalTime horaHasta,
                            String zona,
                            EstadoPartido estado,
                            UbicacionGeo ubicacionReferencia,
                            UUID usuarioId) {

    public boolean tieneFiltroGeografico() {
        return ubicacionReferencia != null && radioKm != null;
    }

    public static FiltroPartido vacio() {
        return new FiltroPartido(null, null, null, null, null, null, null, null, null, null, null, null);
    }
}
