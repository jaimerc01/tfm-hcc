package com.hcc.tfm_hcc.converter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.dto.AnotacionMedicaDTO;
import com.hcc.tfm_hcc.model.AnotacionMedica;
import com.hcc.tfm_hcc.model.Usuario;

/**
 * Convertidor para transformar entidades {@link AnotacionMedica} en {@link AnotacionMedicaDTO}.
 */
@Component
public class AnotacionMedicaConverter {

    /**
     * Convierte una entidad AnotacionMedica a AnotacionMedicaDTO.
     *
     * @param anotacion entidad AnotacionMedica
     * @return AnotacionMedicaDTO correspondiente, o {@code null} si la entrada es nula
     */
    public AnotacionMedicaDTO toDto(AnotacionMedica anotacion) {
        if (anotacion == null) {
            return null;
        }

        AnotacionMedicaDTO dto = new AnotacionMedicaDTO();
        dto.setId(anotacion.getId() != null ? anotacion.getId().toString() : null);
        dto.setMensaje(anotacion.getMensaje());
        dto.setCreatedAt(anotacion.getFechaCreacion() != null ? anotacion.getFechaCreacion().toString() : null);

        Usuario medico = anotacion.getMedico();
        if (medico != null) {
            dto.setMedicoNif(medico.getNif());
            dto.setMedicoNombre(nombreCompletoMedico(medico));
        }
        return dto;
    }

    /**
     * Convierte una lista de entidades AnotacionMedica a una lista de DTO.
     *
     * @param anotaciones lista de entidades AnotacionMedica
     * @return lista de AnotacionMedicaDTO, vacía si la entrada es nula
     */
    public List<AnotacionMedicaDTO> toDtoList(List<AnotacionMedica> anotaciones) {
        if (anotaciones == null) {
            return List.of();
        }
        return anotaciones.stream().map(this::toDto).toList();
    }

    private String nombreCompletoMedico(Usuario medico) {
        StringBuilder nombre = new StringBuilder();
        appendSiPresente(nombre, medico.getNombre());
        appendSiPresente(nombre, medico.getApellido1());
        appendSiPresente(nombre, medico.getApellido2());
        return nombre.toString();
    }

    private void appendSiPresente(StringBuilder builder, String parte) {
        if (parte != null && !parte.isBlank()) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(parte);
        }
    }
}
