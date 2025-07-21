package com.hcc.tfm_hcc.exception;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class IncorrectCredentials extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IncorrectCredentials(String message) {
        super(message);
    }

    public IncorrectCredentials(String message, Throwable cause) {
        super(message, cause);
    }
    
}
