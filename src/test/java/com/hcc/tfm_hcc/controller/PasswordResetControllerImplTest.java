package com.hcc.tfm_hcc.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.hcc.tfm_hcc.controller.impl.PasswordResetControllerImpl;
import com.hcc.tfm_hcc.dto.RestablecerPasswordDTO;
import com.hcc.tfm_hcc.dto.SolicitudRestablecerPasswordDTO;
import com.hcc.tfm_hcc.exception.PasswordResetTokenInvalidoException;
import com.hcc.tfm_hcc.facade.PasswordResetFacade;

class PasswordResetControllerImplTest {

    private MockMvc mvc;
    private PasswordResetFacade facade;

    @BeforeEach
    void setUp() {
        facade = mock(PasswordResetFacade.class);
        mvc = MockMvcBuilders.standaloneSetup(new PasswordResetControllerImpl(facade)).build();
    }

    @Test
    void solicitar_conCorreo_responde204YDelegaEnElFacade() throws Exception {
        mvc.perform(post("/authentication/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@example.com\"}"))
                .andExpect(status().isNoContent());

        verify(facade).solicitarRestablecimiento(any(SolicitudRestablecerPasswordDTO.class));
    }

    @Test
    void solicitar_siElFacadeFalla_sigueRespondiendo204() throws Exception {
        doThrow(new RuntimeException("boom")).when(facade).solicitarRestablecimiento(any());

        mvc.perform(post("/authentication/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@example.com\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void confirmar_conTokenValido_responde204() throws Exception {
        mvc.perform(post("/authentication/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"t\",\"nuevaPassword\":\"nuevaClaveSegura\"}"))
                .andExpect(status().isNoContent());

        verify(facade).restablecerPassword(any(RestablecerPasswordDTO.class));
    }

    @Test
    void confirmar_conTokenInvalido_responde400() throws Exception {
        doThrow(new PasswordResetTokenInvalidoException("no válido")).when(facade).restablecerPassword(any());

        mvc.perform(post("/authentication/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"t\",\"nuevaPassword\":\"nuevaClaveSegura\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmar_conErrorInesperado_responde500() throws Exception {
        doThrow(new RuntimeException("bd caída")).when(facade).restablecerPassword(any());

        mvc.perform(post("/authentication/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"t\",\"nuevaPassword\":\"nuevaClaveSegura\"}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void confirmar_conContrasenaDebil_responde400() throws Exception {
        doThrow(new IllegalArgumentException("demasiado corta")).when(facade).restablecerPassword(any());

        mvc.perform(post("/authentication/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"t\",\"nuevaPassword\":\"corta\"}"))
                .andExpect(status().isBadRequest());
    }
}
