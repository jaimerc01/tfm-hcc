package com.hcc.tfm_hcc.converter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcc.tfm_hcc.dto.AlergiaDTO;
import com.hcc.tfm_hcc.dto.AntecedenteClinicoDTO;
import com.hcc.tfm_hcc.dto.DatoClinicoEntradaDTO;
import com.hcc.tfm_hcc.dto.PersonaResumenDTO;
import com.hcc.tfm_hcc.dto.PropuestaCambioClinicoDTO;
import com.hcc.tfm_hcc.model.PropuestaCambioClinico;
import com.hcc.tfm_hcc.model.Usuario;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Convierte entidades {@link PropuestaCambioClinico} en {@link PropuestaCambioClinicoDTO},
 * dejando fuera de la respuesta las asociaciones JPA completas y parseando el {@code payloadJson}
 * al sub-DTO que corresponda según el dominio.
 *
 * <p>Sigue el mismo patrón que {@link SolicitudAsignacionConverter}: un componente de conversión
 * manual en el paquete {@code converter}, sin lógica de negocio.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PropuestaCambioClinicoConverter {

    private final ObjectMapper objectMapper;

    /**
     * Convierte una propuesta a su DTO. Devuelve {@code null} si la entidad es {@code null}.
     */
    public PropuestaCambioClinicoDTO toDto(PropuestaCambioClinico propuesta) {
        if (propuesta == null) {
            return null;
        }

        PropuestaCambioClinicoDTO dto = new PropuestaCambioClinicoDTO();
        dto.setId(propuesta.getId() != null ? propuesta.getId().toString() : null);
        dto.setDominio(propuesta.getDominio() != null ? propuesta.getDominio().name() : null);
        dto.setOperacion(propuesta.getOperacion() != null ? propuesta.getOperacion().name() : null);
        dto.setEstado(propuesta.getEstado());
        dto.setIdRecursoObjetivo(propuesta.getIdRecursoObjetivo());
        dto.setMotivo(propuesta.getMotivo());
        dto.setDescripcionActual(propuesta.getDescripcionActual());
        dto.setFechaCreacion(propuesta.getFechaCreacion());
        dto.setFechaResolucion(propuesta.getFechaResolucion());
        dto.setMedico(toPersonaResumen(propuesta.getMedico()));
        dto.setPaciente(toPersonaResumen(propuesta.getPaciente()));

        rellenarPayload(propuesta, dto);
        return dto;
    }

    /**
     * Convierte una lista de propuestas a una lista de DTOs. Nunca devuelve {@code null}.
     */
    public List<PropuestaCambioClinicoDTO> toDtoList(List<PropuestaCambioClinico> propuestas) {
        if (propuestas == null) {
            return List.of();
        }
        return propuestas.stream().map(this::toDto).toList();
    }

    /**
     * Parsea el {@code payloadJson} de la propuesta al sub-DTO que corresponda según su dominio.
     * Un JSON ilegible (no debería ocurrir: lo escribe la propia aplicación) se registra y se
     * deja el sub-DTO a nulo, sin abortar la conversión.
     */
    private void rellenarPayload(PropuestaCambioClinico propuesta, PropuestaCambioClinicoDTO dto) {
        String payload = propuesta.getPayloadJson();
        if (payload == null || payload.isBlank() || propuesta.getDominio() == null) {
            return;
        }
        try {
            switch (propuesta.getDominio()) {
                case ANTECEDENTE -> dto.setAntecedente(objectMapper.readValue(payload, AntecedenteClinicoDTO.class));
                case ALERGIA -> dto.setAlergia(objectMapper.readValue(payload, AlergiaDTO.class));
                case ANALISIS_SANGRE, SIGNOS_VITALES, ANALISIS_ORINA ->
                        dto.setMedicion(objectMapper.readValue(payload, DatoClinicoEntradaDTO.class));
            }
        } catch (JsonProcessingException e) {
            log.warn("No se pudo parsear el payload de la propuesta {}: {}", dto.getId(), e.getMessage());
        }
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
