package com.hcc.tfm_hcc.facade;

import com.hcc.tfm_hcc.model.Perfil;

public interface PerfilFacade {

    /**
     * Obtiene un perfil según el rol especificado.
     *
     * @param rol el nombre del rol del perfil a buscar
     * @return el perfil correspondiente al rol, o null si no se encuentra
     */
    Perfil getPerfilByRol(String rol);
    
}
