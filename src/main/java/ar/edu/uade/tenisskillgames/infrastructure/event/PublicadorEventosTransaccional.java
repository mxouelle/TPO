package ar.edu.uade.tenisskillgames.infrastructure.event;

import ar.edu.uade.tenisskillgames.domain.event.DomainEvent;
import ar.edu.uade.tenisskillgames.domain.event.DomainEventBus;
import ar.edu.uade.tenisskillgames.domain.event.DomainEventPublisher;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Publica los eventos de dominio despues del commit. Evita que un suscriptor
 * lea datos no confirmados y agrupa todos los eventos de la transaccion en
 * un unico lote (despacho asincrono desacoplado).
 */
@Component
public class PublicadorEventosTransaccional implements DomainEventPublisher {

    private static final ThreadLocal<List<DomainEvent>> BUFFER = ThreadLocal.withInitial(ArrayList::new);

    private final ObjectProvider<DomainEventBus> busProvider;

    public PublicadorEventosTransaccional(ObjectProvider<DomainEventBus> busProvider) {
        this.busProvider = busProvider;
    }

    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            bus().publish(event);
            return;
        }

        boolean primerEvento = BUFFER.get().isEmpty();
        BUFFER.get().add(event);

        if (primerEvento) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    List<DomainEvent> eventos = new ArrayList<>(BUFFER.get());
                    BUFFER.remove();
                    if (status == STATUS_COMMITTED) {
                        eventos.forEach(e -> bus().publish(e));
                    }
                }
            });
        }
    }

    private DomainEventBus bus() {
        return busProvider.getObject();
    }
}
