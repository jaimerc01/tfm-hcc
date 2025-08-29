package com.hcc.tfm_hcc.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcc.tfm_hcc.controller.impl.HistorialClinicoControllerImpl;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.facade.HistorialClinicoFacade;

@ExtendWith(MockitoExtension.class)
public class HistorialClinicoControllerIT {

    private MockMvc mvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private HistorialClinicoFacade historialClinicoFacade;

    @InjectMocks
    private HistorialClinicoControllerImpl controller;

    @BeforeEach
    void setup() {
        // Build MockMvc standalone with the controller; security filters are not applied here
        this.mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void deleteDatoClinico_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        // facade.delete(id) does not return; ensure it does not throw
        mvc.perform(delete("/historia/me/datos/" + id.toString()))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteAntecedente_returnsUpdatedDto() throws Exception {
        int index = 0;
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        dto.setAntecedentesFamiliares("[2025-01-01] entrada única");
        when(historialClinicoFacade.borrarAntecedente(index)).thenReturn(dto);

        mvc.perform(delete("/historia/me/antecedentes/" + index))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }

    @Test
    void editarAntecedente_returnsUpdatedDto() throws Exception {
        int index = 1;
        String nuevo = "texto modificado";
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        dto.setAntecedentesFamiliares("[2025-01-01] entrada primera\n\n[2025-02-01] " + nuevo);
        when(historialClinicoFacade.editarAntecedente(index, nuevo)).thenReturn(dto);

        mvc.perform(put("/historia/me/antecedentes/" + index)
                .contentType(MediaType.TEXT_PLAIN)
                .content(nuevo))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }
}
