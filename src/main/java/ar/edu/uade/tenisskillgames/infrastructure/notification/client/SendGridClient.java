package ar.edu.uade.tenisskillgames.infrastructure.notification.client;

/**
 * Cliente SMTP / SendGrid de bajo nivel.
 */
public interface SendGridClient {

    void sendEmail(String destinatario, String asunto, String cuerpoHtml, String adjuntoNombre,
                   String adjuntoContenido, String adjuntoMimeType);
}
