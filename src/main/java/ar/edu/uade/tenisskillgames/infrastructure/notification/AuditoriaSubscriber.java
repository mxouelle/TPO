package ar.edu.uade.tenisskillgames.infrastructure.notification;

import ar.edu.uade.tenisskillgames.domain.event.DomainEvent;
import ar.edu.uade.tenisskillgames.domain.event.DomainEventSubscriber;
import ar.edu.uade.tenisskillgames.domain.event.PartidoEstadoCambiadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observador de auditoria: deja traza en el log de cada transicion de estado.
 */
@Component
public class AuditoriaSubscriber implements DomainEventSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(AuditoriaSubscriber.class);

    @Override
    public void onEvent(DomainEvent evento) {
        if (evento instanceof PartidoEstadoCambiadoEvent cambio) {
            LOG.info("[AUDITORIA] partido {} {} -> {} ({})", cambio.getPartidoId(),
                    cambio.estadoAnterior(), cambio.estadoNuevo(), cambio.getOcurridoEn());
        } else {
            LOG.info("[AUDITORIA] evento {} del partido {}", evento.getTipo(), evento.getPartidoId());
        }
    }
}
