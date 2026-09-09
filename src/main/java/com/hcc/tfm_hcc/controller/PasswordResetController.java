package com.hcc.tfm_hcc.controller;

import org.springframework.http.ResponseEntity;

import com.hcc.tfm_hcc.dto.RestablecerPasswordDTO;
import com.hcc.tfm_hcc.dto.SolicitudRestablecerPasswordDTO;

/**
 * Controlador REST del flujo de restablecimiento de contraseña ("he olvidado mi
 * contraseña"). Sus endpoints son públicos: están pensados para usuarios que no
 * pueden iniciar sesión.
 */
public interface PasswordResetController {

    /**
     * Inicia el restablecimiento: si el correo indicado corresponde a una cuenta
     * activa, el sistema le envía un enlace con un token de un solo uso.
     *
     * <p>Responde <b>siempre</b> {@code 204 No Content}, exista o no la cuenta,
     * para no revelar qué correos están registrados.</p>
     *
     * @param solicitud cuerpo con el correo de la cuenta
     * @return {@code 204 No Content}
     */
    ResponseEntity<Void> solicitarRestablecimiento(SolicitudRestablecerPasswordDTO solicitud);

    /**
     * Completa el restablecimiento con el token del enlace y la nueva contraseña.
     *
     * @param datos cuerpo con el token y la nueva contraseña
     * @return {@code 204 No Content} si el cambio se aplica; {@code 400 Bad Request}
     *         si el token no es válido o la contraseña no cumple la política mínima
     */
    ResponseEntity<Void> restablecerPassword(RestablecerPasswordDTO datos);
}
