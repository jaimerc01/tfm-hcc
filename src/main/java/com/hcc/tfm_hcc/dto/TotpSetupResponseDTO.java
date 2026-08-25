package com.hcc.tfm_hcc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Respuesta al iniciar la configuración del segundo factor (TOTP): el secreto
 * recién generado (aún pendiente de confirmar) y la URI {@code otpauth://} lista
 * para mostrar como código QR en la aplicación autenticadora.
 */
@Data
@AllArgsConstructor
public class TotpSetupResponseDTO {
    private String secret;
    private String otpauthUri;
}
