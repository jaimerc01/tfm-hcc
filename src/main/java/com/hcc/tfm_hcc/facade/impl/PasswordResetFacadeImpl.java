package com.hcc.tfm_hcc.facade.impl;

import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.dto.RestablecerPasswordDTO;
import com.hcc.tfm_hcc.dto.SolicitudRestablecerPasswordDTO;
import com.hcc.tfm_hcc.exception.PasswordResetTokenInvalidoException;
import com.hcc.tfm_hcc.facade.PasswordResetFacade;
import com.hcc.tfm_hcc.service.PasswordResetService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del facade de restablecimiento de contraseña.
 *
 * @author Sistema HCC
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetFacadeImpl implements PasswordResetFacade {

    private final PasswordResetService passwordResetService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void solicitarRestablecimiento(SolicitudRestablecerPasswordDTO solicitud) {
        String email = solicitud != null ? solicitud.getEmail() : null;
        log.debug("Solicitud de restablecimiento de contraseña recibida");
        passwordResetService.solicitarRestablecimiento(email);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restablecerPassword(RestablecerPasswordDTO datos) {
        if (datos == null) {
            throw new PasswordResetTokenInvalidoException(ErrorMessages.ERROR_RESET_TOKEN_INVALIDO);
        }
        passwordResetService.restablecerPassword(datos.getToken(), datos.getNuevaPassword());
        log.info("Restablecimiento de contraseña completado a través de enlace de un solo uso");
    }
}
