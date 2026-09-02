package com.hcc.tfm_hcc.dto;

import lombok.Data;

/**
 * Datos identificativos mínimos de un médico asignado, para que el paciente pueda
 * ver y gestionar la relación asistencial sin exponer el resto de datos del médico
 * (email, contraseña, secreto TOTP, índices de búsqueda, etc.).
 */
@Data
public class MedicoResumenDTO {

    private String nombre;
    private String apellido1;
    private String apellido2;
    private String nif;
    private String especialidad;
}
