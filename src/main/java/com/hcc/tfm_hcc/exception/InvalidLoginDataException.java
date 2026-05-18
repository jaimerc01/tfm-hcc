package com.hcc.tfm_hcc.exception;

public class InvalidLoginDataException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public InvalidLoginDataException() {
        super();
    }

    public InvalidLoginDataException(String message) {
        super(message);
    }

    public InvalidLoginDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
