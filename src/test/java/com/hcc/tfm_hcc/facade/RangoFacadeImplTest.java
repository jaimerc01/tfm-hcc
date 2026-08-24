package com.hcc.tfm_hcc.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.converter.RangoConverter;
import com.hcc.tfm_hcc.dto.RangoDTO;
import com.hcc.tfm_hcc.exception.RangoOperacionException;
import com.hcc.tfm_hcc.facade.impl.RangoFacadeImpl;
import com.hcc.tfm_hcc.model.Rango;
import com.hcc.tfm_hcc.service.RangoService;

class RangoFacadeImplTest {

    private RangoService rangoService;
    private RangoConverter rangoConverter;
    private RangoFacadeImpl facade;

    @BeforeEach
    void setUp() {
        rangoService = mock(RangoService.class);
        rangoConverter = mock(RangoConverter.class);
        facade = new RangoFacadeImpl(rangoService, rangoConverter);
    }

    @Test
    void obtenerTodosLosRangos_devuelveLosRangosConvertidos() {
        Rango rango = new Rango();
        RangoDTO dto = new RangoDTO();
        when(rangoService.listarTodos()).thenReturn(List.of(rango));
        when(rangoConverter.toDto(rango)).thenReturn(dto);

        List<RangoDTO> resultado = facade.obtenerTodosLosRangos();

        assertEquals(1, resultado.size());
        assertEquals(dto, resultado.get(0));
    }

    @Test
    void obtenerTodosLosRangos_conErrorEnElServicio_lanzaRangoOperacionException() {
        when(rangoService.listarTodos()).thenThrow(new RuntimeException("fallo de BD"));

        assertThrows(RangoOperacionException.class, () -> facade.obtenerTodosLosRangos());
    }
}
