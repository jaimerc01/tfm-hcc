package com.hcc.tfm_hcc.exception;

/**
 * Excepción para denegación de acceso relativa a notificaciones.
 */
public class NotificacionAccesoException extends SecurityException {

    public NotificacionAccesoException(String message) {
        super(message);
    }

    public NotificacionAccesoException(String message, Throwable cause) {
        super(message, cause);
    }
}
