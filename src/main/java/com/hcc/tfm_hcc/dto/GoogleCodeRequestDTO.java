package com.hcc.tfm_hcc.dto;

import lombok.Data;

/**
 * Cuerpo de la petición de intercambio del código temporal de login con Google
 * por el token JWT de la aplicación.
 */
@Data
public class GoogleCodeRequestDTO {
    private String code;
}
