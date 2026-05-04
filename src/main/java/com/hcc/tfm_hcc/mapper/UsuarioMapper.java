package com.hcc.tfm_hcc.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.model.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioMapper extends BaseMapper {
    
    @Mapping(target = "id", source = "id", qualifiedByName = "uuidToString")
    UsuarioDTO toDto(Usuario usuario);

    @Mapping(target = "id", source = "id", qualifiedByName = "stringToUuid")
    @Mapping(target = "authorities", ignore = true)
    Usuario toEntity(UsuarioDTO usuarioDto);
}
