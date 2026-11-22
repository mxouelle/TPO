package ar.edu.uade.tenisskillgames.controller;

import ar.edu.uade.tenisskillgames.controller.dto.Vistas;
import ar.edu.uade.tenisskillgames.domain.exception.ConflictoException;
import ar.edu.uade.tenisskillgames.domain.exception.CredencialesInvalidasException;
import ar.edu.uade.tenisskillgames.domain.exception.DomainValidationException;
import ar.edu.uade.tenisskillgames.domain.exception.OperacionNoPermitidaException;
import ar.edu.uade.tenisskillgames.domain.exception.RecursoNoEncontradoException;
import ar.edu.uade.tenisskillgames.domain.exception.UsuarioSuspendidoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce las excepciones de dominio al contrato de error de la API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Vistas.Error> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return construir(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req, null);
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<Vistas.Error> validacion(DomainValidationException ex, HttpServletRequest req) {
        return construir(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", ex.getMessage(), req, null);
    }

    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<Vistas.Error> noPermitida(OperacionNoPermitidaException ex, HttpServletRequest req) {
        return construir(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req, null);
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<Vistas.Error> conflicto(ConflictoException ex, HttpServletRequest req) {
        return construir(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req, null);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Vistas.Error> estadoIlegal(IllegalStateException ex, HttpServletRequest req) {
        return construir(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req, null);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Vistas.Error> credenciales(CredencialesInvalidasException ex, HttpServletRequest req) {
        return construir(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), req, null);
    }

    @ExceptionHandler(UsuarioSuspendidoException.class)
    public ResponseEntity<Vistas.Error> suspendido(UsuarioSuspendidoException ex, HttpServletRequest req) {
        Map<String, String> detalles = new LinkedHashMap<>();
        if (ex.getRehabilitacion() != null) {
            detalles.put("rehabilitacion", ex.getRehabilitacion().toString());
        }
        return construir(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), req, detalles);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Vistas.Error> denegado(AccessDeniedException ex, HttpServletRequest req) {
        return construir(HttpStatus.FORBIDDEN, "Forbidden",
                "No tiene permisos para esta operación", req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Vistas.Error> invalido(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> detalles = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                detalles.put(error.getField(), error.getDefaultMessage()));
        return construir(HttpStatus.BAD_REQUEST, "Bad Request",
                "La petición contiene datos inválidos", req, detalles);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Vistas.Error> restriccion(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> detalles = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v ->
                detalles.put(v.getPropertyPath().toString(), v.getMessage()));
        return construir(HttpStatus.BAD_REQUEST, "Bad Request", "Parámetros inválidos", req, detalles);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Vistas.Error> formato(Exception ex, HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST, "Bad Request",
                "Formato de datos inválido: " + ex.getMessage(), req, null);
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<Vistas.Error> rutaInexistente(Exception ex, HttpServletRequest req) {
        return construir(HttpStatus.NOT_FOUND, "Not Found",
                "El recurso solicitado no existe", req, null);
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Vistas.Error> metodoNoSoportado(Exception ex, HttpServletRequest req) {
        return construir(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed",
                "Método HTTP no soportado para este recurso", req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Vistas.Error> inesperado(Exception ex, HttpServletRequest req) {
        LOG.error("Error no controlado en {}", req.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Ocurrió un error inesperado", req, null);
    }

    private ResponseEntity<Vistas.Error> construir(HttpStatus status, String error, String mensaje,
                                                   HttpServletRequest req, Map<String, String> detalles) {
        Vistas.Error cuerpo = new Vistas.Error(status.value(), error, mensaje,
                req.getRequestURI(), LocalDateTime.now(), detalles);
        return ResponseEntity.status(status).body(cuerpo);
    }
}
