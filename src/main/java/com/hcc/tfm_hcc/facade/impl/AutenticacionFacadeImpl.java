package com.hcc.tfm_hcc.facade.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.IncorrectCredentials;
import com.hcc.tfm_hcc.facade.AutenticacionFacade;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.LoginResponse;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.service.AutenticacionService;
import com.hcc.tfm_hcc.service.JwtService;

@Component
public class AutenticacionFacadeImpl implements AutenticacionFacade {

    @Autowired
    private AutenticacionService autenticacionService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioMapper usuarioMapper;
    
    @Override
    public ResponseEntity<LoginResponse> autenticar(LoginUsuarioDTO loginUsuarioDTO) throws IncorrectCredentials {
        Usuario usuarioAutenticado = autenticacionService.autenticar(loginUsuarioDTO);
        String jwtToken = jwtService.generateToken(usuarioAutenticado);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpirationTime(jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }

    @Override
    public ResponseEntity<UsuarioDTO> registrar(UsuarioDTO usuarioDTO) {
        Usuario usuarioRegistrado = autenticacionService.registrar(usuarioDTO);
        UsuarioDTO usuarioResponse = usuarioMapper.toDto(usuarioRegistrado);
        
        return ResponseEntity.ok(usuarioResponse);
    }
}