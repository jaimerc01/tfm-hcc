package com.hcc.tfm_hcc.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.model.Usuario;

class PacienteConverterTest {

    private final PacienteConverter converter = new PacienteConverter();

    @Test
    void toDto_mapeaLosCamposBasicosYFormateaLaFecha() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Ana");
        usuario.setApellido1("García");
        usuario.setApellido2("López");
        usuario.setNif("12345678A");
        usuario.setFechaNacimiento(LocalDateTime.of(1990, 5, 20, 0, 0));

        PacienteDTO dto = converter.toDto(usuario);

        assertEquals("Ana", dto.getNombre());
        assertEquals("García", dto.getApellido1());
        assertEquals("López", dto.getApellido2());
        assertEquals("12345678A", dto.getNif());
        assertEquals("1990-05-20", dto.getFechaNacimiento());
    }

    @Test
    void toDto_conFechaNacimientoNula_devuelveFechaNula() {
        Usuario usuario = new Usuario();

        assertNull(converter.toDto(usuario).getFechaNacimiento());
    }
}
