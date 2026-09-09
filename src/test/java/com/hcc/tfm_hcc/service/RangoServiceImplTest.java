package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.exception.RangoOperacionException;
import com.hcc.tfm_hcc.model.Rango;
import com.hcc.tfm_hcc.repository.RangoRepository;
import com.hcc.tfm_hcc.service.impl.RangoServiceImpl;

class RangoServiceImplTest {

    private RangoRepository rangoRepository;
    private RangoServiceImpl service;

    @BeforeEach
    void setUp() {
        rangoRepository = mock(RangoRepository.class);
        service = new RangoServiceImpl(rangoRepository);
    }

    @Test
    void listarTodos_devuelveLosRangosDelRepositorio() {
        Rango rango = new Rango();
        rango.setNombre("Glucosa en sangre - Normal");
        when(rangoRepository.findAll()).thenReturn(List.of(rango));

        List<Rango> resultado = service.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("Glucosa en sangre - Normal", resultado.get(0).getNombre());
    }

    @Test
    void listarTodos_conErrorDeRepositorio_lanzaRangoOperacionException() {
        when(rangoRepository.findAll()).thenThrow(new RuntimeException("fallo de BD"));

        assertThrows(RangoOperacionException.class, () -> service.listarTodos());
    }
}
