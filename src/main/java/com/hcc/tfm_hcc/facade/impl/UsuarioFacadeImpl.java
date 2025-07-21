package com.hcc.tfm_hcc.facade.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.service.UsuarioService;

@Component
public class UsuarioFacadeImpl implements UsuarioFacade {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioMapper usuarioMapper;

    @Override
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public UsuarioDTO altaUsuario(UsuarioDTO usuarioDTO) {
        return usuarioMapper.toDto(usuarioService.altaUsuario(usuarioDTO));
    }
}
