package com.hcc.tfm_hcc.dto;

import lombok.Data;

/**
 * Datos identificativos mínimos de una persona (médico o paciente) para exponerlos
 * de forma segura anidados dentro de otros DTOs.
 *
 * <p>Se usa, por ejemplo, dentro de {@link SolicitudAsignacionDTO}: las entidades JPA
 * {@code SolicitudAsignacion.medico}/{@code paciente} son {@code Usuario} completos y, si
 * se serializaran tal cual, arrastrarían a la respuesta el hash BCrypt de la contraseña,
 * el secreto TOTP descifrado, el email, la fecha de nacimiento y los índices de búsqueda.
 * Este DTO limita lo que sale de la API a nombre, apellidos y NIF.</p>
 */
@Data
public class PersonaResumenDTO {

    private String nombre;
    private String apellido1;
    private String apellido2;
    private String nif;
}
