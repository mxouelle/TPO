package ar.edu.uade.tenisskillgames.domain.model;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoReporte;
import ar.edu.uade.tenisskillgames.domain.enums.MotivoReporte;
import ar.edu.uade.tenisskillgames.domain.exception.DomainValidationException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Reporte de no-show o conducta antideportiva (RF9 / CU11).
 */
public class ReporteConducta {

    private UUID id;
    private Partido partido;
    private Usuario reportante;
    private Usuario reportado;
    private MotivoReporte motivo;
    private String descripcion;
    private EstadoReporte estado;
    private LocalDateTime fechaReporte;
    private String resolucion;

    public ReporteConducta() {
        this.id = UUID.randomUUID();
        this.estado = EstadoReporte.PENDIENTE;
        this.fechaReporte = LocalDateTime.now();
    }

    public ReporteConducta(Partido partido, Usuario reportante, Usuario reportado,
                           MotivoReporte motivo, String descripcion) {
        this();
        if (reportante.equals(reportado)) {
            throw new DomainValidationException("Un jugador no puede reportarse a si mismo");
        }
        this.partido = partido;
        this.reportante = reportante;
        this.reportado = reportado;
        this.motivo = motivo;
        this.descripcion = descripcion;
    }

    public void confirmar(String resolucion) {
        this.estado = EstadoReporte.CONFIRMADO;
        this.resolucion = resolucion;
    }

    public void desestimar(String resolucion) {
        this.estado = EstadoReporte.RECHAZADO;
        this.resolucion = resolucion;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Partido getPartido() {
        return partido;
    }

    public void setPartido(Partido partido) {
        this.partido = partido;
    }

    public Usuario getReportante() {
        return reportante;
    }

    public void setReportante(Usuario reportante) {
        this.reportante = reportante;
    }

    public Usuario getReportado() {
        return reportado;
    }

    public void setReportado(Usuario reportado) {
        this.reportado = reportado;
    }

    public MotivoReporte getMotivo() {
        return motivo;
    }

    public void setMotivo(MotivoReporte motivo) {
        this.motivo = motivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public EstadoReporte getEstado() {
        return estado;
    }

    public void setEstado(EstadoReporte estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaReporte() {
        return fechaReporte;
    }

    public void setFechaReporte(LocalDateTime fechaReporte) {
        this.fechaReporte = fechaReporte;
    }

    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReporteConducta that)) {
            return false;
        }
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
