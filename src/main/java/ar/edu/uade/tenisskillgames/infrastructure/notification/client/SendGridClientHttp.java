package ar.edu.uade.tenisskillgames.infrastructure.notification.client;

import ar.edu.uade.tenisskillgames.domain.exception.ProveedorExternoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Implementacion real contra SendGrid v3 (Mail Send API).
 * Se activa con tenis.notificaciones.modo=prod.
 */
@Component
@ConditionalOnProperty(name = "tenis.notificaciones.modo", havingValue = "prod")
public class SendGridClientHttp implements SendGridClient {

    private static final Logger LOG = LoggerFactory.getLogger(SendGridClientHttp.class);

    private final RestClient restClient;
    private final String remitente;

    public SendGridClientHttp(RestClient.Builder builder,
                              @Value("${tenis.notificaciones.sendgrid.base-url:https://api.sendgrid.com/v3}") String baseUrl,
                              @Value("${tenis.notificaciones.sendgrid.api-key:}") String apiKey,
                              @Value("${tenis.notificaciones.sendgrid.remitente:no-reply@tenisskillgames.ar}") String remitente) {
        this.remitente = remitente;
        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    @Override
    public void sendEmail(String destinatario, String asunto, String cuerpoHtml,
                          String adjuntoNombre, String adjuntoContenido, String adjuntoMimeType) {
        try {
            Map<String, Object> body = new java.util.HashMap<>(Map.of(
                    "personalizations", List.of(Map.of("to", List.of(Map.of("email", destinatario)))),
                    "from", Map.of("email", remitente, "name", "TenisSkillGames"),
                    "subject", asunto,
                    "content", List.of(Map.of("type", "text/html", "value", cuerpoHtml == null ? "" : cuerpoHtml))));

            if (adjuntoContenido != null) {
                body.put("attachments", List.of(Map.of(
                        "content", Base64.getEncoder().encodeToString(adjuntoContenido.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                        "filename", adjuntoNombre,
                        "type", adjuntoMimeType == null ? "text/calendar" : adjuntoMimeType,
                        "disposition", "attachment")));
            }

            restClient.post().uri("/mail/send").body(body).retrieve().toBodilessEntity();
            LOG.info("[SendGrid] Email enviado a {}", destinatario);
        } catch (RuntimeException ex) {
            throw new ProveedorExternoException("Error enviando email a " + destinatario, ex);
        }
    }
}
