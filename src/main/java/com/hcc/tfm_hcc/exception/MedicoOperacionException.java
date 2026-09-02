package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción personalizada para errores en operaciones generales del médico.
 * Se lanza cuando hay problemas inesperados en operaciones médicas que no
 * se encajan en categorías más específicas.
 *
 * <p>Se traduce a {@code 500 INTERNAL SERVER ERROR}: es un fallo del servidor,
 * no un error atribuible a la petición del cliente.</p>
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
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
