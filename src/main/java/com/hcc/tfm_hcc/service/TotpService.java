package com.hcc.tfm_hcc.service;

/**
 * Genera y valida contraseñas de un solo uso basadas en tiempo (TOTP, RFC 6238),
 * usadas como segundo factor de autenticación con una aplicación autenticadora
 * (Google Authenticator, Authy, etc.).
 */
public interface TotpService {

    /**
     * Genera un nuevo secreto aleatorio para un usuario, codificado en Base32
     * (formato que esperan las aplicaciones autenticadoras).
     *
     * @return secreto en Base32, sin relleno ('=')
     */
    String generarSecreto();

    /**
     * Construye la URI {@code otpauth://} que una aplicación autenticadora puede
     * interpretar (normalmente a través de un código QR) para dar de alta la
     * cuenta y el secreto.
     *
     * @param secreto secreto en Base32 generado por {@link #generarSecreto()}
     * @param nombreCuenta identificador legible de la cuenta (p. ej. el NIF del usuario)
     * @return URI en formato {@code otpauth://totp/...}
     */
    String generarOtpAuthUri(String secreto, String nombreCuenta);

    /**
     * Comprueba si un código de 6 dígitos es válido para el secreto dado en el
     * instante actual, tolerando un pequeño desfase de reloj entre el servidor y
     * el dispositivo del usuario.
     *
     * @param secretoBase32 secreto del usuario, en Base32
     * @param codigo código introducido por el usuario
     * @return true si el código es válido
     */
    boolean validarCodigo(String secretoBase32, String codigo);
}
