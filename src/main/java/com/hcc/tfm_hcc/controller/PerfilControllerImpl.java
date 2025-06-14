package com.hcc.tfm_hcc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcc.tfm_hcc.facade.PerfilFacade;
import com.hcc.tfm_hcc.model.Perfil;

@RestController
@RequestMapping("/perfil")
public class PerfilControllerImpl implements PerfilController {

    @Autowired
    private PerfilFacade perfilFacade;

    @Override
    @GetMapping("/rol/{rol}")
    public Perfil getPerfilByRol(@PathVariable String rol) {
        return perfilFacade.getPerfilByRol(rol);
    }
    
}
