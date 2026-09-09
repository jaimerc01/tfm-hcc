package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción para denegación de acceso relativa a notificaciones.
 *
 * <p>Se traduce a {@code 403 FORBIDDEN}.</p>
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotificacionAccesoException extends SecurityException {

    public NotificacionAccesoException(String message) {
        super(message);
    }

    public NotificacionAccesoException(String message, Throwable cause) {
        super(message, cause);
    }
}
