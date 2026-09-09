package com.hcc.tfm_hcc.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.hcc.tfm_hcc.controller.impl.PerfilControllerImpl;
import com.hcc.tfm_hcc.exception.PerfilNotFoundException;
import com.hcc.tfm_hcc.facade.PerfilFacade;
import com.hcc.tfm_hcc.model.Perfil;

class PerfilControllerImplTest {

    private MockMvc mvc;
    private PerfilFacade perfilFacade;

    @BeforeEach
    void setUp() {
        perfilFacade = mock(PerfilFacade.class);
        PerfilControllerImpl controller = new PerfilControllerImpl(perfilFacade);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getPerfilByRol_conRolExistente_normalizaYDevuelveElPerfil() throws Exception {
        Perfil perfil = new Perfil();
        perfil.setRol("MEDICO");
        when(perfilFacade.getPerfilByRol("MEDICO")).thenReturn(perfil);

        mvc.perform(get("/perfil/rol/medico")).andExpect(status().isOk());
    }

    @Test
    void getPerfilByRol_conRolInexistente_devuelveNotFound() throws Exception {
        when(perfilFacade.getPerfilByRol("INEXISTENTE")).thenThrow(new PerfilNotFoundException("INEXISTENTE"));

        mvc.perform(get("/perfil/rol/inexistente")).andExpect(status().isNotFound());
    }

    @Test
    void getPerfilByRol_conErrorInesperado_devuelve500() throws Exception {
        when(perfilFacade.getPerfilByRol("MEDICO")).thenThrow(new RuntimeException("fallo"));

        mvc.perform(get("/perfil/rol/medico")).andExpect(status().isInternalServerError());
    }
}
