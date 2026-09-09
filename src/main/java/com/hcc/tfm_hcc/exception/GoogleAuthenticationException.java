package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando falla la autenticación con Google: el email no está
 * verificado, no existe ninguna cuenta asociada o el código de intercambio no es válido.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class GoogleAuthenticationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String errorCode;

    public GoogleAuthenticationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
