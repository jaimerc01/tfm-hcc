package com.hcc.tfm_hcc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hcc.tfm_hcc.model.Perfil;

/**
 * Controlador REST para la gestión de perfiles de usuario en el sistema HCC.
 * Proporciona endpoints para consultar y gestionar los diferentes tipos de perfiles
 * según los roles definidos en el sistema.
 * 
 * <p>Los perfiles definen los roles y permisos de los usuarios:</p>
 * <ul>
 *   <li>ADMINISTRADOR: Gestión completa del sistema</li>
 *   <li>MEDICO: Acceso a funcionalidades médicas</li>
 *   <li>PACIENTE: Acceso a datos personales de salud</li>
 * </ul>
 * 
 * <p>Los endpoints pueden requerir autenticación según la configuración de seguridad.</p>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface PerfilController {
    
    /**
     * Obtiene un perfil específico según el rol especificado.
     * Esta operación permite recuperar la configuración de permisos
     * y características asociadas a un rol determinado del sistema.
     *
     * @param rol Nombre del rol del perfil a buscar (ADMINISTRADOR, MEDICO, PACIENTE)
     * @return ResponseEntity con el Perfil correspondiente al rol especificado
     * @throws IllegalArgumentException si el rol especificado no es válido
     * @throws NullPointerException si el rol es null
     */
    @GetMapping("/rol/{rol}")
    ResponseEntity<Perfil> getPerfilByRol(@PathVariable("rol") String rol);
}
