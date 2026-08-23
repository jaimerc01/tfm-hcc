package com.hcc.tfm_hcc.converter;

import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.dto.AntecedenteClinicoDTO;
import com.hcc.tfm_hcc.exception.DatosClinicosValidationException;
import com.hcc.tfm_hcc.model.AntecedenteClinico;
import com.hcc.tfm_hcc.model.HistorialClinico;

/**
 * Convertidor para transformar entidades {@link AntecedenteClinico} en {@link AntecedenteClinicoDTO}
 * y viceversa.
 */
@Component
public class AntecedenteClinicoConverter {

    /**
     * Convierte una entidad AntecedenteClinico a AntecedenteClinicoDTO.
     *
     * @param antecedente entidad AntecedenteClinico
     * @return AntecedenteClinicoDTO correspondiente
     */
    public AntecedenteClinicoDTO toDto(AntecedenteClinico antecedente) {
        AntecedenteClinicoDTO dto = new AntecedenteClinicoDTO();
        dto.setId(antecedente.getId() != null ? antecedente.getId().toString() : null);
        dto.setCategoria(antecedente.getCategoria() != null ? antecedente.getCategoria().name() : null);
        dto.setDescripcion(antecedente.getDescripcion());
        dto.setCreatedAt(antecedente.getFechaCreacion() != null ? antecedente.getFechaCreacion().toString() : null);
        return dto;
    }

    /**
     * Crea una nueva entidad AntecedenteClinico a partir de un DTO y el historial al que pertenece.
     *
     * @param dto AntecedenteClinicoDTO con categoría y descripción
     * @param historial HistorialClinico propietario del antecedente
     * @return entidad AntecedenteClinico sin persistir, lista para asociar fecha de creación y guardar
     * @throws DatosClinicosValidationException si la categoría no es "PERSONAL" o "FAMILIAR"
     */
    public AntecedenteClinico toEntity(AntecedenteClinicoDTO dto, HistorialClinico historial) {
        AntecedenteClinico antecedente = new AntecedenteClinico();
        antecedente.setCategoria(parseCategoria(dto.getCategoria()));
        antecedente.setDescripcion(dto.getDescripcion());
        antecedente.setHistorialClinico(historial);
        return antecedente;
    }

    /**
     * Parsea el texto de categoría a {@link AntecedenteClinico.Categoria}.
     *
     * @param categoria texto de categoría ("PERSONAL" o "FAMILIAR", sin distinguir mayúsculas)
     * @return la categoría parseada
     * @throws DatosClinicosValidationException si la categoría es nula o no reconocida
     */
    public AntecedenteClinico.Categoria parseCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            throw new DatosClinicosValidationException(ErrorMessages.campoRequerido("categoria"));
        }
        try {
            return AntecedenteClinico.Categoria.valueOf(categoria.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DatosClinicosValidationException(
                "La categoría del antecedente debe ser 'PERSONAL' o 'FAMILIAR'", e);
        }
    }
}
