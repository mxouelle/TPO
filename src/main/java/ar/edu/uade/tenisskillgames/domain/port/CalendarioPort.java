package ar.edu.uade.tenisskillgames.domain.port;

import ar.edu.uade.tenisskillgames.domain.model.Partido;

/**
 * Puerto de exportacion de calendario (implementado por ICalendarAdapter).
 */
public interface CalendarioPort {

    byte[] generar(Partido partido);

    String nombreArchivo(Partido partido);
}
