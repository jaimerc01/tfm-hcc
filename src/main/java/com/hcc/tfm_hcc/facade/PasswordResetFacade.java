package com.hcc.tfm_hcc.facade;

import com.hcc.tfm_hcc.dto.RestablecerPasswordDTO;
import com.hcc.tfm_hcc.dto.SolicitudRestablecerPasswordDTO;
import com.hcc.tfm_hcc.exception.PasswordResetTokenInvalidoException;

/**
 * Facade del flujo de restablecimiento de contraseña ("he olvidado mi contraseña").
 * Orquesta la validación de entrada y delega en {@link com.hcc.tfm_hcc.service.PasswordResetService}.
 *
 * <p>Sus operaciones son públicas: se invocan desde endpoints sin autenticación,
 * ya que precisamente están pensadas para quien no puede iniciar sesión.</p>
 */
public interface PasswordResetFacade {

    /**
     * Procesa la solicitud de un enlace de restablecimiento. Siempre termina con
     * normalidad, exista o no una cuenta con ese correo (respuesta neutra).
     *
     * @param solicitud cuerpo con el correo de la cuenta
     */
    void solicitarRestablecimiento(SolicitudRestablecerPasswordDTO solicitud);

    /**
     * Completa el restablecimiento con el token del enlace y la nueva contraseña.
     *
     * @param datos cuerpo con el token y la nueva contraseña
     * @throws PasswordResetTokenInvalidoException si el token no es válido o ha caducado
     * @throws IllegalArgumentException si la nueva contraseña no cumple la política mínima
     */
    void restablecerPassword(RestablecerPasswordDTO datos);
}
