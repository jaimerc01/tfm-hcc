package com.hcc.tfm_hcc.dto;

import lombok.Data;

/**
 * Cuerpo de la petición para confirmar la activación del segundo factor (TOTP)
 * o para desactivarlo: en ambos casos se exige probar la posesión del secreto
 * mediante un código de 6 dígitos vigente.
 */
@Data
public class TotpCodeRequestDTO {
    private String code;
}
