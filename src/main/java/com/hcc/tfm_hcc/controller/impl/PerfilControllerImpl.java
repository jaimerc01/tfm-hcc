package com.hcc.tfm_hcc.controller.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcc.tfm_hcc.controller.PerfilController;
import com.hcc.tfm_hcc.facade.PerfilFacade;
import com.hcc.tfm_hcc.model.Perfil;

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
@RequestMapping("/perfil")
@RequiredArgsConstructor
public class PerfilControllerImpl implements PerfilController {

    /** Facade para operaciones de perfiles */
    private final PerfilFacade perfilFacade;

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/rol/{rol}")
    public ResponseEntity<Perfil> getPerfilByRol(@PathVariable("rol") String rol) {
        log.info("Consultando perfil para rol: {}", rol);
        
        try {
            // Validar parámetro de entrada
            if (rol == null || rol.trim().isEmpty()) {
                log.warn("Intento de consulta con rol nulo o vacío");
                return ResponseEntity.badRequest().build();
            }
            
            // Normalizar el rol a mayúsculas para consistencia
            String rolNormalizado = rol.trim().toUpperCase();
            log.debug("Rol normalizado: {}", rolNormalizado);
            
            Perfil perfil = perfilFacade.getPerfilByRol(rolNormalizado);
            
            if (perfil == null) {
                log.warn("No se encontró perfil para el rol: {}", rolNormalizado);
                return ResponseEntity.notFound().build();
            }
            
            log.info("Perfil encontrado exitosamente para rol: {}, ID: {}", rolNormalizado, perfil.getId());
            return ResponseEntity.ok(perfil);
            
        } catch (IllegalArgumentException e) {
            log.warn("Parámetro inválido para consulta de perfil, rol: {}, error: {}", rol, e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (Exception e) {
            log.error("Error interno al consultar perfil para rol: {}, error: {}", rol, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
