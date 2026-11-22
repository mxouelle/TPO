package ar.edu.uade.tenisskillgames.infrastructure.security;

import ar.edu.uade.tenisskillgames.domain.enums.Rol;
import ar.edu.uade.tenisskillgames.domain.exception.CredencialesInvalidasException;
import ar.edu.uade.tenisskillgames.domain.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Emision y validacion de tokens JWT firmados (HS256).
 */
@Component
public class JwtService {

    public static final String CLAIM_TIPO = "tipo";
    public static final String TIPO_VERIFICACION = "verificacion";

    private final SecretKey clave;
    private final long expiracionSegundos;

    public JwtService(@Value("${tenis.seguridad.jwt.secret}") String secreto,
                      @Value("${tenis.seguridad.jwt.expiracion-minutos:120}") long expiracionMinutos) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionSegundos = expiracionMinutos * 60;
    }

    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getId().toString())
                .claim("email", usuario.getEmail())
                .claim("nombre", usuario.getNombre())
                .claim("roles", usuario.getRoles().stream().map(Enum::name).toList())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusSeconds(expiracionSegundos)))
                .signWith(clave)
                .compact();
    }

    /**
     * Token de un solo proposito para la verificacion de cuenta por email.
     */
    public String generarTokenVerificacion(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getId().toString())
                .claim(CLAIM_TIPO, TIPO_VERIFICACION)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusSeconds(86400)))
                .signWith(clave)
                .compact();
    }

    public UsuarioAutenticado validar(String token) {
        try {
            Jws<Claims> jws = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token);
            Claims claims = jws.getPayload();
            if (TIPO_VERIFICACION.equals(claims.get(CLAIM_TIPO, String.class))) {
                throw new CredencialesInvalidasException("El token no es de acceso");
            }
            Set<Rol> roles = new HashSet<>();
            List<?> rolesClaim = claims.get("roles", List.class);
            if (rolesClaim != null) {
                rolesClaim.forEach(r -> roles.add(Rol.valueOf(String.valueOf(r))));
            }
            return new UsuarioAutenticado(
                    UUID.fromString(claims.getSubject()),
                    claims.get("email", String.class),
                    claims.get("nombre", String.class),
                    roles);
        } catch (CredencialesInvalidasException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new CredencialesInvalidasException("Token invalido o expirado");
        }
    }

    public UUID validarTokenVerificacion(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(clave).build()
                    .parseSignedClaims(token).getPayload();
            if (!TIPO_VERIFICACION.equals(claims.get(CLAIM_TIPO, String.class))) {
                throw new CredencialesInvalidasException("Token de verificacion invalido");
            }
            return UUID.fromString(claims.getSubject());
        } catch (CredencialesInvalidasException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new CredencialesInvalidasException("Token de verificacion invalido o expirado");
        }
    }

    public long getExpiracionSegundos() {
        return expiracionSegundos;
    }
}
