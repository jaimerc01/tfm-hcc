package com.hcc.tfm_hcc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PerfilNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PerfilNotFoundException(String rol) {
        super("Perfil not found with rol: " + rol);
    }
    
}
