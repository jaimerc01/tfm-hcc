package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UsuarioNoAutenticadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UsuarioNoAutenticadoException(String message) {
        super(message);
    }

    public UsuarioNoAutenticadoException(String message, Throwable cause) {
        super(message, cause);
    }
}
