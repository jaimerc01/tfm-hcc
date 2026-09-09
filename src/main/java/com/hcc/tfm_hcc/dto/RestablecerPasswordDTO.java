package com.hcc.tfm_hcc.dto;

import lombok.Data;

/**
 * Cuerpo de la petición que completa el restablecimiento de contraseña: el token
 * recibido en el enlace de correo y la nueva contraseña elegida por el usuario.
 */
@Data
public class RestablecerPasswordDTO {
    private String token;
    private String nuevaPassword;
}
