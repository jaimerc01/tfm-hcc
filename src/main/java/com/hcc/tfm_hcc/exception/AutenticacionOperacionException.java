package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class AutenticacionOperacionException extends RuntimeException {
    public AutenticacionOperacionException(String message) {
        super(message);
    }

    public AutenticacionOperacionException(String message, Throwable cause) {
        super(message, cause);
    }
}
