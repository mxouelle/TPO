package ar.edu.uade.tenisskillgames.integration;

import ar.edu.uade.tenisskillgames.domain.enums.CanalNotificacion;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoEnvio;
import ar.edu.uade.tenisskillgames.domain.enums.TipoEvento;
import ar.edu.uade.tenisskillgames.domain.model.Notificacion;
import ar.edu.uade.tenisskillgames.domain.notification.Notifier;
import ar.edu.uade.tenisskillgames.domain.notification.NotifierFactory;
import ar.edu.uade.tenisskillgames.domain.port.NotificacionRepository;
import ar.edu.uade.tenisskillgames.infrastructure.notification.factory.ConsolaNotifier;
import ar.edu.uade.tenisskillgames.infrastructure.notification.factory.DevNotifierFactory;
import ar.edu.uade.tenisskillgames.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica la Abstract Factory en el contexto Spring real: la familia Dev
 * se activa con tenis.notificaciones.modo=dev y produce notificadores
 * coherentes entre si.
 */
@SpringBootTest
@ActiveProfiles("test")
class NotifierFactoryIntegrationTest {

    @Autowired
    private NotifierFactory notifierFactory;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Test
    @DisplayName("El contexto resuelve la familia Dev de notificadores")
    void familiaDevActiva() {
        assertThat(notifierFactory).isInstanceOf(DevNotifierFactory.class);
        assertThat(notifierFactory.createPushNotifier()).isInstanceOf(ConsolaNotifier.class);
        assertThat(notifierFactory.createEmailNotifier()).isInstanceOf(ConsolaNotifier.class);
        assertThat(notifierFactory.createChatNotifier()).isInstanceOf(ConsolaNotifier.class);
        assertThat(notifierFactory.createEmailNotifier())
                .isNotSameAs(notifierFactory.createChatNotifier());
    }

    @Test
    @DisplayName("Cada notificador envia sin lanzar excepciones y la notificacion queda persistida")
    void envioExitosoPorCadaCanal() {
        var usuario = TestData.usuario("Notificado", 3.5);

        Notificacion notificacion = new Notificacion(usuario, UUID.randomUUID(),
                TipoEvento.CUPO_COMPLETO, CanalNotificacion.EMAIL,
                "Asunto de prueba", "Contenido de prueba");
        notificacionRepository.guardar(notificacion);
        notificacion.marcarEnviada();
        notificacionRepository.guardar(notificacion);

        assertThat(notificacionRepository.listarPorUsuario(usuario.getId()))
                .anySatisfy(n -> {
                    assertThat(n.getCanal()).isEqualTo(CanalNotificacion.EMAIL);
                    assertThat(n.getEstadoEnvio()).isEqualTo(EstadoEnvio.ENVIADO);
                });
    }

    @Test
    @DisplayName("Un notificador falla si el destinatario no existe (fallback registrado)")
    void notificadorValidaDestinatario() {
        Notifier notifier = notifierFactory.createEmailNotifier();
        Notificacion sinDestinatario = new Notificacion(null, null, TipoEvento.PARTIDO_FINALIZADO,
                CanalNotificacion.EMAIL, "x", "y");

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> notifier.enviar(sinDestinatario))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("destinatario");
    }
}
