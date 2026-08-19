package com.hcc.tfm_hcc.exception;

/**
 * Excepción personalizada para errores en operaciones generales del médico.
 * Se lanza cuando hay problemas inesperados en operaciones médicas que no
 * se encajan en categorías más específicas.
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public class MedicoOperacionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MedicoOperacionException() {
        super();
    }

    public MedicoOperacionException(String message) {
        super(message);
    }

    public MedicoOperacionException(String message, Throwable cause) {
        super(message, cause);
    }

    public MedicoOperacionException(Throwable cause) {
        super(cause);
    }
}
