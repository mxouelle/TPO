package ar.edu.uade.tenisskillgames.domain.exception;

/**
 * Violacion de una invariante de negocio (rango NTRP, fecha pasada, cupo, etc.).
 */
public class DomainValidationException extends DomainException {

    public DomainValidationException(String message) {
        super(message);
    }
}
