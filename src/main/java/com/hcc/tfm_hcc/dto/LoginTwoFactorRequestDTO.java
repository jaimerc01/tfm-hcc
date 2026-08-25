package com.hcc.tfm_hcc.dto;

import lombok.Data;

/**
 * Cuerpo de la petición del segundo paso del login cuando el usuario tiene
 * activado el segundo factor (TOTP): el identificador del reto emitido por el
 * primer paso ({@code /authentication/login}) y el código de 6 dígitos.
 */
@Data
public class LoginTwoFactorRequestDTO {
    private String challengeId;
    private String code;
}
