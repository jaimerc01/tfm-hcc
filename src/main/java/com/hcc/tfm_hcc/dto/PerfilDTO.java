package com.hcc.tfm_hcc.dto;

import lombok.Data;

/**
 * Representación de un perfil/rol tal como cruza la API.
 *
 * <p>Sustituye a la exposición directa de la entidad JPA {@code Perfil} en la respuesta del
 * controlador de perfiles, para no arrastrar los campos de auditoría heredados de
 * {@code BaseEntity} ni acoplar el contrato REST al modelo de persistencia.</p>
 */
@Data
public class PerfilDTO {

    private String id;
    private String rol;
}
