package ar.edu.uade.tenisskillgames.infrastructure.notification.client;

import ar.edu.uade.tenisskillgames.domain.exception.ProveedorExternoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Implementacion real contra WhatsApp Cloud API (Meta Graph).
 * Se activa con tenis.notificaciones.modo=prod.
 */
@Component
@ConditionalOnProperty(name = "tenis.notificaciones.modo", havingValue = "prod")
public class WhatsAppCloudClientHttp implements WhatsAppCloudClient {

    private static final Logger LOG = LoggerFactory.getLogger(WhatsAppCloudClientHttp.class);

    private final RestClient restClient;
    private final String phoneNumberId;

    public WhatsAppCloudClientHttp(RestClient.Builder builder,
                                   @Value("${tenis.notificaciones.whatsapp.base-url:https://graph.facebook.com/v20.0}") String baseUrl,
                                   @Value("${tenis.notificaciones.whatsapp.phone-number-id:}") String phoneNumberId,
                                   @Value("${tenis.notificaciones.whatsapp.token:}") String token) {
        this.phoneNumberId = phoneNumberId;
        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
    }

    @Override
    public void sendTemplateMessage(String telefono, String plantilla, String... parametros) {
        try {
            Object componentes = parametros.length == 0
                    ? List.of()
                    : List.of(Map.of(
                    "type", "body",
                    "parameters", java.util.Arrays.stream(parametros)
                            .map(p -> Map.of("type", "text", "text", p == null ? "" : p))
                            .toList()));

            Map<String, Object> body = Map.of(
                    "messaging_product", "whatsapp",
                    "to", telefono,
                    "type", "template",
                    "template", Map.of(
                            "name", plantilla,
                            "language", Map.of("code", "es_AR"),
                            "components", componentes));

            restClient.post()
                    .uri("/{phoneNumberId}/messages", phoneNumberId)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            LOG.info("[WhatsApp] Plantilla {} enviada a {}", plantilla, telefono);
        } catch (RuntimeException ex) {
            throw new ProveedorExternoException("Error enviando WhatsApp a " + telefono, ex);
        }
    }
}
