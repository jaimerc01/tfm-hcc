package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Datos de la petición de login mal formados (NIF o contraseña ausentes o inválidos).
 *
 * <p>Se traduce a {@code 400 BAD REQUEST}. Es distinto de {@link IncorrectCredentials}
 * ({@code 401}), que representa credenciales bien formadas pero que no coinciden.</p>
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidLoginDataException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public InvalidLoginDataException() {
        super();
    }

    public InvalidLoginDataException(String message) {
        super(message);
    }

    public InvalidLoginDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
