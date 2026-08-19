package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MedicoValidationException extends IllegalArgumentException {
    public MedicoValidationException(String message) {
        super(message);
    }

    public MedicoValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
