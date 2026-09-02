package com.hcc.tfm_hcc.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcc.tfm_hcc.controller.impl.HistorialClinicoControllerImpl;
import com.hcc.tfm_hcc.dto.AlergiaDTO;
import com.hcc.tfm_hcc.dto.AntecedenteClinicoDTO;
import com.hcc.tfm_hcc.dto.ArchivoClinicoDTO;
import com.hcc.tfm_hcc.dto.DatoClinicoEntradaDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.exception.ArchivoClinicoException;
import com.hcc.tfm_hcc.exception.HistorialClinicoException;
import com.hcc.tfm_hcc.facade.HistorialClinicoFacade;

class HistorialClinicoControllerImplTest {

    /**
     * Sin un {@code @ControllerAdvice} registrado (no se usa en este MockMvc standalone) y sin
     * {@code @ResponseStatus} en la excepción, el servlet la envuelve en ServletException;
     * esta ayuda desenvuelve la causa real para comprobar qué excepción de dominio se propagó.
     */
    private static Throwable causaReal(jakarta.servlet.ServletException servletException) {
        return servletException.getCause();
    }

    private MockMvc mvc;
    /** Igual que {@link #mvc} pero con el {@code GlobalExceptionHandler} real registrado,
     *  para comprobar el código de estado HTTP que acaba viendo el cliente. */
    private MockMvc mvcConAdvice;
    private HistorialClinicoFacade facade;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        facade = mock(HistorialClinicoFacade.class);
        HistorialClinicoControllerImpl controller = new HistorialClinicoControllerImpl(facade);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
        mvcConAdvice = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new com.hcc.tfm_hcc.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void editarAntecedente_conDatoNoPropio_devuelve400ConElMensajeReal() throws Exception {
        UUID id = UUID.randomUUID();
        // La fachada envuelve el IllegalArgumentException de la capa de servicio.
        when(facade.editarAntecedente(eq(id), any())).thenThrow(new HistorialClinicoException(
                "Error interno durante la edición del antecedente",
                new IllegalArgumentException(com.hcc.tfm_hcc.constants.ErrorMessages.ERROR_NO_PERMITIDO)));

        mvcConAdvice.perform(put("/historia/antecedentes/" + id)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(com.hcc.tfm_hcc.constants.ErrorMessages.ERROR_NO_PERMITIDO));
    }

    @Test
    void crearAntecedente_conFalloRealDelServidor_devuelve500() throws Exception {
        when(facade.crearAntecedente(any())).thenThrow(new HistorialClinicoException(
                "Error interno durante la creación del antecedente", new NullPointerException("bug")));

        mvcConAdvice.perform(post("/historia/antecedentes")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void listarArchivos_devuelveLaListaDelFacade() throws Exception {
        when(facade.listarArchivos()).thenReturn(List.of(new ArchivoClinicoDTO()));

        mvc.perform(get("/historia/archivos"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{}]"));
    }

    @Test
    void listarArchivos_conErrorDelFacade_propagaArchivoClinicoException() {
        when(facade.listarArchivos()).thenThrow(new RuntimeException("fallo"));

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class,
                () -> mvc.perform(get("/historia/archivos")));
        assertInstanceOf(ArchivoClinicoException.class, causaReal(ex));
    }

    @Test
    void subirArchivo_conArchivoValido_devuelveElDto() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "informe.pdf", "application/pdf", "contenido".getBytes());
        ArchivoClinicoDTO dto = new ArchivoClinicoDTO();
        dto.setId("id-1");
        when(facade.upload(any())).thenReturn(dto);

        mvc.perform(multipart("/historia/archivos").file(file))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }

