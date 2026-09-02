package com.hcc.tfm_hcc.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcc.tfm_hcc.controller.impl.UsuarioControllerImpl;
import com.hcc.tfm_hcc.dto.UpdateUsuarioRequest;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;

class UsuarioControllerImplTest {

    private MockMvc mvc;
    private UsuarioFacade usuarioFacade;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        usuarioFacade = mock(UsuarioFacade.class);
        UsuarioControllerImpl controller = new UsuarioControllerImpl(usuarioFacade,
                new com.hcc.tfm_hcc.converter.SolicitudAsignacionConverter(),
                new com.hcc.tfm_hcc.converter.AnotacionMedicaConverter());
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private UsuarioDTO usuarioDto() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNif("12345678A");
        dto.setPassword("hash-no-deberia-verse");
        return dto;
    }

    @Test
    void getNombreUsuario_devuelveElNombreDelFacade() throws Exception {
        when(usuarioFacade.getNombreUsuario()).thenReturn("Ana García");

        mvc.perform(get("/usuario/nombre"))
                .andExpect(status().isOk())
                .andExpect(content().string("Ana García"));
    }

    @Test
    void getUsuarioActual_conUsuarioAutenticado_ocultaLaContrasena() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(usuarioDto());

        mvc.perform(get("/usuario/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nif").value("12345678A"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getUsuarioActual_sinUsuarioAutenticado_devuelve401() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(null);

        mvc.perform(get("/usuario/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_conDatosValidos_devuelveOk() throws Exception {
        mvc.perform(put("/usuario/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"actual123\",\"newPassword\":\"nuevaContrasena1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_conNuevaPasswordCorta_devuelveBadRequest() throws Exception {
        mvc.perform(put("/usuario/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"actual123\",\"newPassword\":\"abc\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarMisSolicitudes_conUsuarioAutenticado_devuelveLaLista() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(usuarioDto());
        when(usuarioFacade.listarMisSolicitudes()).thenReturn(List.of(new SolicitudAsignacion()));

        mvc.perform(get("/usuario/solicitudes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void listarMisSolicitudes_sinUsuarioAutenticado_devuelve401() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(null);

        mvc.perform(get("/usuario/solicitudes")).andExpect(status().isUnauthorized());
    }

    @Test
    void actualizarEstadoSolicitud_conEstadoValido_devuelveLaSolicitudActualizada() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(usuarioDto());
        SolicitudAsignacion actualizada = new SolicitudAsignacion();
        actualizada.setEstado("ACEPTADA");
        when(usuarioFacade.actualizarEstadoSolicitud(eq("id-1"), eq("ACEPTADA"))).thenReturn(actualizada);

        mvc.perform(put("/usuario/solicitudes/id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"ACEPTADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACEPTADA"));
    }

    @Test
    void actualizarEstadoSolicitud_sinEstadoEnElCuerpo_devuelveBadRequest() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(usuarioDto());

        mvc.perform(put("/usuario/solicitudes/id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarEstadoSolicitud_conAccesoDenegado_devuelve403() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(usuarioDto());
        when(usuarioFacade.actualizarEstadoSolicitud(eq("id-1"), eq("ACEPTADA")))
                .thenThrow(new IllegalStateException("no autorizado"));

        mvc.perform(put("/usuario/solicitudes/id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"ACEPTADA\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUsuarioActual_conNifSinCambios_noIncluyeCabeceraDeReautenticacion() throws Exception {
        UsuarioDTO actual = usuarioDto();
        UsuarioDTO actualizado = usuarioDto();
        when(usuarioFacade.getUsuarioActual()).thenReturn(actual);
        when(usuarioFacade.updateUsuarioActual(any())).thenReturn(actualizado);

        UpdateUsuarioRequest req = new UpdateUsuarioRequest();
        req.setNombre("Ana");
        req.setApellido1("García");
        req.setNif("12345678A");
        req.setEmail("ana@example.com");

        mvc.perform(put("/usuario/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("X-Reauth-Required"));
    }

    @Test
    void updateUsuarioActual_conNifCambiado_incluyeCabeceraDeReautenticacion() throws Exception {
        UsuarioDTO actual = usuarioDto();
        UsuarioDTO actualizado = usuarioDto();
        actualizado.setNif("87654321B");
        when(usuarioFacade.getUsuarioActual()).thenReturn(actual);
        when(usuarioFacade.updateUsuarioActual(any())).thenReturn(actualizado);

        UpdateUsuarioRequest req = new UpdateUsuarioRequest();
        req.setNombre("Ana");
        req.setApellido1("García");
        req.setNif("87654321B");
        req.setEmail("ana@example.com");

        mvc.perform(put("/usuario/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Reauth-Required", "true"));
    }

    @Test
    void deleteCuenta_devuelveNoContent() throws Exception {
        mvc.perform(delete("/usuario/me")).andExpect(status().isNoContent());
    }

    @Test
    void misLogs_sinRangoDeFechas_devuelveOk() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(usuarioDto());
        when(usuarioFacade.getMisLogs(null, null)).thenReturn(List.of());

        mvc.perform(get("/usuario/logs")).andExpect(status().isOk());
    }

    @Test
    void exportUsuario_devuelveOk() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(usuarioDto());
        when(usuarioFacade.exportUsuario(any())).thenReturn(null);

        mvc.perform(get("/usuario/export")).andExpect(status().isOk());
    }

    // ---- ramas de error adicionales (catch genérico -> 500, @ResponseStatus en UsuarioOperacionException) ----

    @Test
    void getUsuarioActual_conErrorInesperado_devuelve500() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenThrow(new RuntimeException("fallo"));

        mvc.perform(get("/usuario/me")).andExpect(status().isInternalServerError());
    }

    @Test
    void changePassword_conCuerpoNulo_devuelveBadRequest() throws Exception {
        mvc.perform(put("/usuario/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_conErrorInesperado_devuelve500() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("fallo"))
                .when(usuarioFacade).changePassword(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        mvc.perform(put("/usuario/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"actual123\",\"newPassword\":\"nuevaContrasena1\"}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void listarMisSolicitudes_conErrorInesperado_devuelve500() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(usuarioDto());
        when(usuarioFacade.listarMisSolicitudes()).thenThrow(new RuntimeException("fallo"));

        mvc.perform(get("/usuario/solicitudes")).andExpect(status().isInternalServerError());
    }

    @Test
    void actualizarEstadoSolicitud_sinUsuarioAutenticado_devuelve401() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(null);

        mvc.perform(put("/usuario/solicitudes/id-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"ACEPTADA\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateUsuarioActual_conErrorDeValidacion_devuelveBadRequest() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(usuarioDto());
        when(usuarioFacade.updateUsuarioActual(any())).thenThrow(new IllegalArgumentException("nif ya en uso"));

        UpdateUsuarioRequest req = new UpdateUsuarioRequest();
        req.setNombre("Ana");
        req.setApellido1("García");
        req.setNif("12345678A");
        req.setEmail("ana@example.com");

        mvc.perform(put("/usuario/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUsuarioActual_conErrorInesperado_devuelve500() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(usuarioDto());
        when(usuarioFacade.updateUsuarioActual(any())).thenThrow(new RuntimeException("fallo"));

        UpdateUsuarioRequest req = new UpdateUsuarioRequest();
        req.setNombre("Ana");
        req.setApellido1("García");
        req.setNif("12345678A");
        req.setEmail("ana@example.com");

        mvc.perform(put("/usuario/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteCuenta_conUsuarioNoAutenticado_devuelve401() throws Exception {
        org.mockito.Mockito.doThrow(new com.hcc.tfm_hcc.exception.UsuarioNoAutenticadoException("no autenticado"))
                .when(usuarioFacade).deleteCuentaActual(any());

        mvc.perform(delete("/usuario/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void deleteCuenta_sinContrasenaDeConfirmacion_devuelve401ConCabeceraReauth() throws Exception {
        org.mockito.Mockito.doThrow(new com.hcc.tfm_hcc.exception.ReautenticacionRequeridaException("reautenticación requerida"))
                .when(usuarioFacade).deleteCuentaActual(any());

        mvc.perform(delete("/usuario/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("X-Reauth-Required", "true"));
    }

    @Test
    void exportUsuario_sinContrasenaDeConfirmacion_devuelve401ConCabeceraReauth() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(usuarioDto());
        org.mockito.Mockito.doThrow(new com.hcc.tfm_hcc.exception.ReautenticacionRequeridaException("reautenticación requerida"))
                .when(usuarioFacade).exportUsuario(any());

        mvc.perform(get("/usuario/export"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("X-Reauth-Required", "true"));
    }

    @Test
    void deleteCuenta_conErrorInesperado_devuelve500() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("fallo")).when(usuarioFacade).deleteCuentaActual(any());

        mvc.perform(delete("/usuario/me")).andExpect(status().isInternalServerError());
    }

    @Test
    void misLogs_conRangoDeFechas_delegaEnElFacadeConLasFechasParseadas() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(usuarioDto());
        when(usuarioFacade.getMisLogs(any(), any())).thenReturn(List.of());

        mvc.perform(get("/usuario/logs")
                        .param("desde", "2024-01-01T00:00:00")
                        .param("hasta", "2024-12-31T23:59:59"))
                .andExpect(status().isOk());
    }

    @Test
    void misLogs_sinUsuarioAutenticado_devuelve401() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(null);

        mvc.perform(get("/usuario/logs")).andExpect(status().isUnauthorized());
    }

    @Test
    void exportUsuario_sinUsuarioAutenticado_devuelve401() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(null);

        mvc.perform(get("/usuario/export")).andExpect(status().isUnauthorized());
    }

    // ---- anotaciones médicas ----

    @Test
    void listarMisAnotaciones_conUsuarioAutenticado_devuelveLaLista() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(usuarioDto());
        when(usuarioFacade.listarMisAnotaciones(any(), any(), any()))
                .thenReturn(List.of(new com.hcc.tfm_hcc.model.AnotacionMedica()));

        mvc.perform(get("/usuario/anotaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void listarMisAnotaciones_conFiltroDeMedicoYFechas_delegaEnElFacade() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(usuarioDto());
        when(usuarioFacade.listarMisAnotaciones(eq("11111111A"), any(), any())).thenReturn(List.of());

        mvc.perform(get("/usuario/anotaciones")
                        .param("medicoNif", "11111111A")
                        .param("desde", "2024-01-01T00:00:00")
                        .param("hasta", "2024-12-31T23:59:59"))
                .andExpect(status().isOk());
    }

    @Test
    void listarMisAnotaciones_sinUsuarioAutenticado_devuelve401() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(null);

        mvc.perform(get("/usuario/anotaciones")).andExpect(status().isUnauthorized());
    }

    @Test
    void listarMisAnotaciones_conFechaMalFormada_devuelveBadRequest() throws Exception {
        when(usuarioFacade.getUsuarioActual()).thenReturn(usuarioDto());

        mvc.perform(get("/usuario/anotaciones").param("desde", "no-es-una-fecha"))
                .andExpect(status().isBadRequest());
    }
}
