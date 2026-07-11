package com.hcc.tfm_hcc.converter;

import com.hcc.tfm_hcc.dto.RangoDTO;
import com.hcc.tfm_hcc.model.Rango;

import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * Convertidor para transformar entidades {@link Rango} en {@link RangoDTO} y viceversa.
 */
@Component
public class RangoConverter {

    /**
     * Convierte una entidad Rango a RangoDTO.
     *
     * @param rango entidad Rango
     * @return RangoDTO con valores numéricos parseados
     */
    public RangoDTO toDto(Rango rango) {
        RangoDTO dto = new RangoDTO();
        dto.setId(rango.getId() != null ? rango.getId().toString() : null);
        dto.setNombre(rango.getNombre());
        dto.setValorInferior(rango.getValorInferior());
        dto.setValorSuperior(rango.getValorSuperior());
        dto.setValorInferiorNumerico(parseValorNumerico(rango.getValorInferior()));
        dto.setValorSuperiorNumerico(parseValorNumerico(rango.getValorSuperior()));

        return dto;
    }

    /**
     * Convierte un RangoDTO a una entidad Rango.
     *
     * @param dto RangoDTO con información del rango
     * @return entidad Rango sin ID (para creación) o con ID (para actualización)
     */
    public Rango toEntity(RangoDTO dto) {
        Rango rango = new Rango();
        if (dto.getId() != null) {
            try {
                rango.setId(UUID.fromString(dto.getId()));
            } catch (IllegalArgumentException _) {
                // Manejar el caso donde el ID no es un UUID válido
                rango.setId(null);
            }
        }
        rango.setNombre(dto.getNombre());
        rango.setValorInferior(dto.getValorInferior());
        rango.setValorSuperior(dto.getValorSuperior());

        return rango;
    }

    private Double parseValorNumerico(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }

        String numStr = valor.trim().replaceAll("[^0-9.,]", "").replace(',', '.');
        if (numStr.isEmpty()) {
            return null;
        }

        try {
            return Double.parseDouble(numStr);
        } catch (NumberFormatException _) {
            return null;
        }
    }
}