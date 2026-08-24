package com.hcc.tfm_hcc.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.dto.RangoDTO;
import com.hcc.tfm_hcc.model.Rango;

class RangoConverterTest {

    private final RangoConverter converter = new RangoConverter();

    @Test
    void toDto_conValoresNumericosSimples_losParsea() {
        Rango rango = new Rango();
        rango.setId(UUID.randomUUID());
        rango.setNombre("Glucosa en sangre - Normal");
        rango.setValorInferior("70");
        rango.setValorSuperior("140");

        RangoDTO dto = converter.toDto(rango);

        assertEquals(70.0, dto.getValorInferiorNumerico());
        assertEquals(140.0, dto.getValorSuperiorNumerico());
        assertEquals(rango.getId().toString(), dto.getId());
    }

    @Test
    void toDto_conUnidadesYComaDecimal_extraeElNumero() {
        Rango rango = new Rango();
        rango.setValorInferior("36,5 °C");
        rango.setValorSuperior("37,5 °C");

        RangoDTO dto = converter.toDto(rango);

        assertEquals(36.5, dto.getValorInferiorNumerico());
        assertEquals(37.5, dto.getValorSuperiorNumerico());
    }

    @Test
    void toDto_conValorNulo_devuelveNumericoNulo() {
        Rango rango = new Rango();

        RangoDTO dto = converter.toDto(rango);

        assertNull(dto.getValorInferiorNumerico());
        assertNull(dto.getValorSuperiorNumerico());
    }

    @Test
    void toDto_conValorSinDigitos_devuelveNumericoNulo() {
        Rango rango = new Rango();
        rango.setValorInferior("n/a");

        RangoDTO dto = converter.toDto(rango);

        assertNull(dto.getValorInferiorNumerico());
    }

    @Test
    void toDto_conIdNulo_devuelveIdDtoNulo() {
        Rango rango = new Rango();

        assertNull(converter.toDto(rango).getId());
    }

    @Test
    void toEntity_conIdValido_loParsea() {
        UUID id = UUID.randomUUID();
        RangoDTO dto = new RangoDTO();
        dto.setId(id.toString());
        dto.setNombre("Presión arterial");
        dto.setValorInferior("90");
        dto.setValorSuperior("120");

        Rango rango = converter.toEntity(dto);

        assertEquals(id, rango.getId());
        assertEquals("Presión arterial", rango.getNombre());
    }

    @Test
    void toEntity_conIdConFormatoInvalido_dejaElIdNulo() {
        RangoDTO dto = new RangoDTO();
        dto.setId("no-es-un-uuid");

        assertNull(converter.toEntity(dto).getId());
    }

    @Test
    void toEntity_conIdNulo_dejaElIdNulo() {
        RangoDTO dto = new RangoDTO();

        assertNull(converter.toEntity(dto).getId());
    }
}
