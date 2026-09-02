package com.hcc.tfm_hcc.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.dto.MedicoResumenDTO;
import com.hcc.tfm_hcc.model.Usuario;

class MedicoResumenConverterTest {

    private final MedicoResumenConverter converter = new MedicoResumenConverter();

    private Usuario medico(String nif, String especialidad) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Ana");
        usuario.setApellido1("García");
        usuario.setApellido2("López");
        usuario.setNif(nif);
        usuario.setEspecialidad(especialidad);
        usuario.setEmail("no-deberia-verse@example.com");
        return usuario;
    }

    @Test
    void toDto_mapeaSoloLosCamposDeResumen() {
        MedicoResumenDTO dto = converter.toDto(medico("11111111A", "Cardiología"));

        assertEquals("Ana", dto.getNombre());
        assertEquals("García", dto.getApellido1());
        assertEquals("López", dto.getApellido2());
        assertEquals("11111111A", dto.getNif());
        assertEquals("Cardiología", dto.getEspecialidad());
    }

    @Test
    void toDto_conEntradaNula_devuelveNull() {
        assertNull(converter.toDto(null));
    }

    @Test
    void toDtoList_conEntradaNula_devuelveListaVacia() {
        assertTrue(converter.toDtoList(null).isEmpty());
    }

    @Test
    void toDtoList_mapeaCadaElemento() {
        List<MedicoResumenDTO> dtos = converter.toDtoList(List.of(
                medico("11111111A", "Cardiología"),
                medico("22222222B", "Neurología")));

        assertEquals(2, dtos.size());
        assertEquals("22222222B", dtos.get(1).getNif());
    }
}
