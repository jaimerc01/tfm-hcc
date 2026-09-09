package com.hcc.tfm_hcc.converter;

import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.dto.AlergiaDTO;
import com.hcc.tfm_hcc.model.Alergia;
import com.hcc.tfm_hcc.model.HistorialClinico;

/**
 * Convertidor para transformar entidades {@link Alergia} en {@link AlergiaDTO} y viceversa.
 */
@Component
public class AlergiaConverter {

    /**
     * Convierte una entidad Alergia a AlergiaDTO.
     *
     * @param alergia entidad Alergia
     * @return AlergiaDTO correspondiente
     */
    public AlergiaDTO toDto(Alergia alergia) {
        AlergiaDTO dto = new AlergiaDTO();
        dto.setId(alergia.getId() != null ? alergia.getId().toString() : null);
        dto.setDescripcion(alergia.getDescripcion());
        dto.setCreatedAt(alergia.getFechaCreacion() != null ? alergia.getFechaCreacion().toString() : null);
        return dto;
    }

    /**
     * Crea una nueva entidad Alergia a partir de un DTO y el historial al que pertenece.
     *
     * @param dto AlergiaDTO con la descripción de la alergia
     * @param historial HistorialClinico propietario de la alergia
     * @return entidad Alergia sin persistir, lista para asociar fecha de creación y guardar
     */
    public Alergia toEntity(AlergiaDTO dto, HistorialClinico historial) {
        Alergia alergia = new Alergia();
        alergia.setDescripcion(dto.getDescripcion());
        alergia.setHistorialClinico(historial);
        return alergia;
    }
}
