package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción para cuando no existe la propuesta de cambio clínico referenciada, o la propuesta
 * no pertenece al usuario que intenta operar sobre ella.
 *
 * <p>Se traduce a {@code 404 NOT FOUND}: desde el punto de vista de quien llama, la propuesta
 * no está disponible (no se distingue "no existe" de "no es tuya" para no filtrar información).</p>
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PropuestaCambioNoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PropuestaCambioNoEncontradaException(String message) {
        super(message);
    }

    public PropuestaCambioNoEncontradaException(String message, Throwable cause) {
        super(message, cause);
    }
}