    @Test
    void subirArchivo_sinArchivo_propagaDatosClinicosValidationException() {
        MockMultipartFile file = new MockMultipartFile("file", "", "application/pdf", new byte[0]);

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class,
                () -> mvc.perform(multipart("/historia/archivos").file(file)));
        assertInstanceOf(com.hcc.tfm_hcc.exception.DatosClinicosValidationException.class, causaReal(ex));
    }

    @Test
    void descargarArchivo_conArchivoExistente_devuelveElRecurso() throws Exception {
        UUID id = UUID.randomUUID();
        ArchivoClinicoDTO dto = new ArchivoClinicoDTO();
        dto.setNombreOriginal("informe.pdf");
        dto.setContentType("application/pdf");
        when(facade.getArchivoClinico(id)).thenReturn(dto);
        Resource resource = new ByteArrayResource("contenido".getBytes());
        when(facade.getMineResource(id)).thenReturn(resource);

        mvc.perform(get("/historia/archivos/" + id))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Content-Disposition", org.hamcrest.Matchers.containsString("informe.pdf")));
    }

    @Test
    void eliminarArchivo_conIdValido_devuelveElIdEliminado() throws Exception {
        UUID id = UUID.randomUUID();

        mvc.perform(delete("/historia/archivos/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string("\"" + id + "\""));
    }

    @Test
    void getMiHistoria_conHistorialExistente_devuelveElDto() throws Exception {
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        when(facade.obtenerMiHistoria()).thenReturn(dto);

        mvc.perform(get("/historia"))
                .andExpect(status().isOk());
    }

    @Test
    void getMiHistoria_conFacadeNulo_devuelveDtoVacio() throws Exception {
        when(facade.obtenerMiHistoria()).thenReturn(null);

        mvc.perform(get("/historia"))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }

    @Test
    void crearAntecedente_devuelveElHistorialActualizado() throws Exception {
        AntecedenteClinicoDTO body = new AntecedenteClinicoDTO();
        body.setCategoria("PERSONAL");
        body.setDescripcion("Hipertensión");
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        when(facade.crearAntecedente(any(AntecedenteClinicoDTO.class))).thenReturn(dto);

        mvc.perform(post("/historia/antecedentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void editarAntecedente_devuelveElHistorialActualizado() throws Exception {
        UUID id = UUID.randomUUID();
        AntecedenteClinicoDTO body = new AntecedenteClinicoDTO();
        body.setCategoria("PERSONAL");
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        when(facade.editarAntecedente(eq(id), any(AntecedenteClinicoDTO.class))).thenReturn(dto);

        mvc.perform(put("/historia/antecedentes/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }

    @Test
    void borrarAntecedente_devuelveElHistorialActualizado() throws Exception {
        UUID id = UUID.randomUUID();
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        when(facade.borrarAntecedente(id)).thenReturn(dto);

        mvc.perform(delete("/historia/antecedentes/" + id))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }

    @Test
    void crearAlergia_devuelveElHistorialActualizado() throws Exception {
        AlergiaDTO body = new AlergiaDTO();
        body.setDescripcion("Alergia a la penicilina");
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        when(facade.crearAlergia(any(AlergiaDTO.class))).thenReturn(dto);

        mvc.perform(post("/historia/alergias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void borrarAlergia_devuelveElHistorialActualizado() throws Exception {
        UUID id = UUID.randomUUID();
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        when(facade.borrarAlergia(id)).thenReturn(dto);

        mvc.perform(delete("/historia/alergias/" + id))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }

    private static final String CUERPO_MEDICIONES = "[{\"label\":\"Glucosa\",\"value\":\"90\"}]";

    @Test
    void actualizarAnalisisSangre_deserializaElArrayYLoReenviaAlFacade() throws Exception {
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        when(facade.actualizarAnalisisSangre(any())).thenReturn(dto);

        mvc.perform(put("/historia/analisis-sangre")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_MEDICIONES))
                .andExpect(status().isOk());
    }

    @Test
    void crearAnalisisSangre_delegaEnAnadirAnalisisSangre() throws Exception {
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        when(facade.anadirAnalisisSangre(any())).thenReturn(dto);

        mvc.perform(post("/historia/analisis-sangre")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_MEDICIONES))
                .andExpect(status().isOk());
    }

    @Test
    void actualizarSignosVitales_devuelveElHistorialActualizado() throws Exception {
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        when(facade.actualizarSignosVitales(any())).thenReturn(dto);

        mvc.perform(put("/historia/signos-vitales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_MEDICIONES))
                .andExpect(status().isOk());
    }

    @Test
    void crearSignosVitales_devuelveElHistorialActualizado() throws Exception {
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        when(facade.anadirSignosVitales(any())).thenReturn(dto);

        mvc.perform(post("/historia/signos-vitales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_MEDICIONES))
                .andExpect(status().isOk());
    }

    @Test
    void actualizarAnalisisOrina_devuelveElHistorialActualizado() throws Exception {
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        when(facade.actualizarAnalisisOrina(any())).thenReturn(dto);

        mvc.perform(put("/historia/analisis-orina")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_MEDICIONES))
                .andExpect(status().isOk());
    }

    @Test
    void crearAnalisisOrina_devuelveElHistorialActualizado() throws Exception {
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        when(facade.anadirAnalisisOrina(any())).thenReturn(dto);

        mvc.perform(post("/historia/analisis-orina")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_MEDICIONES))
                .andExpect(status().isOk());
    }

    @Test
    void actualizarAnalisisSangre_conCuerpoQueNoEsUnArray_devuelve400() throws Exception {
        mvc.perform(put("/historia/analisis-sangre")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"glucosa\":90}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarAnalisisSangre_conErrorDelFacade_propagaHistorialClinicoException() {
        when(facade.actualizarAnalisisSangre(any())).thenThrow(new RuntimeException("fallo"));

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class, () -> mvc.perform(
                put("/historia/analisis-sangre").contentType(MediaType.APPLICATION_JSON).content(CUERPO_MEDICIONES)));
        assertInstanceOf(HistorialClinicoException.class, causaReal(ex));
    }

    @Test
    void borrarDatoClinico_devuelveElIdEliminado() throws Exception {
        UUID id = UUID.randomUUID();

        mvc.perform(delete("/historia/datos-clinicos/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string("\"" + id + "\""));
    }

    @Test
    void borrarDatoClinico_conErrorDelFacade_propagaHistorialClinicoException() {
        UUID id = UUID.randomUUID();
        doThrow(new RuntimeException("fallo")).when(facade).borrarDatoClinico(id);

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class,
                () -> mvc.perform(delete("/historia/datos-clinicos/" + id)));
        assertInstanceOf(HistorialClinicoException.class, causaReal(ex));
    }

    // ---- ramas adicionales: content-type / nombre de archivo por defecto ----

    @Test
    void descargarArchivo_conContentTypeNulo_usaOctetStreamPorDefecto() throws Exception {
        UUID id = UUID.randomUUID();
        ArchivoClinicoDTO dto = new ArchivoClinicoDTO();
        dto.setNombreOriginal("informe.pdf");
        dto.setContentType(null);
        when(facade.getArchivoClinico(id)).thenReturn(dto);
        when(facade.getMineResource(id)).thenReturn(new ByteArrayResource("contenido".getBytes()));

        mvc.perform(get("/historia/archivos/" + id))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    void descargarArchivo_conNombreOriginalNulo_usaNombreFallback() throws Exception {
        UUID id = UUID.randomUUID();
        ArchivoClinicoDTO dto = new ArchivoClinicoDTO();
        dto.setNombreOriginal(null);
        dto.setContentType("application/pdf");
        when(facade.getArchivoClinico(id)).thenReturn(dto);
        when(facade.getMineResource(id)).thenReturn(new ByteArrayResource("contenido".getBytes()));

        mvc.perform(get("/historia/archivos/" + id))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Content-Disposition", org.hamcrest.Matchers.containsString("archivo")));
    }

    @Test
    void descargarArchivo_conErrorDelFacade_propagaArchivoClinicoException() {
        UUID id = UUID.randomUUID();
        when(facade.getArchivoClinico(id)).thenThrow(new RuntimeException("fallo"));

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class,
                () -> mvc.perform(get("/historia/archivos/" + id)));
        assertInstanceOf(ArchivoClinicoException.class, causaReal(ex));
    }

    @Test
    void eliminarArchivo_conErrorInesperadoDelFacade_propagaArchivoClinicoException() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new RuntimeException("fallo")).when(facade).borrarArchivoClinico(id);

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class,
                () -> mvc.perform(delete("/historia/archivos/" + id)));
        assertInstanceOf(ArchivoClinicoException.class, causaReal(ex));
    }

    @Test
    void eliminarArchivo_conIOExceptionDelFacade_propagaArchivoClinicoException() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new java.io.IOException("disco lleno")).when(facade).borrarArchivoClinico(id);

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class,
                () -> mvc.perform(delete("/historia/archivos/" + id)));
        assertInstanceOf(ArchivoClinicoException.class, causaReal(ex));
    }

    @Test
    void subirArchivo_conIOExceptionDelFacade_propagaArchivoClinicoException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "informe.pdf", "application/pdf", "contenido".getBytes());
        when(facade.upload(any())).thenThrow(new java.io.IOException("disco lleno"));

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class,
                () -> mvc.perform(multipart("/historia/archivos").file(file)));
        assertInstanceOf(ArchivoClinicoException.class, causaReal(ex));
    }

    // ---- ramas de error (catch genérico -> HistorialClinicoException) ----

    @Test
    void getMiHistoria_conErrorDelFacade_propagaHistorialClinicoException() {
        when(facade.obtenerMiHistoria()).thenThrow(new RuntimeException("fallo"));

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class,
                () -> mvc.perform(get("/historia")));
        assertInstanceOf(HistorialClinicoException.class, causaReal(ex));
    }

    @Test
    void crearAntecedente_conErrorDelFacade_propagaHistorialClinicoException() {
        when(facade.crearAntecedente(any())).thenThrow(new RuntimeException("fallo"));

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class, () -> mvc.perform(
                post("/historia/antecedentes").contentType(MediaType.APPLICATION_JSON).content("{}")));
        assertInstanceOf(HistorialClinicoException.class, causaReal(ex));
    }

    @Test
    void editarAntecedente_conErrorDelFacade_propagaHistorialClinicoException() {
        UUID id = UUID.randomUUID();
        when(facade.editarAntecedente(eq(id), any())).thenThrow(new RuntimeException("fallo"));

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class, () -> mvc.perform(
                put("/historia/antecedentes/" + id).contentType(MediaType.APPLICATION_JSON).content("{}")));
        assertInstanceOf(HistorialClinicoException.class, causaReal(ex));
    }

    @Test
    void borrarAntecedente_conErrorDelFacade_propagaHistorialClinicoException() {
        UUID id = UUID.randomUUID();
        when(facade.borrarAntecedente(id)).thenThrow(new RuntimeException("fallo"));

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class,
                () -> mvc.perform(delete("/historia/antecedentes/" + id)));
        assertInstanceOf(HistorialClinicoException.class, causaReal(ex));
    }

    @Test
    void crearAlergia_conErrorDelFacade_propagaHistorialClinicoException() {
        when(facade.crearAlergia(any())).thenThrow(new RuntimeException("fallo"));

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class, () -> mvc.perform(
                post("/historia/alergias").contentType(MediaType.APPLICATION_JSON).content("{}")));
        assertInstanceOf(HistorialClinicoException.class, causaReal(ex));
    }

    @Test
    void borrarAlergia_conErrorDelFacade_propagaHistorialClinicoException() {
        UUID id = UUID.randomUUID();
        when(facade.borrarAlergia(id)).thenThrow(new RuntimeException("fallo"));

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class,
                () -> mvc.perform(delete("/historia/alergias/" + id)));
        assertInstanceOf(HistorialClinicoException.class, causaReal(ex));
    }

    @Test
    void actualizarSignosVitales_conErrorDelFacade_propagaHistorialClinicoException() {
        when(facade.actualizarSignosVitales(any())).thenThrow(new RuntimeException("fallo"));

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class, () -> mvc.perform(
                put("/historia/signos-vitales").contentType(MediaType.APPLICATION_JSON).content(CUERPO_MEDICIONES)));
        assertInstanceOf(HistorialClinicoException.class, causaReal(ex));
    }

    @Test
    void actualizarAnalisisOrina_conErrorDelFacade_propagaHistorialClinicoException() {
        when(facade.actualizarAnalisisOrina(any())).thenThrow(new RuntimeException("fallo"));

        jakarta.servlet.ServletException ex = assertThrows(jakarta.servlet.ServletException.class, () -> mvc.perform(
                put("/historia/analisis-orina").contentType(MediaType.APPLICATION_JSON).content(CUERPO_MEDICIONES)));
        assertInstanceOf(HistorialClinicoException.class, causaReal(ex));
    }

    @Test
    void actualizarAnalisisSangre_conValoresQueContienenMasOPorcentaje_noLosCorrompe() throws Exception {
        // Antes, el controlador aplicaba una heurística de URL-decode que convertía "B+" en "B "
        // y "98%" en un error de decodificación. Ahora el cuerpo es JSON tipado y llega intacto.
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        java.util.List<DatoClinicoEntradaDTO> capturado = new java.util.ArrayList<>();
        when(facade.actualizarAnalisisSangre(any())).thenAnswer(inv -> {
            capturado.addAll(inv.getArgument(0));
            return dto;
        });

        mvc.perform(put("/historia/analisis-sangre")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"label\":\"Grupo sanguíneo\",\"value\":\"B+\",\"unit\":\"98%\"}]"))
                .andExpect(status().isOk());

        assertEquals("B+", capturado.get(0).getValue());
        assertEquals("98%", capturado.get(0).getUnit());
    }
}
