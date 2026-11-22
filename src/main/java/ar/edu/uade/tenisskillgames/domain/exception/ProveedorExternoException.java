package ar.edu.uade.tenisskillgames.domain.exception;

/**
 * Fallo al interactuar con un proveedor externo (Push / Email / WhatsApp).
 */
public class ProveedorExternoException extends DomainException {

    public ProveedorExternoException(String message) {
        super(message);
    }

    public ProveedorExternoException(String message, Throwable cause) {
        super(message, cause);
    }
}
