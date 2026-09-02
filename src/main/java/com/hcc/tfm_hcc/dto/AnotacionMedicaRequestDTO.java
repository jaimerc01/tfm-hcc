package com.hcc.tfm_hcc.dto;

import lombok.Data;

/**
 * Cuerpo de la petición para que un médico escriba una anotación sobre un
 * paciente al que tiene asignado.
 */
@Data
public class AnotacionMedicaRequestDTO {
    private String mensaje;
}
