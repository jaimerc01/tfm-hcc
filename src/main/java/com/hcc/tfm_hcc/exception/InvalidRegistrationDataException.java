package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Datos de la petición de registro mal formados o incompletos.
 *
 * <p>Se traduce a {@code 400 BAD REQUEST}.</p>
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRegistrationDataException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public InvalidRegistrationDataException() {
        super();
    }

    public InvalidRegistrationDataException(String message) {
        super(message);
    }

    public InvalidRegistrationDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
