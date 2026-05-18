package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UsuarioValidationException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public UsuarioValidationException(String message) {
        super(message);
    }

    public UsuarioValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
