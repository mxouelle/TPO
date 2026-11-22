package ar.edu.uade.tenisskillgames.infrastructure.notification.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Simulador de WhatsApp Cloud API para desarrollo y pruebas.
 */
@Component
@ConditionalOnProperty(name = "tenis.notificaciones.modo", havingValue = "dev", matchIfMissing = true)
public class WhatsAppCloudClientSimulado implements WhatsAppCloudClient {

    private static final Logger LOG = LoggerFactory.getLogger(WhatsAppCloudClientSimulado.class);

    @Override
    public void sendTemplateMessage(String telefono, String plantilla, String... parametros) {
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El telefono destino es obligatorio para WhatsApp");
        }
        LOG.info("[WhatsApp-SIM] to={} template={} params={}", telefono, plantilla, String.join(" | ", parametros));
    }
}
