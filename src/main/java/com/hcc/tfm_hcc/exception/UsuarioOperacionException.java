package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UsuarioOperacionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UsuarioOperacionException(String message) {
        super(message);
    }

    public UsuarioOperacionException(String message, Throwable cause) {
        super(message, cause);
    }
}
