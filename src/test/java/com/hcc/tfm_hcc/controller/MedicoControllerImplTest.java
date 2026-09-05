package com.hcc.tfm_hcc.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.hcc.tfm_hcc.controller.impl.MedicoControllerImpl;
import com.hcc.tfm_hcc.converter.AnotacionMedicaConverter;
import com.hcc.tfm_hcc.converter.SolicitudAsignacionConverter;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.exception.PacienteNoEncontradoException;
import com.hcc.tfm_hcc.exception.SolicitudExistenteException;
import com.hcc.tfm_hcc.exception.UsuarioSinPermisoException;
import com.hcc.tfm_hcc.facade.MedicoFacade;
import com.hcc.tfm_hcc.model.AnotacionMedica;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.model.Usuario;

class MedicoControllerImplTest {

    private MockMvc mvc;
    private MedicoFacade medicoFacade;

    @BeforeEach
    void setUp() {
        medicoFacade = mock(MedicoFacade.class);
        MedicoControllerImpl controller = new MedicoControllerImpl(medicoFacade,
                new SolicitudAsignacionConverter(), new AnotacionMedicaConverter());
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
    void buscarPaciente_conErrorInesperado_devuelve500() throws Exception {
        when(medicoFacade.buscarPacientePorDniYFechaNacimiento("12345678A", "1990-05-20"))
                .thenThrow(new RuntimeException("fallo"));

        mvc.perform(get("/medico/pacientes/buscar")
                        .param("dni", "12345678A")
                        .param("fechaNacimiento", "1990-05-20"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void obtenerHistorialPaciente_conRelacionActiva_devuelveElHistorial() throws Exception {
        when(medicoFacade.obtenerHistorialPaciente("12345678A")).thenReturn(new HistorialClinicoDTO());

        mvc.perform(get("/medico/pacientes/12345678A/historial"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerHistorialPaciente_conPacienteNoEncontrado_devuelveNotFound() throws Exception {
        when(medicoFacade.obtenerHistorialPaciente("00000000Z"))
                .thenThrow(new PacienteNoEncontradoException("no encontrado"));

        mvc.perform(get("/medico/pacientes/00000000Z/historial"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerHistorialPaciente_sinRelacionActiva_devuelveForbidden() throws Exception {
        when(medicoFacade.obtenerHistorialPaciente("12345678A"))
                .thenThrow(new UsuarioSinPermisoException("acceso denegado"));

        mvc.perform(get("/medico/pacientes/12345678A/historial"))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtenerHistorialPaciente_conErrorInesperado_devuelve500() throws Exception {
        when(medicoFacade.obtenerHistorialPaciente("12345678A")).thenThrow(new RuntimeException("fallo"));

        mvc.perform(get("/medico/pacientes/12345678A/historial"))
                .andExpect(status().isInternalServerError());
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
    void crearSolicitudAsignacion_conErrorInesperado_devuelve500() throws Exception {
        when(medicoFacade.crearSolicitudAsignacion("22222222B")).thenThrow(new RuntimeException("fallo"));

        mvc.perform(post("/medico/solicitudes-asignacion").param("nifPaciente", "22222222B"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void listarSolicitudesPendientes_conErrorInesperado_devuelve500() throws Exception {
        when(medicoFacade.listarSolicitudesPendientes()).thenThrow(new RuntimeException("fallo"));

        mvc.perform(get("/medico/solicitudes-asignacion/pendientes"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void listarSolicitudesEnviadas_conErrorInesperado_devuelve500() throws Exception {
        when(medicoFacade.listarSolicitudesEnviadas()).thenThrow(new RuntimeException("fallo"));

        mvc.perform(get("/medico/solicitudes-asignacion/enviadas"))
                .andExpect(status().isInternalServerError());
    }

    // ---- anotaciones médicas ----

    private AnotacionMedica anotacionDe(String medicoNif, String mensaje) {
        Usuario medico = new Usuario();
        medico.setNif(medicoNif);
        medico.setNombre("Dr. Prueba");
        AnotacionMedica anotacion = new AnotacionMedica();
        anotacion.setMedico(medico);
        anotacion.setMensaje(mensaje);
        return anotacion;
    }

    @Test
    void crearAnotacion_conRelacionActiva_devuelveLaAnotacionCreada() throws Exception {
        when(medicoFacade.crearAnotacion("22222222B", "Revisar tensión"))
                .thenReturn(anotacionDe("11111111A", "Revisar tensión"));

        mvc.perform(post("/medico/pacientes/22222222B/anotaciones")
                        .contentType("application/json")
                        .content("{\"mensaje\":\"Revisar tensión\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void crearAnotacion_sinRelacionActiva_devuelveForbidden() throws Exception {
        when(medicoFacade.crearAnotacion("22222222B", "texto"))
                .thenThrow(new UsuarioSinPermisoException("acceso denegado"));

        mvc.perform(post("/medico/pacientes/22222222B/anotaciones")
                        .contentType("application/json")
                        .content("{\"mensaje\":\"texto\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearAnotacion_conPacienteNoEncontrado_devuelveNotFound() throws Exception {
        when(medicoFacade.crearAnotacion("00000000Z", "texto"))
                .thenThrow(new PacienteNoEncontradoException("no encontrado"));

        mvc.perform(post("/medico/pacientes/00000000Z/anotaciones")
                        .contentType("application/json")
                        .content("{\"mensaje\":\"texto\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearAnotacion_conErrorInesperado_devuelve500() throws Exception {
        when(medicoFacade.crearAnotacion("22222222B", "texto"))
                .thenThrow(new RuntimeException("fallo"));

        mvc.perform(post("/medico/pacientes/22222222B/anotaciones")
                        .contentType("application/json")
                        .content("{\"mensaje\":\"texto\"}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void listarAnotaciones_devuelveLaListaDelFacade() throws Exception {
        when(medicoFacade.listarAnotacionesPaciente("22222222B"))
                .thenReturn(List.of(new com.hcc.tfm_hcc.dto.AnotacionMedicaDTO()));

        mvc.perform(get("/medico/pacientes/22222222B/anotaciones")).andExpect(status().isOk());
    }

    @Test
    void listarAnotaciones_sinRelacionActiva_devuelveForbidden() throws Exception {
        when(medicoFacade.listarAnotacionesPaciente("22222222B"))
                .thenThrow(new UsuarioSinPermisoException("acceso denegado"));

        mvc.perform(get("/medico/pacientes/22222222B/anotaciones")).andExpect(status().isForbidden());
    }

    @Test
    void listarAnotaciones_conPacienteInexistente_devuelveNotFound() throws Exception {
        when(medicoFacade.listarAnotacionesPaciente("00000000Z"))
                .thenThrow(new PacienteNoEncontradoException("paciente no encontrado"));

        mvc.perform(get("/medico/pacientes/00000000Z/anotaciones")).andExpect(status().isNotFound());
    }

    // ---- documentos del paciente (CU-16) ----

    @Test
    void listarArchivosPaciente_devuelveLaListaDelFacade() throws Exception {
        when(medicoFacade.listarArchivosPaciente("22222222B"))
                .thenReturn(List.of(new com.hcc.tfm_hcc.dto.ArchivoClinicoDTO()));

        mvc.perform(get("/medico/pacientes/22222222B/archivos")).andExpect(status().isOk());
    }

    @Test
    void listarArchivosPaciente_sinRelacionActiva_devuelveForbidden() throws Exception {
        when(medicoFacade.listarArchivosPaciente("22222222B"))
                .thenThrow(new UsuarioSinPermisoException("acceso denegado"));

        mvc.perform(get("/medico/pacientes/22222222B/archivos")).andExpect(status().isForbidden());
    }

    @Test
    void subirArchivoPaciente_conRelacionActiva_devuelveElDto() throws Exception {
        when(medicoFacade.subirArchivoPaciente(org.mockito.ArgumentMatchers.eq("22222222B"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new com.hcc.tfm_hcc.dto.ArchivoClinicoDTO());

        mvc.perform(multipart("/medico/pacientes/22222222B/archivos")
                        .file(new org.springframework.mock.web.MockMultipartFile("file", "a.pdf", "application/pdf", "x".getBytes())))
                .andExpect(status().isOk());
    }

    @Test
    void descargarArchivoPaciente_conArchivoInexistente_devuelveNotFound() throws Exception {
        java.util.UUID id = java.util.UUID.randomUUID();
        when(medicoFacade.obtenerArchivoPaciente(org.mockito.ArgumentMatchers.eq("22222222B"), org.mockito.ArgumentMatchers.eq(id)))
                .thenThrow(new IllegalArgumentException("no existe"));

        mvc.perform(get("/medico/pacientes/22222222B/archivos/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void subirArchivoPaciente_sinRelacionActiva_devuelveForbidden() throws Exception {
        when(medicoFacade.subirArchivoPaciente(org.mockito.ArgumentMatchers.eq("22222222B"), org.mockito.ArgumentMatchers.any()))
                .thenThrow(new UsuarioSinPermisoException("acceso denegado"));

        mvc.perform(multipart("/medico/pacientes/22222222B/archivos")
                        .file(new org.springframework.mock.web.MockMultipartFile("file", "a.pdf", "application/pdf", "x".getBytes())))
                .andExpect(status().isForbidden());
    }

    @Test
    void subirArchivoPaciente_conArchivoInvalido_devuelveBadRequest() throws Exception {
        when(medicoFacade.subirArchivoPaciente(org.mockito.ArgumentMatchers.eq("22222222B"), org.mockito.ArgumentMatchers.any()))
                .thenThrow(new IllegalArgumentException("archivo vacío"));

        mvc.perform(multipart("/medico/pacientes/22222222B/archivos")
                        .file(new org.springframework.mock.web.MockMultipartFile("file", "a.pdf", "application/pdf", new byte[0])))
                .andExpect(status().isBadRequest());
    }

    @Test
    void descargarArchivoPaciente_conArchivo_devuelveElContenido() throws Exception {
        java.util.UUID id = java.util.UUID.randomUUID();
        com.hcc.tfm_hcc.dto.ArchivoClinicoDTO dto = new com.hcc.tfm_hcc.dto.ArchivoClinicoDTO();
        dto.setNombreOriginal("informe.pdf");
        dto.setContentType("application/pdf");
        when(medicoFacade.obtenerArchivoPaciente("22222222B", id)).thenReturn(dto);
        when(medicoFacade.descargarArchivoPaciente("22222222B", id))
                .thenReturn(new org.springframework.core.io.ByteArrayResource("contenido".getBytes()));

        mvc.perform(get("/medico/pacientes/22222222B/archivos/" + id)).andExpect(status().isOk());
    }
}
