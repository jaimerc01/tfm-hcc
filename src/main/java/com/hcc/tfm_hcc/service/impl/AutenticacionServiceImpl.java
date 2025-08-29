package com.hcc.tfm_hcc.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.IncorrectCredentials;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.AutenticacionService;
import com.hcc.tfm_hcc.service.UsuarioService;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio de autenticación.
 * Proporciona funcionalidades para registro y autenticación de usuarios.
 * 
 * @author Sistema HCC
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class AutenticacionServiceImpl implements AutenticacionService {

    private final UsuarioService usuarioService;
    private final UsuarioRepository userRepository;
    private final AuthenticationManager authenticationManager;

    /**
     * Valida que el DTO de usuario no sea nulo
     */
    private void validarUsuarioDTO(UsuarioDTO usuarioDTO) {
        if (usuarioDTO == null) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("usuarioDTO"));
        }
    }

    /**
     * Valida que el DTO de login no sea nulo y contenga datos válidos
     */
    private void validarLoginDTO(LoginUsuarioDTO loginUsuarioDTO) {
        if (loginUsuarioDTO == null) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("loginUsuarioDTO"));
        }
        
        if (loginUsuarioDTO.getNif() == null || loginUsuarioDTO.getNif().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("NIF"));
        }
        
        if (loginUsuarioDTO.getPassword() == null || loginUsuarioDTO.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("password"));
        }
    }

    /**
     * Crea el token de autenticación para el proceso de login
     */
    private UsernamePasswordAuthenticationToken crearTokenAutenticacion(LoginUsuarioDTO loginUsuarioDTO) {
        return new UsernamePasswordAuthenticationToken(
                loginUsuarioDTO.getNif(),
                loginUsuarioDTO.getPassword()
        );
    }

    /**
     * Extrae el usuario del principal de autenticación
     */
    private Usuario extraerUsuarioDelPrincipal(Object principal, String nif) {
        if (principal instanceof Usuario usuario) {
            return usuario; // Usuario ya viene con authorities desde UserDetailsService
        }
        
        // Fallback a repositorio (no debería ocurrir normalmente)
        return userRepository.findByNif(nif)
                .orElseThrow(() -> new IncorrectCredentials(ErrorMessages.ERROR_CREDENCIALES_INVALIDAS));
    }

    /**
     * Registra un nuevo usuario en el sistema
     * 
     * @param usuarioDTO Datos del usuario a registrar
     * @return Usuario registrado
     * @throws IllegalArgumentException si los datos son inválidos
     */
    @Override
    public Usuario registrar(UsuarioDTO usuarioDTO) {
        validarUsuarioDTO(usuarioDTO);
        return usuarioService.altaUsuario(usuarioDTO);
    }

    /**
     * Autentica un usuario en el sistema
     * 
     * @param loginUsuarioDTO Credenciales de login
     * @return Usuario autenticado
     * @throws IncorrectCredentials si las credenciales son incorrectas
     * @throws IllegalArgumentException si los datos de entrada son inválidos
     */
    @Override
    public Usuario autenticar(LoginUsuarioDTO loginUsuarioDTO) {
        validarLoginDTO(loginUsuarioDTO);
        
        try {
            var token = crearTokenAutenticacion(loginUsuarioDTO);
            var authentication = authenticationManager.authenticate(token);
            
            return extraerUsuarioDelPrincipal(authentication.getPrincipal(), loginUsuarioDTO.getNif());
            
        } catch (Exception e) {
            throw new IncorrectCredentials(ErrorMessages.ERROR_CREDENCIALES_INVALIDAS);
        }
    }

}
