package com.hcc.tfm_hcc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.IncorrectCredentials;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.AutenticacionService;
import com.hcc.tfm_hcc.service.UsuarioService;

@Service
public class AutenticacionServiceImpl implements AutenticacionService {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository userRepository;
    
    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public Usuario registrar(UsuarioDTO usuarioDTO) {
        return usuarioService.altaUsuario(usuarioDTO);
    }

    @Override
    public Usuario autenticar(LoginUsuarioDTO loginUsuarioDTO) {
        var authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginUsuarioDTO.getNif(),
                loginUsuarioDTO.getPassword()
            )
        );
        Object principal = authentication.getPrincipal();
        if (principal instanceof Usuario u) {
            return u; // Usuario ya viene con authorities desde UserDetailsService
        }
        // Fallback a repositorio (no debería ocurrir)
        return userRepository.findByNif(loginUsuarioDTO.getNif())
            .orElseThrow(() -> new IncorrectCredentials("Credenciales incorrectas o usuario no existe"));
    }

}
