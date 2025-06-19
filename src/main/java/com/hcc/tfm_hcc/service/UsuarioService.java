package com.hcc.tfm_hcc.service;

import com.hcc.tfm_hcc.dto.UsuarioDTO;

public interface UsuarioService {
    
    /**
     * Da de alta un nuevo usuario en el sistema.
     *
     * @param usuarioDTO el objeto que contiene los datos del usuario a dar de alta
     * @return el id del usuario creado, o un mensaje de error si la operación falla
     */
    public String altaUsuario(UsuarioDTO usuarioDTO);
}
