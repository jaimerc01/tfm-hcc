package com.hcc.tfm_hcc.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * Representación de una solicitud de asignación médico-paciente tal como cruza la API.
 *
 * <p>Sustituye a la exposición directa de la entidad JPA {@code SolicitudAsignacion} en las
 * respuestas de los controladores. La entidad tiene asociaciones {@code @ManyToOne} a
 * {@code Usuario} (médico y paciente) que Jackson serializaría por completo, filtrando datos
 * sensibles (contraseña, secreto TOTP, email, fecha de nacimiento, hashes de búsqueda). Aquí
 * solo viajan el estado de la solicitud, su fecha y los datos identificativos mínimos de las
 * dos personas implicadas.</p>
 */
@Data
public class SolicitudAsignacionDTO {

    private String id;
    private String estado;
    private LocalDateTime fechaCreacion;
    private PersonaResumenDTO medico;
    private PersonaResumenDTO paciente;
}
