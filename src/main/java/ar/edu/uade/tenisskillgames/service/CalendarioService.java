package ar.edu.uade.tenisskillgames.service;

import ar.edu.uade.tenisskillgames.controller.dto.Vistas;
import ar.edu.uade.tenisskillgames.domain.enums.EstadoPartido;
import ar.edu.uade.tenisskillgames.domain.exception.OperacionNoPermitidaException;
import ar.edu.uade.tenisskillgames.domain.exception.RecursoNoEncontradoException;
import ar.edu.uade.tenisskillgames.domain.model.Partido;
import ar.edu.uade.tenisskillgames.domain.port.CalendarioPort;
import ar.edu.uade.tenisskillgames.domain.port.PartidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso RF10: exportacion del encuentro confirmado en formato iCal.
 */
@Service
public class CalendarioService {

    private final PartidoRepository partidos;
    private final CalendarioPort calendario;

    public CalendarioService(PartidoRepository partidos, CalendarioPort calendario) {
        this.partidos = partidos;
        this.calendario = calendario;
    }

    @Transactional(readOnly = true)
    public Vistas.Archivo exportar(UUID partidoId) {
        Partido partido = partidos.buscarPorId(partidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Partido no encontrado: " + partidoId));

        if (partido.getEstado() == EstadoPartido.BUSCANDO_JUGADORES
                || partido.getEstado() == EstadoPartido.CUPO_COMPLETO
                || partido.getEstado() == EstadoPartido.CANCELADO) {
            throw new OperacionNoPermitidaException(
                    "La invitación de calendario se habilita cuando el encuentro está confirmado. Estado actual: "
                            + partido.getEstado());
        }

        return new Vistas.Archivo(calendario.nombreArchivo(partido), calendario.generar(partido));
    }
}
