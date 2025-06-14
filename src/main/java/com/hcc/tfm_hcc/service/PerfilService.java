package com.hcc.tfm_hcc.service;

import com.hcc.tfm_hcc.model.Perfil;

public interface PerfilService {

    /**
     * Busca un perfil según el rol proporcionado.
     *
     * @param rol el nombre del rol a buscar
     * @return el perfil correspondiente o null si no se encuentra
     */
    Perfil getPerfilByRol(String rol);

}
