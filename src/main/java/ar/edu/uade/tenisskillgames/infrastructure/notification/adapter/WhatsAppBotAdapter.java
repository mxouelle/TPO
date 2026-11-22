package ar.edu.uade.tenisskillgames.infrastructure.notification.adapter;

import ar.edu.uade.tenisskillgames.domain.model.Notificacion;
import ar.edu.uade.tenisskillgames.domain.notification.Notifier;
import ar.edu.uade.tenisskillgames.infrastructure.notification.client.WhatsAppCloudClient;

/**
 * Adapter del patron estructural: traduce el contrato Notifier del dominio
 * a la API de WhatsApp Cloud (metodos, plantillas y formato ajenos).
 */
public class WhatsAppBotAdapter implements Notifier {

    private final WhatsAppCloudClient whatsAppApi;

    public WhatsAppBotAdapter(WhatsAppCloudClient api) {
        this.whatsAppApi = api;
    }

    @Override
    public void enviar(Notificacion n) {
        whatsAppApi.sendTemplateMessage(n.getTelefonoDestino(), "partido_update", n.getContenido());
    }
}
