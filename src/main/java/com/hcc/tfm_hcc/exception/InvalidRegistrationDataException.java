package com.hcc.tfm_hcc.exception;

public class InvalidRegistrationDataException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public InvalidRegistrationDataException() {
        super();
    }

    public InvalidRegistrationDataException(String message) {
        super(message);
    }

    public InvalidRegistrationDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
