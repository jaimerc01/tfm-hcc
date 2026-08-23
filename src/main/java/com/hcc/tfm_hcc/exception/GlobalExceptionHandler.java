package com.hcc.tfm_hcc.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hcc.tfm_hcc.constants.ErrorMessages;

import lombok.extern.slf4j.Slf4j;

/**
 * Manejador global para errores de parseo y validación de peticiones entrantes.
 *
 * <p>Sin este manejador, un JSON mal formado o un fallo de validación de {@code @Valid}
 * provoca que el contenedor reenvíe la petición internamente a {@code /error} para
 * construir la respuesta de error por defecto. Ese reenvío es un despacho de tipo
 * ERROR que {@code JwtAuthenticationFilter} (por ser un {@code OncePerRequestFilter})
 * no procesa por defecto, dejando esa segunda pasada sin autenticar; como {@code /error}
 * no está en la lista de rutas públicas de {@code WebSecurityConfig}, Spring Security
 * la rechaza con 403 en lugar de devolver el 400 real, ocultando el verdadero error de
 * validación tras un "Forbidden" engañoso. Resolviendo la excepción aquí, dentro del
 * mismo ciclo de la petición original, se evita ese reenvío y se devuelve el código de
 * estado correcto.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Cuerpo de la petición no legible: JSON mal formado o tipos incompatibles
     * (por ejemplo, una fecha que no cumple el formato esperado).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleMensajeIlegible(HttpMessageNotReadableException e) {
        log.warn("Cuerpo de la petición ilegible o mal formado: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorMessages.ERROR_JSON_MAL_FORMADO);
    }

    /**
     * Fallo de validación de un DTO anotado con {@code @Valid}.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidacionInvalida(MethodArgumentNotValidException e) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errores.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("Validación de datos de entrada fallida: {}", errores);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }
}
