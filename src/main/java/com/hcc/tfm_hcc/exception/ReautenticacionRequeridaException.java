package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Se lanza cuando una operación sensible (exportar todos los datos del usuario, eliminar
 * la cuenta) requiere confirmar la contraseña actual mediante la cabecera
 * {@code X-Current-Password} y esta falta o no coincide. El controlador la traduce a una
 * respuesta 401 con la cabecera {@code X-Reauth-Required: true}, para que el cliente sepa
 * que debe pedir la contraseña al usuario y reintentar la operación.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class ReautenticacionRequeridaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReautenticacionRequeridaException(String message) {
        super(message);
    }
}
