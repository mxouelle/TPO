package ar.edu.uade.tenisskillgames.domain.notification;

import ar.edu.uade.tenisskillgames.domain.model.Notificacion;

/**
 * Contrato comun de envio. Los adaptadores concretos (WhatsApp, SendGrid,
 * Firebase) implementan esta interfaz para desacoplar el dominio de cada SDK.
 */
public interface Notifier {

    void enviar(Notificacion notificacion);
}
