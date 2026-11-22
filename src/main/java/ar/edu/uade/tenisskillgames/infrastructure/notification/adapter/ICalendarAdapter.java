package ar.edu.uade.tenisskillgames.infrastructure.notification.adapter;

import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.port.CalendarioPort;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Adapter de calendario: genera eventos RFC 5545 (.ics) compatibles con
 * Google Calendar y Apple Calendar (RF10).
 */
@Component
public class ICalendarAdapter implements CalendarioPort {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.ROOT);
    private static final String CRLF = "\r\n";

    /**
     * Genera el contenido iCalendar del encuentro confirmado.
     */
    public String generarArchivoICS(Partido p) {
        ZonedDateTime inicio = p.getFechaHora().atZone(ZoneId.of("America/Argentina/Buenos_Aires"));
        ZonedDateTime fin = inicio.plusMinutes(p.getDuracionMinutos());
        ZonedDateTime ahora = ZonedDateTime.now(ZoneId.of("UTC"));

        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR").append(CRLF);
        sb.append("VERSION:2.0").append(CRLF);
        sb.append("PRODID:-//TenisSkillGames//UADE//ES").append(CRLF);
        sb.append("CALSCALE:GREGORIAN").append(CRLF);
        sb.append("METHOD:PUBLISH").append(CRLF);
        sb.append("BEGIN:VEVENT").append(CRLF);
        sb.append("UID:").append(p.getId()).append("@tenisskillgames.uade.ar").append(CRLF);
        sb.append("DTSTAMP:").append(ahora.withZoneSameInstant(ZoneId.of("UTC")).format(FORMATO)).append(CRLF);
        sb.append("DTSTART:").append(inicio.withZoneSameInstant(ZoneId.of("UTC")).format(FORMATO)).append(CRLF);
        sb.append("DTEND:").append(fin.withZoneSameInstant(ZoneId.of("UTC")).format(FORMATO)).append(CRLF);
        sb.append("SUMMARY:").append(escapar("Tenis " + p.getFormato() + " en " + p.getClubSede())).append(CRLF);
        sb.append("LOCATION:").append(escapar(p.getClubSede() + " - " + texto(p.getDireccion()))).append(CRLF);
        sb.append("DESCRIPTION:").append(escapar(descripcion(p))).append(CRLF);
        sb.append("STATUS:CONFIRMED").append(CRLF);
        sb.append("END:VEVENT").append(CRLF);
        sb.append("END:VCALENDAR").append(CRLF);
        return sb.toString();
    }

    @Override
    public byte[] generar(Partido partido) {
        return generarArchivoICS(partido).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String nombreArchivo(Partido partido) {
        return "partido-" + partido.getId() + ".ics";
    }

    private String descripcion(Partido p) {
        return "Partido " + p.getFormato()
                + " | Superficie: " + p.getSuperficie()
                + " | Nivel NTRP " + p.getNtrpMin() + "-" + p.getNtrpMax()
                + " | Modalidad: " + p.getModalidad()
                + " | Organiza: " + (p.getOrganizador() == null ? "-" : p.getOrganizador().getNombre());
    }

    private String texto(String valor) {
        return valor == null ? "" : valor;
    }

    /**
     * Escapa los caracteres especiales definidos por la RFC 5545.
     */
    private String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n");
    }
}
