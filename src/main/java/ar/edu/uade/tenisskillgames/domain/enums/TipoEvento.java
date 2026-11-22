package ar.edu.uade.tenisskillgames.domain.enums;

/**
 * Tipos de evento de dominio publicados en el bus (CU10).
 */
public enum TipoEvento {

    PARTIDO_CREADO,
    CUPO_COMPLETO,
    PARTIDO_CONFIRMADO,
    PARTIDO_INICIADO,
    PARTIDO_FINALIZADO,
    PARTIDO_CANCELADO,
    RESULTADO_CARGADO,
    REPORTE_CONFIRMADO,
    CUENTA_VERIFICADA,
    RECORDATORIO
}
