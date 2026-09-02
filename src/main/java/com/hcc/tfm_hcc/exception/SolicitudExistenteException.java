package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Se lanza al intentar crear una solicitud de asignación médico-paciente cuando ya
 * existe otra en estado PENDIENTE para la misma pareja médico/paciente.
 *
 * <p>Se traduce a {@code 409 CONFLICT}: es un conflicto con el estado actual del
 * recurso, no un error de validación de la petición.</p>
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class SolicitudExistenteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SolicitudExistenteException(String message) {
        super(message);
    }
}
