package ar.edu.uade.tenisskillgames.domain.event;

/**
 * Observador de eventos de dominio.
 */
@FunctionalInterface
public interface DomainEventSubscriber {

    void onEvent(DomainEvent event);
}
