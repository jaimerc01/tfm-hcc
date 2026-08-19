package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PerfilValidationException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public PerfilValidationException(String message) {
        super(message);
    }

    public PerfilValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
