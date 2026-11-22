package ar.edu.uade.tenisskillgames.infrastructure.notification.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Simulador de SendGrid para desarrollo y pruebas.
 */
@Component
@ConditionalOnProperty(name = "tenis.notificaciones.modo", havingValue = "dev", matchIfMissing = true)
public class SendGridClientSimulado implements SendGridClient {

    private static final Logger LOG = LoggerFactory.getLogger(SendGridClientSimulado.class);

    @Override
    public void sendEmail(String destinatario, String asunto, String cuerpoHtml,
                          String adjuntoNombre, String adjuntoContenido, String adjuntoMimeType) {
        if (destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException("El email destino es obligatorio");
        }
        LOG.info("[Email-SIM] to={} subject={} adjunto={} ({} bytes)", destinatario, asunto,
                adjuntoNombre, adjuntoContenido == null ? 0 : adjuntoContenido.length());
    }
}
