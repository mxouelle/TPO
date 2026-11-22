package ar.edu.uade.tenisskillgames.infrastructure.notification.factory;

import ar.edu.uade.tenisskillgames.domain.model.Notificacion;
import ar.edu.uade.tenisskillgames.domain.notification.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Notificador de consola usado por la familia Dev de la Abstract Factory.
 * Simula el proveedor externo y valida los datos de contacto.
 */
public class ConsolaNotifier implements Notifier {

    private static final Logger LOG = LoggerFactory.getLogger(ConsolaNotifier.class);

    private final String canal;

    public ConsolaNotifier(String canal) {
        this.canal = canal;
    }

    @Override
    public void enviar(Notificacion n) {
        if (n.getDestinatario() == null) {
            throw new IllegalStateException("La notificacion no tiene destinatario");
        }
        LOG.info("[{}] -> {} | {} | {}", canal, n.getDestinatario().getEmail(),
                n.getAsunto(), n.getContenido());
    }
}
