package com.hcc.tfm_hcc.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.dto.AlergiaDTO;
import com.hcc.tfm_hcc.model.Alergia;
import com.hcc.tfm_hcc.model.HistorialClinico;

class AlergiaConverterTest {

    private final AlergiaConverter converter = new AlergiaConverter();

    @Test
    void toDto_mapeaTodosLosCampos() {
        Alergia alergia = new Alergia();
        alergia.setId(UUID.randomUUID());
        alergia.setDescripcion("Alergia a la penicilina");
        alergia.setFechaCreacion(LocalDateTime.now());

        AlergiaDTO dto = converter.toDto(alergia);

        assertEquals(alergia.getId().toString(), dto.getId());
        assertEquals("Alergia a la penicilina", dto.getDescripcion());
    }

    @Test
    void toDto_conIdNulo_dejaElIdDtoNulo() {
        Alergia alergia = new Alergia();

        assertNull(converter.toDto(alergia).getId());
    }

    @Test
    void toEntity_asociaLaAlergiaAlHistorial() {
        AlergiaDTO dto = new AlergiaDTO();
        dto.setDescripcion("Alergia al polen");
        HistorialClinico historial = new HistorialClinico();

        Alergia entidad = converter.toEntity(dto, historial);

        assertEquals("Alergia al polen", entidad.getDescripcion());
        assertEquals(historial, entidad.getHistorialClinico());
    }
}
