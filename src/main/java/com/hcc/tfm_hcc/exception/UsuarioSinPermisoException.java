package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UsuarioSinPermisoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UsuarioSinPermisoException(String message) {
        super(message);
    }

    public UsuarioSinPermisoException(String message, Throwable cause) {
        super(message, cause);
    }
}
