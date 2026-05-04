package com.hcc.tfm_hcc.facade;

import com.hcc.tfm_hcc.model.Perfil;

/**
 * Facade para la gestión de perfiles de usuario en el sistema HCC.
 * Proporciona operaciones para consultar y gestionar los diferentes
 * tipos de perfiles según los roles definidos en el sistema.
 * 
 * <p>Los perfiles definen los roles y permisos de los usuarios:</p>
 * <ul>
 *   <li>Administrador: Gestión completa del sistema</li>
 *   <li>Médico: Acceso a funcionalidades médicas</li>
 *   <li>Paciente: Acceso a datos personales de salud</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface PerfilFacade {

    /**
     * Obtiene un perfil específico según el rol especificado.
     * Esta operación permite recuperar la configuración de permisos
     * y características asociadas a un rol determinado.
     *
     * @param rol Nombre del rol del perfil a buscar (ADMIN, MEDICO, PACIENTE)
     * @return Perfil correspondiente al rol especificado
     * @throws IllegalArgumentException si el rol especificado no es válido
     * @throws NullPointerException si el rol es null
     */
    Perfil getPerfilByRol(String rol) throws IllegalArgumentException, NullPointerException;
}
