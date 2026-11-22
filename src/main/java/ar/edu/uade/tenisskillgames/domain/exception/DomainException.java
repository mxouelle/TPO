package ar.edu.uade.tenisskillgames.domain.exception;

/**
 * Excepcion raiz del dominio. No depende de Spring ni de HTTP.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
