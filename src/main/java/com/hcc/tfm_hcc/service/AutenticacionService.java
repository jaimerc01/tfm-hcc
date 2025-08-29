package com.hcc.tfm_hcc.service;

import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.IncorrectCredentials;
import com.hcc.tfm_hcc.model.Usuario;

public interface AutenticacionService {

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param usuarioDTO los datos del usuario a registrar
     * @return el usuario registrado
     */
    Usuario registrar(UsuarioDTO usuarioDTO);

    /**
     * Autentica a un usuario con las credenciales proporcionadas.
     *
     * @param loginUsuarioDTO objeto que contiene las credenciales del usuario
     * @return el usuario autenticado
     * @throws IncorrectCredentials si las credenciales son incorrectas o el usuario no existe
     */
    Usuario autenticar(LoginUsuarioDTO loginUsuarioDTO) throws IncorrectCredentials;
}
