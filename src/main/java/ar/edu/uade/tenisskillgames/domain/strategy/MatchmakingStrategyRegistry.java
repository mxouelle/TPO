package ar.edu.uade.tenisskillgames.domain.strategy;

import ar.edu.uade.tenisskillgames.domain.enums.EstrategiaEmparejamiento;
import ar.edu.uade.tenisskillgames.domain.exception.DomainValidationException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resuelve la estrategia concreta a partir del codigo recibido por API.
 * Las estrategias se inyectan como beans con nombre NTRP / CERCANIA / HISTORIAL.
 */
@Component
public class MatchmakingStrategyRegistry {

    private final Map<EstrategiaEmparejamiento, MatchmakingStrategy> estrategias =
            new EnumMap<>(EstrategiaEmparejamiento.class);

    public MatchmakingStrategyRegistry(List<MatchmakingStrategy> disponibles) {
        for (MatchmakingStrategy estrategia : disponibles) {
            for (EstrategiaEmparejamiento tipo : EstrategiaEmparejamiento.values()) {
                if (tipo.name().equalsIgnoreCase(estrategia.getCodigo())) {
                    estrategias.put(tipo, estrategia);
                }
            }
        }
    }

    public MatchmakingStrategy obtener(EstrategiaEmparejamiento tipo) {
        MatchmakingStrategy estrategia = estrategias.get(tipo);
        if (estrategia == null) {
            throw new DomainValidationException("Estrategia de emparejamiento no soportada: " + tipo);
        }
        return estrategia;
    }

    public MatchmakingStrategy obtener(String codigo) {
        try {
            return obtener(EstrategiaEmparejamiento.desde(codigo));
        } catch (IllegalArgumentException ex) {
            throw new DomainValidationException("Estrategia de emparejamiento no soportada: " + codigo);
        }
    }

    public Map<EstrategiaEmparejamiento, MatchmakingStrategy> disponibles() {
        return Map.copyOf(estrategias);
    }
}
