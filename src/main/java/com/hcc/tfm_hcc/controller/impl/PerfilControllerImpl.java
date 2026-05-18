package com.hcc.tfm_hcc.controller.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcc.tfm_hcc.constants.RestUrls;
import com.hcc.tfm_hcc.controller.PerfilController;
import com.hcc.tfm_hcc.facade.PerfilFacade;
import com.hcc.tfm_hcc.model.Perfil;
import com.hcc.tfm_hcc.exception.PerfilNotFoundException;
import com.hcc.tfm_hcc.exception.PerfilOperacionException;
import com.hcc.tfm_hcc.exception.PerfilValidationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del controlador REST para la gestión de perfiles de usuario.
 * Proporciona endpoints REST para consultar perfiles según roles,
 * delegando la lógica de negocio al facade correspondiente.
 * 
 * <p>Características del controlador:</p>
 * <ul>
 *   <li>Endpoints públicos para consulta de perfiles</li>
 *   <li>Validación de parámetros de entrada</li>
 *   <li>Logging detallado de operaciones</li>
 *   <li>Gestión centralizada de errores</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping(RestUrls.PERFIL_BASE)
@RequiredArgsConstructor
public class PerfilControllerImpl implements PerfilController {

    /** Facade para operaciones de perfiles */
    private final PerfilFacade perfilFacade;

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.PERFIL_ROL)
    public ResponseEntity<Perfil> getPerfilByRol(@PathVariable("rol") String rol) {
        log.info("Consultando perfil para rol: {}", rol);
        
        try {
            if (rol == null || rol.trim().isEmpty()) {
                log.warn("Intento de consulta con rol nulo o vacío");
                throw new PerfilValidationException("El rol es obligatorio");
            }
            
            String rolNormalizado = rol.trim().toUpperCase();
            log.debug("Rol normalizado: {}", rolNormalizado);
            
            Perfil perfil = perfilFacade.getPerfilByRol(rolNormalizado);
            
            if (perfil == null) {
                log.warn("No se encontró perfil para el rol: {}", rolNormalizado);
                throw new PerfilNotFoundException(rolNormalizado);
            }
            
            log.info("Perfil encontrado exitosamente para rol: {}, ID: {}", rolNormalizado, perfil.getId());
            return ResponseEntity.ok(perfil);
            
        } catch (PerfilValidationException e) {
            log.warn("Parámetro inválido para consulta de perfil, rol: {}, error: {}", rol, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (PerfilNotFoundException e) {
            log.warn("Perfil no encontrado para rol: {}", rol);
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Error interno al consultar perfil para rol: {}, error: {}", rol, e.getMessage(), e);
            throw new PerfilOperacionException("Error interno al consultar perfil", e);
        }
    }
}
