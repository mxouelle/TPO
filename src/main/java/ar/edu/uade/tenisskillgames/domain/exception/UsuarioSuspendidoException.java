package ar.edu.uade.tenisskillgames.domain.exception;

import java.time.LocalDate;

/**
 * Cuenta con suspension vigente por acumulacion de strikes (RF9).
 */
public class UsuarioSuspendidoException extends DomainException {

    private final LocalDate rehabilitacion;

    public UsuarioSuspendidoException(String message, LocalDate rehabilitacion) {
        super(message);
        this.rehabilitacion = rehabilitacion;
    }

    public LocalDate getRehabilitacion() {
        return rehabilitacion;
    }
}
