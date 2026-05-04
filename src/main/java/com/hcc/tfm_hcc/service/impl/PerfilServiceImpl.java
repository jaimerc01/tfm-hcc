package com.hcc.tfm_hcc.service.impl;

import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.exception.PerfilNotFoundException;
import com.hcc.tfm_hcc.model.Perfil;
import com.hcc.tfm_hcc.repository.PerfilRepository;
import com.hcc.tfm_hcc.service.PerfilService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de perfiles.
 * 
 * <p>Esta clase proporciona la implementación de los servicios relacionados
 * con la gestión de perfiles de usuario en el sistema.</p>
 * 
 * <p>Responsabilidades principales:</p>
 * <ul>
 *   <li>Búsqueda de perfiles por rol</li>
 *   <li>Validación de existencia de perfiles</li>
 *   <li>Gestión centralizada de errores</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 2024
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PerfilServiceImpl implements PerfilService {

    // Constantes de campos
    private static final String CAMPO_ROL = "Rol";
    
    // Constantes de logging
    private static final String LOG_BUSCANDO_PERFIL = "Buscando perfil con rol: {}";
    private static final String LOG_PERFIL_ENCONTRADO = "Perfil encontrado con ID: {} para rol: {}";
    private static final String LOG_PERFIL_NO_ENCONTRADO = "Perfil no encontrado para rol: {}";

    // Dependencias inyectadas por constructor
    private final PerfilRepository perfilRepository;

    /**
     * Obtiene un perfil por su rol.
     *
     * @param rol el rol del perfil a buscar
     * @return el perfil encontrado
     * @throws PerfilNotFoundException si no se encuentra el perfil
     * @throws IllegalArgumentException si el rol es nulo o vacío
     */
    @Override
    public Perfil getPerfilByRol(String rol) throws PerfilNotFoundException {
        log.debug(LOG_BUSCANDO_PERFIL, rol);
        
        validarRol(rol);
        
        return perfilRepository.getPerfilByRol(rol)
            .map(perfil -> {
                log.info(LOG_PERFIL_ENCONTRADO, perfil.getId(), rol);
                return perfil;
            })
            .orElseThrow(() -> {
                log.warn(LOG_PERFIL_NO_ENCONTRADO, rol);
                return new PerfilNotFoundException(rol);
            });
    }

    /**
     * Valida que el rol proporcionado sea válido.
     *
     * @param rol el rol a validar
     * @throws IllegalArgumentException si el rol es nulo o vacío
     */
    private void validarRol(String rol) {
        if (rol == null || rol.trim().isEmpty()) {
            String error = ErrorMessages.campoRequerido(CAMPO_ROL);
            log.error(error);
            throw new IllegalArgumentException(error);
        }
    }
}