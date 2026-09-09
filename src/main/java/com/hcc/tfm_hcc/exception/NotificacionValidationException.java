package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción para errores de validación en el módulo de notificaciones.
 *
 * <p>Se traduce a {@code 400 BAD REQUEST}.</p>
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NotificacionValidationException extends IllegalArgumentException {

    public NotificacionValidationException(String message) {
        super(message);
    }

    public NotificacionValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
