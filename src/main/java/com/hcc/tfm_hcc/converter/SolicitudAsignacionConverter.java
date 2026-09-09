package com.hcc.tfm_hcc.converter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.dto.PersonaResumenDTO;
import com.hcc.tfm_hcc.dto.SolicitudAsignacionDTO;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.model.Usuario;

/**
 * Convierte entidades {@link SolicitudAsignacion} en {@link SolicitudAsignacionDTO},
 * dejando fuera de la respuesta todo lo que no sea el estado de la solicitud, su fecha y
 * los datos identificativos mínimos del médico y del paciente.
 *
 * <p>Sigue el mismo patrón que {@link PacienteConverter}: un componente de conversión
 * manual en el paquete {@code converter}, sin lógica de negocio.</p>
 */
@Component
public class SolicitudAsignacionConverter {

    /**
     * Convierte una solicitud a su DTO. Devuelve {@code null} si la entidad es {@code null}.
     */
    public SolicitudAsignacionDTO toDto(SolicitudAsignacion solicitud) {
        if (solicitud == null) {
            return null;
        }

        SolicitudAsignacionDTO dto = new SolicitudAsignacionDTO();
        dto.setId(solicitud.getId() != null ? solicitud.getId().toString() : null);
        dto.setEstado(solicitud.getEstado());
        dto.setFechaCreacion(solicitud.getFechaCreacion());
        dto.setMedico(toPersonaResumen(solicitud.getMedico()));
        dto.setPaciente(toPersonaResumen(solicitud.getPaciente()));
        return dto;
    }

    /**
     * Convierte una lista de solicitudes a una lista de DTOs. Nunca devuelve {@code null}.
     */
    public List<SolicitudAsignacionDTO> toDtoList(List<SolicitudAsignacion> solicitudes) {
        if (solicitudes == null) {
            return List.of();
        }
        return solicitudes.stream().map(this::toDto).toList();
    }

    private PersonaResumenDTO toPersonaResumen(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        PersonaResumenDTO persona = new PersonaResumenDTO();
        persona.setNombre(usuario.getNombre());
        persona.setApellido1(usuario.getApellido1());
        persona.setApellido2(usuario.getApellido2());
        persona.setNif(usuario.getNif());
        return persona;
    }
}
