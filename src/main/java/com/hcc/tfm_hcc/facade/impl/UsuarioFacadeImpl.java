package com.hcc.tfm_hcc.facade.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.service.UsuarioService;

@Component
public class UsuarioFacadeImpl implements UsuarioFacade {

    @Autowired
    private UsuarioService usuarioService;

    @Override
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String altaUsuario(UsuarioDTO usuarioDTO) {
        return usuarioService.altaUsuario(usuarioDTO);
    }
}
