package ar.edu.uade.tenisskillgames.infrastructure.notification.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Simulador de Firebase Cloud Messaging para desarrollo y pruebas.
 */
@Component
@ConditionalOnProperty(name = "tenis.notificaciones.modo", havingValue = "dev", matchIfMissing = true)
public class FirebaseClientSimulado implements FirebaseClient {

    private static final Logger LOG = LoggerFactory.getLogger(FirebaseClientSimulado.class);

    @Override
    public void sendPush(String token, String titulo, String cuerpo) {
        LOG.info("[Push-SIM] token={} titulo={} cuerpo={}", token, titulo, cuerpo);
    }
}
