package com.hcc.tfm_hcc.exception;

/**
 * El token de restablecimiento de contraseña recibido no existe, ya se ha usado o
 * ha caducado. El controlador la traduce a un {@code 400 Bad Request} con un
 * mensaje genérico: no se distingue entre los tres casos para no dar pistas a
 * quien esté probando enlaces al azar.
 */
public class PasswordResetTokenInvalidoException extends RuntimeException {

    public PasswordResetTokenInvalidoException(String message) {
        super(message);
    }
}
