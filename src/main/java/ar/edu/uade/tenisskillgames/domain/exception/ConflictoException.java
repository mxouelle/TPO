package ar.edu.uade.tenisskillgames.domain.exception;

/**
 * Conflicto de negocio: email duplicado, inscripcion repetida, superposicion horaria.
 */
public class ConflictoException extends DomainException {

    public ConflictoException(String message) {
        super(message);
    }
}
