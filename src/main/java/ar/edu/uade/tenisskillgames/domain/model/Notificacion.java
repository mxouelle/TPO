package ar.edu.uade.tenisskillgames.domain.model;

import ar.edu.uade.tenisskillgames.domain.enums.CanalNotificacion;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoEnvio;
import ar.edu.uade.tenisskillgames.domain.enums.TipoEvento;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Notificacion emitida a un destinatario por un canal (RF7 / CU10).
 */
public class Notificacion {

    private UUID id;
    private Usuario destinatario;
    private UUID partidoId;
    private TipoEvento tipoEvento;
    private CanalNotificacion canal;
    private String contenido;
    private String asunto;
    private EstadoEnvio estadoEnvio;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEnvio;
    private int intentos;
    private String ultimoError;
    private String adjuntoNombre;
    private String adjuntoContenido;

    public Notificacion() {
        this.id = UUID.randomUUID();
        this.estadoEnvio = EstadoEnvio.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Notificacion(Usuario destinatario, UUID partidoId, TipoEvento tipoEvento,
                        CanalNotificacion canal, String asunto, String contenido) {
        this();
        this.destinatario = destinatario;
        this.partidoId = partidoId;
        this.tipoEvento = tipoEvento;
        this.canal = canal;
        this.asunto = asunto;
        this.contenido = contenido;
    }

    public void marcarEnviada() {
        this.estadoEnvio = EstadoEnvio.ENVIADO;
        this.fechaEnvio = LocalDateTime.now();
        this.ultimoError = null;
    }

    public void marcarFallida(String error) {
        this.estadoEnvio = EstadoEnvio.FALLIDO;
        this.fechaEnvio = LocalDateTime.now();
        this.ultimoError = error;
    }

    public void registrarIntento() {
        this.intentos++;
    }

    // ------------------------------------------------------- datos de contacto del destinatario

    public String getTelefonoDestino() {
        return destinatario == null ? null : destinatario.getTelefono();
    }

    public String getEmailDestino() {
        return destinatario == null ? null : destinatario.getEmail();
    }

    public String getPushTokenDestino() {
        return destinatario == null ? null : destinatario.getPushToken();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Usuario getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(Usuario destinatario) {
        this.destinatario = destinatario;
    }

    public UUID getPartidoId() {
        return partidoId;
    }

    public void setPartidoId(UUID partidoId) {
        this.partidoId = partidoId;
    }

    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(TipoEvento tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public CanalNotificacion getCanal() {
        return canal;
    }

    public void setCanal(CanalNotificacion canal) {
        this.canal = canal;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public EstadoEnvio getEstadoEnvio() {
        return estadoEnvio;
    }

    public void setEstadoEnvio(EstadoEnvio estadoEnvio) {
        this.estadoEnvio = estadoEnvio;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public int getIntentos() {
        return intentos;
    }

    public void setIntentos(int intentos) {
        this.intentos = intentos;
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public void setUltimoError(String ultimoError) {
        this.ultimoError = ultimoError;
    }

    public String getAdjuntoNombre() {
        return adjuntoNombre;
    }

    public void setAdjuntoNombre(String adjuntoNombre) {
        this.adjuntoNombre = adjuntoNombre;
    }

    public String getAdjuntoContenido() {
        return adjuntoContenido;
    }

    public void setAdjuntoContenido(String adjuntoContenido) {
        this.adjuntoContenido = adjuntoContenido;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Notificacion that)) {
            return false;
        }
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
