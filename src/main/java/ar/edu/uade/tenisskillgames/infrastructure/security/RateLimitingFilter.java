package ar.edu.uade.tenisskillgames.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting de ventana deslizante sobre los endpoints publicos de
 * autenticacion (RF no funcional de seguridad).
 */
public class RateLimitingFilter extends OncePerRequestFilter {

    private final int maximoIntentos;
    private final long ventanaSegundos;
    private final Map<String, Deque<Long>> registros = new ConcurrentHashMap<>();

    public RateLimitingFilter(int maximoIntentos, long ventanaSegundos) {
        this.maximoIntentos = maximoIntentos;
        this.ventanaSegundos = ventanaSegundos;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clave = request.getRemoteAddr() + "|" + request.getRequestURI();
        long ahora = Instant.now().getEpochSecond();
        Deque<Long> marcas = registros.computeIfAbsent(clave, k -> new ArrayDeque<>());

        synchronized (marcas) {
            while (!marcas.isEmpty() && ahora - marcas.peekFirst() > ventanaSegundos) {
                marcas.pollFirst();
            }
            if (marcas.size() >= maximoIntentos) {
                long espera = ventanaSegundos - (ahora - marcas.peekFirst());
                response.setStatus(429);
                response.setHeader("Retry-After", String.valueOf(Math.max(espera, 1)));
                response.setContentType("application/json");
                response.getWriter().write("""
                        {"status":429,"error":"Too Many Requests","mensaje":"Demasiados intentos. Reintente en unos segundos."}""");
                return;
            }
            marcas.addLast(ahora);
        }
        filterChain.doFilter(request, response);
    }
}
