package ar.edu.uade.tenisskillgames.domain.event;

/**
 * Contrato minimo que el agregado usa para publicar eventos sin conocer
 * la implementacion (Spring Events, cola, bus en memoria).
 */
@FunctionalInterface
public interface DomainEventPublisher {

    void publish(DomainEvent event);

    DomainEventPublisher NO_OP = event -> {
        // Sin bus configurado (por ejemplo en tests unitarios puros)
    };
}
