package com.hcc.tfm_hcc.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.hcc.tfm_hcc.controller.impl.RangoControllerImpl;
import com.hcc.tfm_hcc.dto.RangoDTO;
import com.hcc.tfm_hcc.facade.RangoFacade;

class RangoControllerImplTest {

    private MockMvc mvc;
    private RangoFacade rangoFacade;

    @BeforeEach
    void setUp() {
        rangoFacade = mock(RangoFacade.class);
        RangoControllerImpl controller = new RangoControllerImpl(rangoFacade);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void obtenerTodosLosRangos_devuelveLaListaDelFacade() throws Exception {
        RangoDTO dto = new RangoDTO();
        dto.setNombre("Glucosa - Normal");
        when(rangoFacade.obtenerTodosLosRangos()).thenReturn(List.of(dto));

        mvc.perform(get("/rangos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Glucosa - Normal"));
    }
}
