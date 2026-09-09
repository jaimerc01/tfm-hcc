package com.hcc.tfm_hcc.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.model.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioMapper extends BaseMapper {

    /**
     * {@code password} se ignora deliberadamente: {@code UsuarioDTO} se reutiliza tanto
     * como cuerpo de petición de alta/registro (donde sí necesita llevar la contraseña en
     * claro/cifrada de entrada, ver {@link #toEntity(UsuarioDTO)}) como cuerpo de respuesta
     * en múltiples endpoints (consulta de perfil propio, búsqueda de usuario por un
     * administrador, alta de usuario...). Sin este {@code ignore}, MapStruct copia por
     * nombre de campo y el hash BCrypt de {@code Usuario.password} viajaría tal cual en la
     * respuesta JSON de esos endpoints: varios de ellos ya no lo limpian manualmente antes
     * de devolver el DTO (a diferencia de {@code UsuarioControllerImpl.getUsuarioActual}),
     * así que la protección real tiene que estar aquí, no delegada a cada llamador.
     */
    @Mapping(target = "id", source = "id", qualifiedByName = "uuidToString")
    @Mapping(target = "password", ignore = true)
    UsuarioDTO toDto(Usuario usuario);

    @Mapping(target = "id", source = "id", qualifiedByName = "stringToUuid")
    @Mapping(target = "authorities", ignore = true)
    Usuario toEntity(UsuarioDTO usuarioDto);
}
