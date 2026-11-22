package ar.edu.uade.tenisskillgames.infrastructure.notification.client;

import ar.edu.uade.tenisskillgames.domain.exception.ProveedorExternoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Implementacion real de Firebase Cloud Messaging (HTTP v1).
 * Se activa con tenis.notificaciones.modo=prod.
 */
@Component
@ConditionalOnProperty(name = "tenis.notificaciones.modo", havingValue = "prod")
public class FirebaseClientHttp implements FirebaseClient {

    private static final Logger LOG = LoggerFactory.getLogger(FirebaseClientHttp.class);

    private final RestClient restClient;
    private final String proyecto;

    public FirebaseClientHttp(RestClient.Builder builder,
                              @Value("${tenis.notificaciones.firebase.base-url:https://fcm.googleapis.com}") String baseUrl,
                              @Value("${tenis.notificaciones.firebase.proyecto:}") String proyecto,
                              @Value("${tenis.notificaciones.firebase.token:}") String token) {
        this.proyecto = proyecto;
        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
    }

    @Override
    public void sendPush(String token, String titulo, String cuerpo) {
        if (token == null || token.isBlank()) {
            throw new ProveedorExternoException("El dispositivo no tiene push token registrado");
        }
        try {
            Map<String, Object> body = Map.of(
                    "message", Map.of(
                            "token", token,
                            "notification", Map.of("title", titulo == null ? "" : titulo,
                                    "body", cuerpo == null ? "" : cuerpo)));
            restClient.post()
                    .uri("/v1/projects/{proyecto}/messages:send", proyecto)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            LOG.info("[Firebase] Push enviado ({} chars)", cuerpo == null ? 0 : cuerpo.length());
        } catch (RuntimeException ex) {
            throw new ProveedorExternoException("Error enviando push", ex);
        }
    }
}
