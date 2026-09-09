package com.hcc.tfm_hcc.dto;

import lombok.Data;

/**
 * Cuerpo de la petición que inicia el flujo de restablecimiento de contraseña:
 * el usuario indica el correo de su cuenta y el sistema le envía (si esa cuenta
 * existe) un enlace con un token de un solo uso.
 */
@Data
public class SolicitudRestablecerPasswordDTO {
    private String email;
}
