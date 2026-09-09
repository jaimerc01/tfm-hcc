package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Se lanza cuando se intenta revocar una relación médico-paciente que no existe
 * o que no está actualmente activa.
 *
 * <p>Se traduce a {@code 404 NOT FOUND}.</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RelacionMedicoPacienteNoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RelacionMedicoPacienteNoEncontradaException(String message) {
        super(message);
    }

    public RelacionMedicoPacienteNoEncontradaException(String message, Throwable cause) {
        super(message, cause);
    }
}
