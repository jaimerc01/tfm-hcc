package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción para errores en operaciones sobre propuestas de cambio clínico (crear, listar o
 * resolver una propuesta con datos inválidos, motivo ausente, operación no soportada, etc.).
 *
 * <p>Se traduce a {@code 400 BAD REQUEST}: representa una petición que el cliente no ha formado
 * correctamente. La falta de permiso (relación no activa, propuesta ajena) se expresa con
 * {@link UsuarioSinPermisoException}, y la ausencia de la propuesta con
 * {@link PropuestaCambioNoEncontradaException}.</p>
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PropuestaCambioClinicoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PropuestaCambioClinicoException(String message) {
        super(message);
    }

    public PropuestaCambioClinicoException(String message, Throwable cause) {
        super(message, cause);
    }
}
