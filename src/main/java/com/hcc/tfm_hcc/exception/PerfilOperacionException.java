package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class PerfilOperacionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PerfilOperacionException(String message) {
        super(message);
    }

    public PerfilOperacionException(String message, Throwable cause) {
        super(message, cause);
    }
}
