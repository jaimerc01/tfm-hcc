package com.hcc.tfm_hcc.converter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.dto.MedicoResumenDTO;
import com.hcc.tfm_hcc.model.Usuario;

/**
 * Convertidor para transformar un {@link Usuario} con perfil médico en un
 * {@link MedicoResumenDTO} apto para exponerlo al paciente asignado.
 */
@Component
public class MedicoResumenConverter {

    /**
     * Convierte un usuario médico a MedicoResumenDTO.
     *
     * @param medico usuario médico
     * @return MedicoResumenDTO correspondiente, o {@code null} si la entrada es nula
     */
    public MedicoResumenDTO toDto(Usuario medico) {
        if (medico == null) {
            return null;
        }
        MedicoResumenDTO dto = new MedicoResumenDTO();
        dto.setNombre(medico.getNombre());
        dto.setApellido1(medico.getApellido1());
        dto.setApellido2(medico.getApellido2());
        dto.setNif(medico.getNif());
        dto.setEspecialidad(medico.getEspecialidad());
        return dto;
    }

    /**
     * Convierte una lista de usuarios médicos a una lista de DTO.
     *
     * @param medicos lista de usuarios médicos
     * @return lista de MedicoResumenDTO, vacía si la entrada es nula
     */
    public List<MedicoResumenDTO> toDtoList(List<Usuario> medicos) {
        if (medicos == null) {
            return List.of();
        }
        return medicos.stream().map(this::toDto).toList();
    }
}
