package com.hcc.tfm_hcc.facade.impl;

import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.facade.PerfilFacade;
import com.hcc.tfm_hcc.model.Perfil;
import com.hcc.tfm_hcc.service.PerfilService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación de la fachada para operaciones de perfil.
 * 
 * <p>Esta clase actúa como una capa de fachada entre los controladores y los servicios
 * de perfil, proporcionando una interfaz simplificada para las operaciones relacionadas
 * con la gestión de perfiles y roles del sistema.</p>
 * 
 * <p>Funcionalidades principales:</p>
 * <ul>
 *   <li>Consulta de perfiles por rol</li>
 *   <li>Gestión de permisos y autorización</li>
 *   <li>Validación de roles de usuario</li>
 * </ul>
 * 
 * <p>La clase utiliza inyección de dependencias por constructor y logging estructurado
 * para seguir las mejores prácticas de desarrollo empresarial.</p>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PerfilFacadeImpl implements PerfilFacade {

    // ===============================
    // DEPENDENCIAS INYECTADAS
    // ===============================
    
    /**
     * Servicio para operaciones de perfil.
     */
    private final PerfilService perfilService;

    // ===============================
    // MÉTODOS DE GESTIÓN DE PERFILES
    // ===============================

    /**
     * Obtiene un perfil específico basado en el rol proporcionado.
     * 
     * @param rol El rol para el cual se busca el perfil
     * @return Perfil El perfil correspondiente al rol especificado
     * @throws IllegalArgumentException Si el rol es nulo o vacío
     * @throws RuntimeException Si ocurre un error durante la consulta
     */
    @Override
    public Perfil getPerfilByRol(String rol) {
        log.debug("Obteniendo perfil para rol: {}", rol);
        
        try {
            validarRol(rol);
            
            Perfil perfil = perfilService.getPerfilByRol(rol);
            
            log.info("Perfil obtenido exitosamente para rol: {} - ID: {}", 
                    rol, perfil != null ? perfil.getId() : "null");
            return perfil;
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al obtener perfil por rol: {} - Error: {}", 
                    rol, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al obtener perfil por rol: {} - Error: {}", 
                     rol, e.getMessage(), e);
            throw new RuntimeException("Error interno durante la consulta del perfil", e);
        }
    }

    // ===============================
    // MÉTODOS UTILITARIOS PRIVADOS
    // ===============================

    /**
     * Valida que el rol proporcionado sea válido.
     * 
     * @param rol El rol a validar
     * @throws IllegalArgumentException Si el rol es nulo o vacío
     */
    private void validarRol(String rol) {
        if (rol == null || rol.trim().isEmpty()) {
            throw new IllegalArgumentException("El rol no puede ser nulo o vacío");
        }
    }
}
