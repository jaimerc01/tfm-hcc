package com.hcc.tfm_hcc.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.dto.AntecedenteClinicoDTO;
import com.hcc.tfm_hcc.exception.DatosClinicosValidationException;
import com.hcc.tfm_hcc.model.AntecedenteClinico;
import com.hcc.tfm_hcc.model.HistorialClinico;

class AntecedenteClinicoConverterTest {

    private final AntecedenteClinicoConverter converter = new AntecedenteClinicoConverter();

    @Test
    void toDto_mapeaTodosLosCampos() {
        AntecedenteClinico antecedente = new AntecedenteClinico();
        antecedente.setId(UUID.randomUUID());
        antecedente.setCategoria(AntecedenteClinico.Categoria.FAMILIAR);
        antecedente.setDescripcion("Diabetes tipo 2");
        antecedente.setFechaCreacion(LocalDateTime.now());

        AntecedenteClinicoDTO dto = converter.toDto(antecedente);

        assertEquals(antecedente.getId().toString(), dto.getId());
        assertEquals("FAMILIAR", dto.getCategoria());
        assertEquals("Diabetes tipo 2", dto.getDescripcion());
    }

    @Test
    void toDto_conCategoriaNula_dejaElCampoNulo() {
        AntecedenteClinico antecedente = new AntecedenteClinico();

        assertNull(converter.toDto(antecedente).getCategoria());
    }

    @Test
    void toEntity_conCategoriaValida_creaLaEntidadAsociadaAlHistorial() {
        AntecedenteClinicoDTO dto = new AntecedenteClinicoDTO();
        dto.setCategoria("personal");
        dto.setDescripcion("Alergia estacional");
        HistorialClinico historial = new HistorialClinico();

        AntecedenteClinico entidad = converter.toEntity(dto, historial);

        assertEquals(AntecedenteClinico.Categoria.PERSONAL, entidad.getCategoria());
        assertEquals(historial, entidad.getHistorialClinico());
    }

    @Test
    void toEntity_conCategoriaInvalida_lanzaDatosClinicosValidationException() {
        AntecedenteClinicoDTO dto = new AntecedenteClinicoDTO();
        dto.setCategoria("DESCONOCIDA");

        assertThrows(DatosClinicosValidationException.class, () -> converter.toEntity(dto, new HistorialClinico()));
    }

    @Test
    void parseCategoria_conValorNulo_lanzaDatosClinicosValidationException() {
        assertThrows(DatosClinicosValidationException.class, () -> converter.parseCategoria(null));
    }

    @Test
    void parseCategoria_conEspaciosYMinusculas_normalizaCorrectamente() {
        assertEquals(AntecedenteClinico.Categoria.FAMILIAR, converter.parseCategoria("  familiar  "));
    }
}
