package ar.edu.uade.tenisskillgames.infrastructure.notification.factory;

import ar.edu.uade.tenisskillgames.domain.notification.Notifier;
import ar.edu.uade.tenisskillgames.domain.notification.NotifierFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Familia de simuladores en consola: se usa en desarrollo y en la correccion
 * academica (tenis.notificaciones.modo=dev).
 */
@Component
@ConditionalOnProperty(name = "tenis.notificaciones.modo", havingValue = "dev", matchIfMissing = true)
public class DevNotifierFactory implements NotifierFactory {

    private final Notifier push = new ConsolaNotifier("PUSH");
    private final Notifier email = new ConsolaNotifier("EMAIL");
    private final Notifier chat = new ConsolaNotifier("CHAT");

    @Override
    public Notifier createPushNotifier() {
        return push;
    }

    @Override
    public Notifier createEmailNotifier() {
        return email;
    }

    @Override
    public Notifier createChatNotifier() {
        return chat;
    }
}
