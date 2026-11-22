package ar.edu.uade.tenisskillgames.infrastructure.notification.factory;

import ar.edu.uade.tenisskillgames.domain.notification.Notifier;
import ar.edu.uade.tenisskillgames.domain.notification.NotifierFactory;
import ar.edu.uade.tenisskillgames.infrastructure.notification.adapter.FirebasePushAdapter;
import ar.edu.uade.tenisskillgames.infrastructure.notification.adapter.SendGridEmailAdapter;
import ar.edu.uade.tenisskillgames.infrastructure.notification.adapter.WhatsAppBotAdapter;
import ar.edu.uade.tenisskillgames.infrastructure.notification.client.FirebaseClient;
import ar.edu.uade.tenisskillgames.infrastructure.notification.client.SendGridClient;
import ar.edu.uade.tenisskillgames.infrastructure.notification.client.WhatsAppCloudClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Familia de adaptadores reales: Firebase + SendGrid + WhatsApp Cloud.
 * Se activa con tenis.notificaciones.modo=prod.
 */
@Component
@ConditionalOnProperty(name = "tenis.notificaciones.modo", havingValue = "prod")
public class ProdNotifierFactory implements NotifierFactory {

    private final Notifier push;
    private final Notifier email;
    private final Notifier chat;

    public ProdNotifierFactory(FirebaseClient firebase, SendGridClient sendGrid, WhatsAppCloudClient whatsApp) {
        this.push = new FirebasePushAdapter(firebase);
        this.email = new SendGridEmailAdapter(sendGrid);
        this.chat = new WhatsAppBotAdapter(whatsApp);
    }

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
