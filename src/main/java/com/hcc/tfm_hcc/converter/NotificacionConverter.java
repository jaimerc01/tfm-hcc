package com.hcc.tfm_hcc.converter;

import com.hcc.tfm_hcc.dto.NotificacionDTO;
import com.hcc.tfm_hcc.model.Notificacion;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * Convertidor para transformar entidades {@link Notificacion} en {@link NotificacionDTO} y viceversa.
 */
@Component
@RequiredArgsConstructor
public class NotificacionConverter {

    private final UsuarioConverter usuarioConverter;

    /**
     * Convierte una entidad Notificacion a NotificacionDTO.
     *
     * @param notificacion entidad Notificacion
     * @return NotificacionDTO con valores numéricos parseados
     */
    public NotificacionDTO toDto(Notificacion notificacion) {
        NotificacionDTO dto = new NotificacionDTO();
        dto.setId(notificacion.getId() != null ? notificacion.getId().toString() : null);
        dto.setLeida(notificacion.isLeida());
        dto.setMensaje(notificacion.getMensaje());
        dto.setFechaCreacion(notificacion.getFechaCreacion());
        dto.setUsuarioDTO(notificacion.getUsuario() != null ? usuarioConverter.toDto(notificacion.getUsuario()) : null);
        dto.asignarEnlace(notificacion);

        return dto;
    }

    /**
     * Convierte un NotificacionDTO a una entidad Notificacion.
     *
     * @param dto NotificacionDTO con información de la notificación
     * @return entidad Notificacion sin ID (para creación) o con ID (para actualización)
     */
    public Notificacion toEntity(NotificacionDTO dto) {
        Notificacion notificacion = new Notificacion();
        if (dto.getId() != null) {
            try {
                notificacion.setId(UUID.fromString(dto.getId()));
            } catch (IllegalArgumentException _) {
                // Manejar el caso donde el ID no es un UUID válido
                notificacion.setId(null);
            }
        }
        notificacion.setLeida(dto.isLeida());
        notificacion.setMensaje(dto.getMensaje());
        notificacion.setFechaCreacion(dto.getFechaCreacion());
        notificacion.setUsuario(dto.getUsuarioDTO() != null ? usuarioConverter.toEntity(dto.getUsuarioDTO()) : null);

        return notificacion;
    }

    /**
     * Convierte una lista de entidades Notificacion a una lista de NotificacionDTO.
     * @param notificaciones Lista de entidades Notificacion
     * @return Lista de NotificacionDTO
     */
    public List<NotificacionDTO> toDtoList(List<Notificacion> notificaciones) {
        return notificaciones.stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Convierte una lista de NotificacionDTO a una lista de entidades Notificacion.
     * @param dtos Lista de NotificacionDTO
     * @return Lista de entidades Notificacion
     */
    public List<Notificacion> toEntityList(List<NotificacionDTO> dtos) {
        return dtos.stream()
                .map(this::toEntity)
                .toList();
    }
}