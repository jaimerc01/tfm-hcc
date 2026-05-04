package com.hcc.tfm_hcc.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hcc.tfm_hcc.dto.ArchivoClinicoDTO;
import com.hcc.tfm_hcc.model.ArchivoClinico;

@Mapper(componentModel = "spring")
public interface ArchivoClinicoMapper extends BaseMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "uuidToString")
    ArchivoClinicoDTO toDto(ArchivoClinico entity);
}
