package com.hcc.tfm_hcc.converter;

import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UsuarioConverter {

    private final HmacSearchIndexService hmacSearchIndexService;

    /**
     * Convierte un usuario a UsuarioDTO.
     *
     * @param usuario usuario a convertir
     * @return DTO de usuario
     */
    public UsuarioDTO toDto(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNombre(usuario.getNombre());
        dto.setApellido1(usuario.getApellido1());
        dto.setApellido2(usuario.getApellido2());
        dto.setNif(usuario.getNif());
        dto.setFechaNacimiento(usuario.getFechaNacimiento());
        dto.setEmail(usuario.getEmail());
        dto.setTelefono(usuario.getTelefono());
        dto.setId(usuario.getId().toString());
        dto.setEspecialidad(usuario.getEspecialidad());
        dto.setEstadoCuenta(usuario.getEstadoCuenta());
        dto.setLastPasswordChange(usuario.getLastPasswordChange());
        dto.setFechaEliminacion(usuario.getFechaEliminacion());

        return dto;
    }

    /**
     * Convierte un UsuarioDTO a usuario.
     *
     * @param dto DTO de usuario a convertir
     * @return usuario
     */
    public Usuario toEntity(UsuarioDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setApellido1(dto.getApellido1());
        usuario.setApellido2(dto.getApellido2());
        usuario.setNif(dto.getNif());
        usuario.setNifHash(hmacSearchIndexService.indexar(dto.getNif()));
        usuario.setFechaNacimiento(dto.getFechaNacimiento());
        usuario.setEmail(dto.getEmail());
        usuario.setEmailHash(hmacSearchIndexService.indexar(dto.getEmail()));
        usuario.setTelefono(dto.getTelefono());
        usuario.setEspecialidad(dto.getEspecialidad());
        usuario.setEstadoCuenta(dto.getEstadoCuenta());
        usuario.setLastPasswordChange(dto.getLastPasswordChange());
        usuario.setFechaEliminacion(dto.getFechaEliminacion());

        return usuario;
    }
}
