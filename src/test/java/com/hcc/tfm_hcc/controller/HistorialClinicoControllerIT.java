package com.hcc.tfm_hcc.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.hcc.tfm_hcc.dto.AntecedenteClinicoDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.facade.HistorialClinicoFacade;

@ExtendWith(MockitoExtension.class)
class HistorialClinicoControllerIT {

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

    @SuppressWarnings("null")
    @Test
    void deleteAntecedente_returnsUpdatedDto() throws Exception {
        UUID id = UUID.randomUUID();
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        when(historialClinicoFacade.borrarAntecedente(id)).thenReturn(dto);

        mvc.perform(delete("/historia/antecedentes/" + id))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }

    @SuppressWarnings("null")
    @Test
    void editarAntecedente_returnsUpdatedDto() throws Exception {
        UUID id = UUID.randomUUID();
        AntecedenteClinicoDTO body = new AntecedenteClinicoDTO();
        body.setCategoria("PERSONAL");
        body.setDescripcion("texto modificado");
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        when(historialClinicoFacade.editarAntecedente(eq(id), any(AntecedenteClinicoDTO.class))).thenReturn(dto);

        mvc.perform(put("/historia/antecedentes/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }
}
