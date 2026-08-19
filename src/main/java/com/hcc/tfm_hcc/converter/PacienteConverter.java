package com.hcc.tfm_hcc.converter;

import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.util.FechaUtils;
import org.springframework.stereotype.Component;

/**
 * Convertidor para transformar usuarios en DTOs de paciente.
 */
@Component
public class PacienteConverter {

    /**
     * Convierte un usuario a PacienteDTO.
     *
     * @param usuario usuario a convertir
     * @return DTO de paciente
     */
    public PacienteDTO toDto(Usuario usuario) {
        PacienteDTO dto = new PacienteDTO();
        dto.setNombre(usuario.getNombre());
        dto.setApellido1(usuario.getApellido1());
        dto.setApellido2(usuario.getApellido2());
        dto.setNif(usuario.getNif());
        dto.setFechaNacimiento(FechaUtils.toIsoDate(usuario.getFechaNacimiento()));
        return dto;
    }
}