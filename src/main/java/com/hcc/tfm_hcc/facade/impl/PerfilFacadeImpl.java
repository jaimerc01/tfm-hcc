package com.hcc.tfm_hcc.facade.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.facade.PerfilFacade;
import com.hcc.tfm_hcc.model.Perfil;
import com.hcc.tfm_hcc.service.PerfilService;

@Component
public class PerfilFacadeImpl implements PerfilFacade {

    @Autowired
    private PerfilService perfilService;

    @Override
    public Perfil getPerfilByRol(String rol) {
        
        return perfilService.getPerfilByRol(rol);
    }
    
}
