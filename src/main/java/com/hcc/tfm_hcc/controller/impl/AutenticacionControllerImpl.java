package com.hcc.tfm_hcc.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcc.tfm_hcc.controller.AutenticacionController;
import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.facade.AutenticacionFacade;
import com.hcc.tfm_hcc.model.LoginResponse;

@RestController
@RequestMapping("/authentication")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class AutenticacionControllerImpl implements AutenticacionController {

    @Autowired
    private AutenticacionFacade autenticacionFacade;

    @Override
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> autenticar(@RequestBody LoginUsuarioDTO loginUsuarioDTO) {
        return autenticacionFacade.autenticar(loginUsuarioDTO);
    }
    
    @Override
    @PostMapping("/signup")
    public ResponseEntity<UsuarioDTO> registrar(@RequestBody UsuarioDTO usuarioDTO) {
        if (usuarioDTO.getFechaNacimiento() == null) {
            return ResponseEntity.badRequest().build();
        }
        return autenticacionFacade.registrar(usuarioDTO);
    }
}
