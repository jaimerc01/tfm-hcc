package com.hcc.tfm_hcc.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcc.tfm_hcc.controller.UsuarioController;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.facade.UsuarioFacade;

@RestController
@RequestMapping("/usuario")
public class UsuarioControllerImpl implements UsuarioController{

    @Autowired
    private UsuarioFacade usuarioFacade;

    // @Override
    // @PostMapping("/alta")
    // public String altaUsuario(@RequestBody UsuarioDTO usuarioDTO) {
    //     return usuarioFacade.altaUsuario(usuarioDTO);
    // }
}
