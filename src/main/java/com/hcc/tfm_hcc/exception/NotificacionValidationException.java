package com.hcc.tfm_hcc.exception;

/**
 * Excepción para errores de validación en el módulo de notificaciones.
 */
public class NotificacionValidationException extends IllegalArgumentException {

    public NotificacionValidationException(String message) {
        super(message);
    }

    public NotificacionValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
