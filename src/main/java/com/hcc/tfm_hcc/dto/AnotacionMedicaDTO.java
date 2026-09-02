package com.hcc.tfm_hcc.dto;

import lombok.Data;

/**
 * DTO de salida para una anotación médica: observación de texto libre que un
 * médico registra sobre un paciente asignado, visible para el propio paciente.
 */
@Data
public class AnotacionMedicaDTO {
    private String id;
    private String medicoNif;
    private String medicoNombre;
    private String mensaje;
    private String createdAt;
}
