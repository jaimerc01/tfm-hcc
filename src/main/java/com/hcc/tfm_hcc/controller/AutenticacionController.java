package com.hcc.tfm_hcc.controller;

import org.springframework.http.ResponseEntity;

import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.model.LoginResponse;

public interface AutenticacionController {

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param usuarioDTO los datos del usuario a registrar
     * @return un ResponseEntity que contiene el UsuarioDTO del usuario registrado
     */
    ResponseEntity<UsuarioDTO> registrar(UsuarioDTO usuarioDTO);

    /**
     * Autentica a un usuario con sus credenciales.
     *
     * @param loginUsuarioDTO las credenciales del usuario
     * @return un ResponseEntity que contiene el LoginResponse con el token JWT y la información del usuario autenticado
     */
    ResponseEntity<LoginResponse> autenticar(LoginUsuarioDTO loginUsuarioDTO);
    
}
