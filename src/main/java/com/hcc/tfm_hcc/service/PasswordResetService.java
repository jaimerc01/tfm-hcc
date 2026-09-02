package com.hcc.tfm_hcc.service;

import com.hcc.tfm_hcc.exception.PasswordResetTokenInvalidoException;

/**
 * Flujo de restablecimiento de contraseña para usuarios que no recuerdan la suya
 * ("he olvidado mi contraseña").
 *
 * <p>El canal fuera de banda es el correo electrónico: el sistema envía a la
 * dirección de la cuenta un enlace con un token de un solo uso y corta duración,
 * y ese token es la única credencial que permite fijar una contraseña nueva sin
 * conocer la anterior.</p>
 */
public interface PasswordResetService {

    /**
     * Inicia el restablecimiento para la cuenta asociada a un correo.
     *
     * <p>La operación es deliberadamente <b>neutra</b>: no revela si existe o no
     * una cuenta con ese correo. Si existe y está activa, invalida cualquier token
     * anterior, genera uno nuevo y envía el enlace por correo; si no existe, o la
     * cuenta está eliminada, no hace nada. En ningún caso lanza una excepción por
     * "correo desconocido".</p>
     *
     * @param email correo indicado por el usuario
     */
    void solicitarRestablecimiento(String email);

    /**
     * Completa el restablecimiento: valida el token recibido en el enlace y, si es
     * correcto, sustituye la contraseña de la cuenta por la nueva.
     *
     * <p>El token se consume: un segundo intento con el mismo enlace se rechaza.
     * Al cambiar la contraseña se actualiza {@code lastPasswordChange}, lo que
     * invalida los JWT emitidos antes.</p>
     *
     * @param token token en claro recibido en el enlace de correo
     * @param nuevaPassword nueva contraseña elegida por el usuario
     * @throws PasswordResetTokenInvalidoException si el token no existe, ya se usó o caducó
     * @throws IllegalArgumentException si la nueva contraseña no cumple la política mínima
     */
    void restablecerPassword(String token, String nuevaPassword);
}
