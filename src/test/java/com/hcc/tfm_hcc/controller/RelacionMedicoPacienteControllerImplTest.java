package com.hcc.tfm_hcc.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.hcc.tfm_hcc.controller.impl.RelacionMedicoPacienteControllerImpl;
import com.hcc.tfm_hcc.dto.MedicoResumenDTO;
import com.hcc.tfm_hcc.exception.RelacionMedicoPacienteNoEncontradaException;
import com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException;
import com.hcc.tfm_hcc.facade.RelacionMedicoPacienteFacade;

class RelacionMedicoPacienteControllerImplTest {

    private MockMvc mvc;
    private RelacionMedicoPacienteFacade facade;

    @BeforeEach
    void setUp() {
        facade = mock(RelacionMedicoPacienteFacade.class);
        mvc = MockMvcBuilders.standaloneSetup(new RelacionMedicoPacienteControllerImpl(facade)).build();
    }

    private MedicoResumenDTO medico(String nif) {
        MedicoResumenDTO dto = new MedicoResumenDTO();
        dto.setNif(nif);
        dto.setNombre("Ana");
        return dto;
    }

    @Test
    void listarMisMedicos_devuelveLaListaDelFacade() throws Exception {
        when(facade.listarMisMedicos()).thenReturn(List.of(medico("11111111A")));

        mvc.perform(get("/relaciones/mis-medicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nif").value("11111111A"));
    }

    @Test
    void desasignarMiMedico_conRelacionActiva_devuelveNoContent() throws Exception {
        mvc.perform(delete("/relaciones/mis-medicos/11111111A"))
                .andExpect(status().isNoContent());
        verify(facade).desasignarMiMedico("11111111A");
    }

    @Test
    void desasignarMiMedico_sinRelacionActiva_devuelveNotFound() throws Exception {
        doThrow(new RelacionMedicoPacienteNoEncontradaException("no activa"))
                .when(facade).desasignarMiMedico(anyString());

        mvc.perform(delete("/relaciones/mis-medicos/11111111A"))
                .andExpect(status().isNotFound());
    }

    @Test
    void desasignarMiPaciente_conRelacionActiva_devuelveNoContent() throws Exception {
        mvc.perform(delete("/relaciones/mis-pacientes/22222222B"))
                .andExpect(status().isNoContent());
        verify(facade).desasignarMiPaciente("22222222B");
    }

    @Test
    void desasignarMiPaciente_conPacienteInexistente_devuelveNotFound() throws Exception {
        doThrow(new UsuarioNoEncontradoException("no encontrado"))
                .when(facade).desasignarMiPaciente(anyString());

        mvc.perform(delete("/relaciones/mis-pacientes/00000000Z"))
                .andExpect(status().isNotFound());
    }
}
