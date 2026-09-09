package com.hcc.tfm_hcc.controller.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcc.tfm_hcc.constants.RestUrls;
import com.hcc.tfm_hcc.controller.PasswordResetController;
import com.hcc.tfm_hcc.dto.RestablecerPasswordDTO;
import com.hcc.tfm_hcc.dto.SolicitudRestablecerPasswordDTO;
import com.hcc.tfm_hcc.exception.PasswordResetTokenInvalidoException;
import com.hcc.tfm_hcc.facade.PasswordResetFacade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del controlador REST del flujo de restablecimiento de contraseña.
 *
 * <p>Delega en {@link PasswordResetFacade}. La solicitud de enlace responde
 * siempre {@code 204} (incluso ante un fallo interno) para no filtrar qué correos
 * están registrados; el canje del token distingue el {@code 400} cuando el enlace
 * no es válido o la contraseña es demasiado corta.</p>
 *
 * @author Sistema HCC
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping(RestUrls.AUTH_BASE)
@RequiredArgsConstructor
public class PasswordResetControllerImpl implements PasswordResetController {

    private final PasswordResetFacade passwordResetFacade;

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping(RestUrls.AUTH_PASSWORD_RESET_REQUEST)
    public ResponseEntity<Void> solicitarRestablecimiento(@RequestBody SolicitudRestablecerPasswordDTO solicitud) {
        try {
            passwordResetFacade.solicitarRestablecimiento(solicitud);
        } catch (Exception e) {
            // Nunca se propaga: la respuesta debe ser indistinguible del caso "correo
            // existente", para no revelar el estado de la cuenta.
            log.error("Error al procesar una solicitud de restablecimiento de contraseña: {}", e.getMessage(), e);
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping(RestUrls.AUTH_PASSWORD_RESET_CONFIRM)
    public ResponseEntity<Void> restablecerPassword(@RequestBody RestablecerPasswordDTO datos) {
        try {
            passwordResetFacade.restablecerPassword(datos);
            log.info("Restablecimiento de contraseña aplicado correctamente");
            return ResponseEntity.noContent().build();
        } catch (PasswordResetTokenInvalidoException | IllegalArgumentException e) {
            log.warn("Restablecimiento de contraseña rechazado: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error interno al restablecer la contraseña: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
