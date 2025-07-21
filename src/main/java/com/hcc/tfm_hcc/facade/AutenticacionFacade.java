package com.hcc.tfm_hcc.facade;

import org.springframework.http.ResponseEntity;

import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.IncorrectCredentials;
import com.hcc.tfm_hcc.model.LoginResponse;

public interface AutenticacionFacade {

    /**
     * Realiza el proceso de autenticación de un usuario.
     *
     * @param loginUsuarioDTO objeto que contiene las credenciales del usuario
     * @return un ResponseEntity que contiene el LoginResponse con el token JWT y la información del usuario
     *         autenticado
     * @throws IncorrectCredentials si las credenciales son incorrectas o el usuario no existe
     */
    ResponseEntity<LoginResponse> autenticar(LoginUsuarioDTO loginUsuarioDTO) throws IncorrectCredentials;

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param usuarioDTO objeto que contiene los datos del usuario a registrar
     * @return un ResponseEntity que contiene el UsuarioDTO del usuario registrado
     */
    ResponseEntity<UsuarioDTO> registrar(UsuarioDTO usuarioDTO);
    
}
