package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción para errores internos/operacionales en el módulo de notificaciones.
 *
 * <p>Se traduce a {@code 500 INTERNAL SERVER ERROR}.</p>
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class NotificacionOperacionException extends RuntimeException {

    public NotificacionOperacionException(String message) {
        super(message);
    }

    public NotificacionOperacionException(String message, Throwable cause) {
        super(message, cause);
    }
}
