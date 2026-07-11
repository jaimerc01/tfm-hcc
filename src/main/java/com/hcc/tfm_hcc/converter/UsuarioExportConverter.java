package com.hcc.tfm_hcc.converter;

import com.hcc.tfm_hcc.dto.UserExportDTO;
import com.hcc.tfm_hcc.dto.UserExportDTO.AccesoDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.model.AccessLog;
import com.hcc.tfm_hcc.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Convertidor para exportación de datos de usuario y logs de acceso.
 */
@Component
public class UsuarioExportConverter {

    /**
     * Convierte un AccessLog a AccesoDTO.
     *
     * @param log registro de acceso
     * @return DTO del acceso
     */
    public AccesoDTO toAccesoDto(AccessLog log) {
        return AccesoDTO.builder()
                .timestamp(log.getTimestamp())
                .metodo(log.getMetodo())
                .ruta(log.getRuta())
                .estado(log.getEstado())
                .duracionMs(log.getDuracionMs())
                .ip(log.getIp())
                .userAgent(log.getUserAgent())
                .build();
    }

    /**
     * Construye el DTO exportable del usuario.
     *
     * @param dto datos base del usuario
     * @param usuario entidad persistida para obtener campos no expuestos en UsuarioDTO
     * @param logs logs de acceso asociados
     * @return exportación completa del usuario
     */
    public UserExportDTO toExportDto(UsuarioDTO dto, Usuario usuario, List<AccessLog> logs) {
        List<AccesoDTO> accesos = logs.stream()
                .limit(500)
                .map(this::toAccesoDto)
                .toList();

        return UserExportDTO.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .apellido1(dto.getApellido1())
                .apellido2(dto.getApellido2())
                .email(dto.getEmail())
                .nif(dto.getNif())
                .telefono(dto.getTelefono())
                .fechaNacimiento(dto.getFechaNacimiento())
                .fechaCreacion(dto.getFechaCreacion())
                .fechaUltimaModificacion(dto.getFechaUltimaModificacion())
                .estadoCuenta(usuario != null ? usuario.getEstadoCuenta() : null)
                .fechaEliminacion(usuario != null ? usuario.getFechaEliminacion() : null)
                .accesos(accesos)
                .build();
    }
}