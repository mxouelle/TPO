package ar.edu.uade.tenisskillgames.infrastructure.security;

import ar.edu.uade.tenisskillgames.domain.exception.CredencialesInvalidasException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro stateless: valida el Bearer token y publica la autenticacion
 * en el SecurityContext. No consulta la base de datos por request.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            try {
                UsuarioAutenticado principal = jwtService.validar(header.substring(7).trim());
                List<SimpleGrantedAuthority> authorities = principal.roles().stream()
                        .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.name()))
                        .toList();
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (CredencialesInvalidasException ex) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("""
                        {"status":401,"error":"Unauthorized","mensaje":"Token invalido o expirado"}""");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
