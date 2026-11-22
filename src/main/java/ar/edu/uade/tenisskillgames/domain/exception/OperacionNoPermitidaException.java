package ar.edu.uade.tenisskillgames.domain.exception;

/**
 * Operacion rechazada por el estado actual del agregado o por falta de permisos.
 */
public class OperacionNoPermitidaException extends DomainException {

    public OperacionNoPermitidaException(String message) {
        super(message);
    }
}
