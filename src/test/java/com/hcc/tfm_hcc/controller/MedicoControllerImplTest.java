package com.hcc.tfm_hcc.controller;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.hcc.tfm_hcc.controller.impl.MedicoControllerImpl;
import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.exception.MedicoOperacionException;
import com.hcc.tfm_hcc.exception.SolicitudExistenteException;
import com.hcc.tfm_hcc.facade.MedicoFacade;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;

class MedicoControllerImplTest {

    private MockMvc mvc;
    private MedicoFacade medicoFacade;

    @BeforeEach
    void setUp() {
        medicoFacade = mock(MedicoFacade.class);
        MedicoControllerImpl controller = new MedicoControllerImpl(medicoFacade);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void buscarPaciente_conPacienteEncontrado_devuelveElDto() throws Exception {
        when(medicoFacade.buscarPacientePorDniYFechaNacimiento("12345678A", "1990-05-20"))
                .thenReturn(new PacienteDTO());

        mvc.perform(get("/medico/pacientes/buscar")
                        .param("dni", "12345678A")
                        .param("fechaNacimiento", "1990-05-20"))
                .andExpect(status().isOk());
    }

    @Test
    void buscarPaciente_conPacienteNoEncontrado_devuelveNotFound() throws Exception {
        when(medicoFacade.buscarPacientePorDniYFechaNacimiento("00000000Z", "1990-05-20")).thenReturn(null);

        mvc.perform(get("/medico/pacientes/buscar")
                        .param("dni", "00000000Z")
                        .param("fechaNacimiento", "1990-05-20"))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarPaciente_conErrorInesperado_propagaMedicoOperacionException() {
        when(medicoFacade.buscarPacientePorDniYFechaNacimiento("12345678A", "1990-05-20"))
                .thenThrow(new RuntimeException("fallo"));

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class,
                () -> mvc.perform(get("/medico/pacientes/buscar")
                        .param("dni", "12345678A")
                        .param("fechaNacimiento", "1990-05-20")));
        assertInstanceOf(MedicoOperacionException.class, ex.getCause());
    }

    @Test
    void crearSolicitudAsignacion_conSolicitudCreada_devuelveLaSolicitud() throws Exception {
        when(medicoFacade.crearSolicitudAsignacion("22222222B")).thenReturn(new SolicitudAsignacion());

        mvc.perform(post("/medico/solicitudes-asignacion").param("nifPaciente", "22222222B"))
                .andExpect(status().isOk());
    }

    @Test
    void crearSolicitudAsignacion_conSolicitudYaExistente_devuelveConflict() throws Exception {
        when(medicoFacade.crearSolicitudAsignacion("22222222B"))
                .thenThrow(new SolicitudExistenteException("ya existe"));

        mvc.perform(post("/medico/solicitudes-asignacion").param("nifPaciente", "22222222B"))
                .andExpect(status().isConflict());
    }

    @Test
    void listarSolicitudesPendientes_devuelveLaListaDelFacade() throws Exception {
        when(medicoFacade.listarSolicitudesPendientes()).thenReturn(List.of(new SolicitudAsignacion()));

        mvc.perform(get("/medico/solicitudes-asignacion/pendientes")).andExpect(status().isOk());
    }

    @Test
    void listarSolicitudesEnviadas_devuelveLaListaDelFacade() throws Exception {
        when(medicoFacade.listarSolicitudesEnviadas()).thenReturn(List.of());

        mvc.perform(get("/medico/solicitudes-asignacion/enviadas")).andExpect(status().isOk());
    }

    @Test
    void crearSolicitudAsignacion_conSolicitudNula_devuelveBadRequest() throws Exception {
        when(medicoFacade.crearSolicitudAsignacion("22222222B")).thenReturn(null);

        mvc.perform(post("/medico/solicitudes-asignacion").param("nifPaciente", "22222222B"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearSolicitudAsignacion_conErrorInesperado_propagaMedicoOperacionException() {
        when(medicoFacade.crearSolicitudAsignacion("22222222B")).thenThrow(new RuntimeException("fallo"));

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class,
                () -> mvc.perform(post("/medico/solicitudes-asignacion").param("nifPaciente", "22222222B")));
        assertInstanceOf(MedicoOperacionException.class, ex.getCause());
    }

    @Test
    void listarSolicitudesPendientes_conErrorInesperado_propagaMedicoOperacionException() {
        when(medicoFacade.listarSolicitudesPendientes()).thenThrow(new RuntimeException("fallo"));

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class,
                () -> mvc.perform(get("/medico/solicitudes-asignacion/pendientes")));
        assertInstanceOf(MedicoOperacionException.class, ex.getCause());
    }

    @Test
    void listarSolicitudesEnviadas_conErrorInesperado_propagaMedicoOperacionException() {
        when(medicoFacade.listarSolicitudesEnviadas()).thenThrow(new RuntimeException("fallo"));

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class,
                () -> mvc.perform(get("/medico/solicitudes-asignacion/enviadas")));
        assertInstanceOf(MedicoOperacionException.class, ex.getCause());
    }
}
