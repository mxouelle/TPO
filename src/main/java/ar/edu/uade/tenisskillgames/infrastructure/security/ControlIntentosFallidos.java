package ar.edu.uade.tenisskillgames.infrastructure.security;

import ar.edu.uade.tenisskillgames.domain.exception.CredencialesInvalidasException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Control de intentos fallidos de login por email (CU2: maximo 5 por minuto).
 */
@Component
public class ControlIntentosFallidos {

    private static final int MAXIMO_FALLOS = 5;
    private static final long VENTANA_SEGUNDOS = 60;

    private final Map<String, Deque<Long>> fallos = new ConcurrentHashMap<>();

    public void registrarFallo(String email) {
        Deque<Long> marcas = fallos.computeIfAbsent(email.toLowerCase(), k -> new ArrayDeque<>());
        synchronized (marcas) {
            marcas.addLast(Instant.now().getEpochSecond());
        }
    }

    public void limpiar(String email) {
        fallos.remove(email.toLowerCase());
    }

    public void validarNoBloqueado(String email) {
        Deque<Long> marcas = fallos.get(email.toLowerCase());
        if (marcas == null) {
            return;
        }
        long ahora = Instant.now().getEpochSecond();
        synchronized (marcas) {
            while (!marcas.isEmpty() && ahora - marcas.peekFirst() > VENTANA_SEGUNDOS) {
                marcas.pollFirst();
            }
            if (marcas.size() >= MAXIMO_FALLOS) {
                throw new CredencialesInvalidasException(
                        "Demasiados intentos fallidos. Espere un minuto antes de reintentar.");
            }
        }
    }
}
