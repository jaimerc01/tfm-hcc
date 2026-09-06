package com.hcc.tfm_hcc.dto;

import lombok.Data;

/**
 * Cuerpo de la petición con la que un paciente resuelve una propuesta de cambio clínico
 * enviada por un médico: {@code aceptar = true} aplica el cambio sobre su historial;
 * {@code aceptar = false} lo rechaza.
 */
@Data
public class ResponderPropuestaRequestDTO {

    /** {@code true} para aceptar la propuesta (y aplicar el cambio), {@code false} para rechazarla. */
    private Boolean aceptar;
}
