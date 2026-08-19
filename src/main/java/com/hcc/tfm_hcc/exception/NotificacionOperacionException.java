package com.hcc.tfm_hcc.exception;

/**
 * Excepción para errores internos/operacionales en el módulo de notificaciones.
 */
public class NotificacionOperacionException extends RuntimeException {

    public NotificacionOperacionException(String message) {
        super(message);
    }

    public NotificacionOperacionException(String message, Throwable cause) {
        super(message, cause);
    }
}
