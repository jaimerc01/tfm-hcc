package com.hcc.tfm_hcc.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.model.Usuario;

class UsuarioMapperTest {

    private UsuarioMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UsuarioMapperImpl();
    }

    /**
     * Regresión: UsuarioDTO se usa como cuerpo de respuesta en varios endpoints
     * (perfil propio, búsqueda de usuario por un administrador, alta de usuario...) y
     * ninguno de ellos debe filtrar nunca el hash BCrypt de la contraseña. Antes de
     * añadir {@code @Mapping(target = "password", ignore = true)} en
     * {@link UsuarioMapper#toDto(Usuario)}, MapStruct copiaba el campo por nombre y
     * ese hash viajaba tal cual en la respuesta JSON.
     */
    @Test
    void toDto_nuncaCopiaElHashDeLaContrasena() {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNombre("Ana");
        usuario.setApellido1("García");
        usuario.setNif("12345678A");
        usuario.setEmail("ana@example.com");
        usuario.setPassword("$2a$10$hashBcryptDeEjemploQueNuncaDeberiaSalirEnUnaRespuesta");

        UsuarioDTO dto = mapper.toDto(usuario);

        assertNull(dto.getPassword());
        assertEquals("Ana", dto.getNombre());
    }

    @Test
    void toEntity_siConservaLaContrasenaDeEntrada() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(UUID.randomUUID().toString());
        dto.setNombre("Ana");
        dto.setPassword("$2a$10$hashYaCifradoAlDarDeAltaAlUsuario");

        Usuario usuario = mapper.toEntity(dto);

        assertEquals("$2a$10$hashYaCifradoAlDarDeAltaAlUsuario", usuario.getPassword());
    }
}
