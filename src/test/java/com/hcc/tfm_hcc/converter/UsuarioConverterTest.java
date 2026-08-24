package com.hcc.tfm_hcc.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.model.Usuario;

class UsuarioConverterTest {

    private final UsuarioConverter converter = new UsuarioConverter();

    @Test
    void toDto_mapeaTodosLosCamposDelUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNombre("Ana");
        usuario.setApellido1("García");
        usuario.setApellido2("López");
        usuario.setNif("12345678A");
        usuario.setEmail("ana@example.com");
        usuario.setTelefono("600123456");
        usuario.setEspecialidad("Cardiología");
        usuario.setEstadoCuenta("ACTIVO");
        LocalDateTime ahora = LocalDateTime.now();
        usuario.setFechaNacimiento(ahora);
        usuario.setLastPasswordChange(ahora);
        usuario.setFechaEliminacion(ahora);

        UsuarioDTO dto = converter.toDto(usuario);

        assertEquals(usuario.getId().toString(), dto.getId());
        assertEquals("Ana", dto.getNombre());
        assertEquals("García", dto.getApellido1());
        assertEquals("López", dto.getApellido2());
        assertEquals("12345678A", dto.getNif());
        assertEquals("ana@example.com", dto.getEmail());
        assertEquals("600123456", dto.getTelefono());
        assertEquals("Cardiología", dto.getEspecialidad());
        assertEquals("ACTIVO", dto.getEstadoCuenta());
        assertEquals(ahora, dto.getFechaNacimiento());
        assertEquals(ahora, dto.getLastPasswordChange());
        assertEquals(ahora, dto.getFechaEliminacion());
    }

    @Test
    void toEntity_mapeaTodosLosCamposDelDto() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNombre("Ana");
        dto.setApellido1("García");
        dto.setApellido2("López");
        dto.setNif("12345678A");
        dto.setEmail("ana@example.com");
        dto.setTelefono("600123456");
        dto.setEspecialidad("Cardiología");
        dto.setEstadoCuenta("ACTIVO");
        LocalDateTime ahora = LocalDateTime.now();
        dto.setFechaNacimiento(ahora);
        dto.setLastPasswordChange(ahora);
        dto.setFechaEliminacion(ahora);

        Usuario usuario = converter.toEntity(dto);

        assertEquals("Ana", usuario.getNombre());
        assertEquals("García", usuario.getApellido1());
        assertEquals("López", usuario.getApellido2());
        assertEquals("12345678A", usuario.getNif());
        assertEquals("ana@example.com", usuario.getEmail());
        assertEquals("600123456", usuario.getTelefono());
        assertEquals("Cardiología", usuario.getEspecialidad());
        assertEquals("ACTIVO", usuario.getEstadoCuenta());
        assertEquals(ahora, usuario.getFechaNacimiento());
        assertEquals(ahora, usuario.getLastPasswordChange());
        assertEquals(ahora, usuario.getFechaEliminacion());
    }
}
