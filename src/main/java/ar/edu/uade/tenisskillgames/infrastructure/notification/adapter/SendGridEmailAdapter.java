package ar.edu.uade.tenisskillgames.infrastructure.notification.adapter;

import ar.edu.uade.tenisskillgames.domain.model.Notificacion;
import ar.edu.uade.tenisskillgames.domain.notification.Notifier;
import ar.edu.uade.tenisskillgames.infrastructure.notification.client.SendGridClient;

/**
 * Adapter de correo electronico: adapta SendGrid/SMTP al contrato Notifier.
 */
public class SendGridEmailAdapter implements Notifier {

    private final SendGridClient sendGridApi;

    public SendGridEmailAdapter(SendGridClient sendGridApi) {
        this.sendGridApi = sendGridApi;
    }

    @Override
    public void enviar(Notificacion n) {
        sendGridApi.sendEmail(
                n.getEmailDestino(),
                n.getAsunto(),
                n.getContenido(),
                n.getAdjuntoNombre(),
                n.getAdjuntoContenido(),
                n.getAdjuntoNombre() == null ? null : "text/calendar");
    }
}
