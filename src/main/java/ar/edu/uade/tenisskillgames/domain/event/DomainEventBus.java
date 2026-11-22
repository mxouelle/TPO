package ar.edu.uade.tenisskillgames.domain.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Subject del patron Observer. Mantiene la lista de suscriptores y propaga
 * cada evento. Un fallo en un suscriptor no interrumpe a los demas.
 */
public class DomainEventBus implements DomainEventPublisher {

    private static final Logger LOG = Logger.getLogger(DomainEventBus.class.getName());

    private final List<DomainEventSubscriber> subscribers = new CopyOnWriteArrayList<>();

    public void subscribe(DomainEventSubscriber subscriber) {
        if (subscriber != null && !subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    public void unsubscribe(DomainEventSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            return;
        }
        for (DomainEventSubscriber subscriber : subscribers) {
            try {
                subscriber.onEvent(event);
            } catch (RuntimeException ex) {
                LOG.log(Level.WARNING, "Suscriptor " + subscriber.getClass().getSimpleName()
                        + " fallo procesando el evento " + event.getTipo(), ex);
            }
        }
    }

    public int cantidadSuscriptores() {
        return subscribers.size();
    }
}
