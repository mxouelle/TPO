package ar.edu.uade.tenisskillgames.infrastructure.notification;

import ar.edu.uade.tenisskillgames.domain.enums.CanalNotificacion;
import ar.edu.uade.tenisskillgames.domain.enums.TipoEvento;
import ar.edu.uade.tenisskillgames.domain.model.Notificacion;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import ar.edu.uade.tenisskillgames.infrastructure.notification.adapter.ICalendarAdapter;
import ar.edu.uade.tenisskillgames.infrastructure.notification.adapter.WhatsAppBotAdapter;
import ar.edu.uade.tenisskillgames.infrastructure.notification.client.WhatsAppCloudClient;
import ar.edu.uade.tenisskillgames.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Verifica los adaptadores estructurales (Adapter): traduccion de la API
 * externa al contrato Notifier y generacion de iCalendar.
 */
class AdapterTest {

    @Test
    @DisplayName("WhatsAppBotAdapter traduce Notificacion a la plantilla de WhatsApp Cloud")
    void whatsAppAdapterTraduceMensaje() {
        WhatsAppCloudClient cliente = mock(WhatsAppCloudClient.class);
        WhatsAppBotAdapter adapter = new WhatsAppBotAdapter(cliente);

        Usuario usuario = TestData.usuario("Tester", 3.5);
        usuario.setTelefono("+5491155550000");
        Notificacion notificacion = new Notificacion(usuario, UUID.randomUUID(),
                TipoEvento.CUPO_COMPLETO, CanalNotificacion.CHAT,
                "Cupo completo", "Confirmá tu asistencia");

        adapter.enviar(notificacion);

        verify(cliente).sendTemplateMessage(eq("+5491155550000"), eq("partido_update"),
                eq("Confirmá tu asistencia"));
    }

    @Test
    @DisplayName("ICalendarAdapter genera un evento RFC 5545 valido con CRLF")
    void iCalendarGeneraEventoValido() {
        Partido partido = TestData.partidoSingles(TestData.usuario("Organizador", 3.5));
        ICalendarAdapter adapter = new ICalendarAdapter();

        String ics = adapter.generarArchivoICS(partido);

        assertThat(ics).startsWith("BEGIN:VCALENDAR\r\n");
        assertThat(ics).contains("VERSION:2.0");
        assertThat(ics).contains("PRODID:-//TenisSkillGames//UADE//ES");
        assertThat(ics).contains("BEGIN:VEVENT");
        assertThat(ics).contains("DTSTART:");
        assertThat(ics).contains("DTEND:");
        assertThat(ics).contains("SUMMARY:Tenis SINGLES en Club Náutico Núñez");
        assertThat(ics).contains("UID:" + partido.getId());
        assertThat(ics).endsWith("END:VCALENDAR\r\n");
        assertThat(adapter.nombreArchivo(partido)).endsWith(".ics");
        assertThat(new String(adapter.generar(partido), java.nio.charset.StandardCharsets.UTF_8))
                .isEqualTo(ics);
    }

    @Test
    @DisplayName("Los caracteres especiales de iCalendar se escapan segun la RFC")
    void iCalendarEscapaCaracteres() {
        Usuario organizador = TestData.usuario("Organizador", 3.5);
        Partido partido = new ar.edu.uade.tenisskillgames.domain.builder.PartidoBuilder()
                .organizador(organizador)
                .formato(ar.edu.uade.tenisskillgames.domain.enums.FormatoTenis.SINGLES)
                .rangoNTRP(3.0, 4.0)
                .sedeYFecha("Club A, B; C", ar.edu.uade.tenisskillgames.domain.enums.Superficie.RAPIDA_CEMENTO,
                        java.time.LocalDateTime.now().plusDays(2))
                .build();

        String ics = new ICalendarAdapter().generarArchivoICS(partido);

        assertThat(ics).contains("Club A\\, B\\; C");
    }

    @Test
    @DisplayName("El adaptador de email valida que exista destinatario")
    void emailRequiereDestinatario() {
        ICalendarAdapter ical = new ICalendarAdapter();
        ar.edu.uade.tenisskillgames.infrastructure.notification.client.SendGridClient cliente =
                mock(ar.edu.uade.tenisskillgames.infrastructure.notification.client.SendGridClient.class);
        var adapter = new ar.edu.uade.tenisskillgames.infrastructure.notification.adapter.SendGridEmailAdapter(cliente);

        Usuario usuario = TestData.usuario("SinTelefono", 3.0);
        Notificacion notificacion = new Notificacion(usuario, null, TipoEvento.PARTIDO_FINALIZADO,
                CanalNotificacion.EMAIL, "Asunto", "<p>cuerpo</p>");

        adapter.enviar(notificacion);

        verify(cliente).sendEmail(eq(usuario.getEmail()), eq("Asunto"), anyString(), eq(null), eq(null), eq(null));
        assertThat(ical).isNotNull();
    }
}
