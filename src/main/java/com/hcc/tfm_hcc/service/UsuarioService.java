package com.hcc.tfm_hcc.service;

import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.model.Usuario;

public interface UsuarioService {
    
    /**
     * Da de alta un nuevo usuario en el sistema.
     *
     * @param usuarioDTO el objeto que contiene los datos del usuario a dar de alta
     * @return el usuario creado
     */
    public Usuario altaUsuario(UsuarioDTO usuarioDTO);
}
