package ar.edu.uade.tenisskillgames.domain.model;

import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;

import java.time.LocalDateTime;

/**
 * Registro de auditoria de una transicion de estado del partido (RF no funcional:
 * trazabilidad). Se persiste en la tabla TRANSICIONES_ESTADO.
 */
public record TransicionEstado(EstadoPartido estadoAnterior,
                               EstadoPartido estadoNuevo,
                               LocalDateTime fecha,
                               String motivo) {

    public boolean esCambio() {
        return estadoAnterior != estadoNuevo;
    }
}
