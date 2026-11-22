package ar.edu.uade.tenisskillgames.infrastructure.notification.adapter;

import ar.edu.uade.tenisskillgames.domain.model.Notificacion;
import ar.edu.uade.tenisskillgames.domain.notification.Notifier;
import ar.edu.uade.tenisskillgames.infrastructure.notification.client.FirebaseClient;

/**
 * Adapter de notificaciones Push: adapta Firebase Cloud Messaging a Notifier.
 */
public class FirebasePushAdapter implements Notifier {

    private final FirebaseClient firebaseApi;

    public FirebasePushAdapter(FirebaseClient firebaseApi) {
        this.firebaseApi = firebaseApi;
    }

    @Override
    public void enviar(Notificacion n) {
        firebaseApi.sendPush(n.getPushTokenDestino(), n.getAsunto(), n.getContenido());
    }
}
