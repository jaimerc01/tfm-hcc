package com.hcc.tfm_hcc.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcc.tfm_hcc.controller.impl.AdminControllerImpl;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException;
import com.hcc.tfm_hcc.facade.AdminFacade;

class AdminControllerImplTest {

    private MockMvc mvc;
    private AdminFacade adminFacade;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        adminFacade = mock(AdminFacade.class);
        AdminControllerImpl controller = new AdminControllerImpl(adminFacade);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void listarMedicos_devuelveLaListaDelFacade() throws Exception {
        when(adminFacade.listarMedicos()).thenReturn(List.of(new UsuarioDTO()));

        mvc.perform(get("/admin/medicos")).andExpect(status().isOk());
    }

    @Test
    void listarMedicos_conErrorDelFacade_devuelve500() throws Exception {
        when(adminFacade.listarMedicos()).thenThrow(new RuntimeException("fallo"));

        mvc.perform(get("/admin/medicos")).andExpect(status().isInternalServerError());
    }

    @Test
    void buscarUsuarioPorNif_conNifValido_devuelveElUsuario() throws Exception {
        when(adminFacade.buscarUsuarioPorNif("12345678A")).thenReturn(new UsuarioDTO());

        mvc.perform(get("/admin/usuarios/by-nif").param("nif", "12345678A")).andExpect(status().isOk());
    }

    @Test
    void buscarUsuarioPorNif_conNifVacio_devuelveBadRequest() throws Exception {
        mvc.perform(get("/admin/usuarios/by-nif").param("nif", "")).andExpect(status().isBadRequest());
    }

    @Test
    void buscarUsuarioPorNif_conUsuarioInexistente_devuelveNotFound() throws Exception {
        when(adminFacade.buscarUsuarioPorNif("00000000Z"))
                .thenThrow(new UsuarioNoEncontradoException("Usuario con ID '***00Z' no encontrado"));

        mvc.perform(get("/admin/usuarios/by-nif").param("nif", "00000000Z")).andExpect(status().isNotFound());
    }

    @Test
    void crearMedico_conDatosValidos_devuelveElMedicoCreado() throws Exception {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNif("12345678A");
        when(adminFacade.crearMedico(ArgumentMatchers.any())).thenReturn(dto);

        mvc.perform(post("/admin/medicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void crearMedico_sinNif_devuelveBadRequest() throws Exception {
        UsuarioDTO dto = new UsuarioDTO();

        mvc.perform(post("/admin/medicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarMedico_conDatosValidos_devuelveElMedicoActualizado() throws Exception {
        UUID id = UUID.randomUUID();
        UsuarioDTO dto = new UsuarioDTO();
        when(adminFacade.actualizarMedico(ArgumentMatchers.eq(id), ArgumentMatchers.any())).thenReturn(dto);

        mvc.perform(put("/admin/medicos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void eliminarMedico_conIdValido_devuelveElIdEliminado() throws Exception {
        UUID id = UUID.randomUUID();
        when(adminFacade.eliminarMedico(id)).thenReturn(id);

        mvc.perform(delete("/admin/medicos/" + id)).andExpect(status().isOk());
    }

    @Test
    void setPerfilMedico_conParametroAsignar_devuelveOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(adminFacade.setPerfilMedico(id, true)).thenReturn(id);

        mvc.perform(put("/admin/medicos/" + id + "/perfil-medico").param("asignar", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void buscarUsuarioPorNif_conErrorInesperado_devuelve500() throws Exception {
        when(adminFacade.buscarUsuarioPorNif("12345678A")).thenThrow(new RuntimeException("fallo"));

        mvc.perform(get("/admin/usuarios/by-nif").param("nif", "12345678A"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void crearMedico_conCuerpoNulo_devuelveBadRequest() throws Exception {
        mvc.perform(post("/admin/medicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearMedico_conErrorInesperado_devuelve500() throws Exception {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNif("12345678A");
        when(adminFacade.crearMedico(ArgumentMatchers.any())).thenThrow(new RuntimeException("fallo"));

        mvc.perform(post("/admin/medicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void actualizarMedico_conCuerpoNulo_devuelveBadRequest() throws Exception {
        UUID id = UUID.randomUUID();

        mvc.perform(put("/admin/medicos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarMedico_conErrorInesperado_devuelve500() throws Exception {
        UUID id = UUID.randomUUID();
        UsuarioDTO dto = new UsuarioDTO();
        when(adminFacade.actualizarMedico(ArgumentMatchers.eq(id), ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("fallo"));

        mvc.perform(put("/admin/medicos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void eliminarMedico_conErrorInesperado_devuelve500() throws Exception {
        UUID id = UUID.randomUUID();
        when(adminFacade.eliminarMedico(id)).thenThrow(new RuntimeException("fallo"));

        mvc.perform(delete("/admin/medicos/" + id)).andExpect(status().isInternalServerError());
    }

    @Test
    void setPerfilMedico_conErrorInesperado_devuelve500() throws Exception {
        UUID id = UUID.randomUUID();
        when(adminFacade.setPerfilMedico(id, true)).thenThrow(new RuntimeException("fallo"));

        mvc.perform(put("/admin/medicos/" + id + "/perfil-medico").param("asignar", "true"))
                .andExpect(status().isInternalServerError());
    }
}
