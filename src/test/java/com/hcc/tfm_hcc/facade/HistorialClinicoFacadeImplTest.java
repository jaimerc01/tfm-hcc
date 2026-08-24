package com.hcc.tfm_hcc.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import com.hcc.tfm_hcc.dto.AlergiaDTO;
import com.hcc.tfm_hcc.dto.AntecedenteClinicoDTO;
import com.hcc.tfm_hcc.dto.ArchivoClinicoDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.exception.ArchivoClinicoException;
import com.hcc.tfm_hcc.exception.DatosClinicosValidationException;
import com.hcc.tfm_hcc.exception.HistorialClinicoException;
import com.hcc.tfm_hcc.facade.impl.HistorialClinicoFacadeImpl;
import com.hcc.tfm_hcc.mapper.ArchivoClinicoMapper;
import com.hcc.tfm_hcc.model.ArchivoClinico;
import com.hcc.tfm_hcc.service.ArchivoClinicoService;
import com.hcc.tfm_hcc.service.HistorialClinicoService;

class HistorialClinicoFacadeImplTest {

    private ArchivoClinicoService archivoClinicoService;
    private ArchivoClinicoMapper archivoClinicoMapper;
    private HistorialClinicoService historiaClinicaService;
    private HistorialClinicoFacadeImpl facade;

    @BeforeEach
    void setUp() {
        archivoClinicoService = mock(ArchivoClinicoService.class);
        archivoClinicoMapper = mock(ArchivoClinicoMapper.class);
        historiaClinicaService = mock(HistorialClinicoService.class);
        facade = new HistorialClinicoFacadeImpl(archivoClinicoService, archivoClinicoMapper, historiaClinicaService);
    }

    // ---- archivos clínicos ----

    @Test
    void listarArchivos_devuelveLosArchivosConvertidos() {
        ArchivoClinico archivo = new ArchivoClinico();
        ArchivoClinicoDTO dto = new ArchivoClinicoDTO();
        when(archivoClinicoService.listMine()).thenReturn(List.of(archivo));
        when(archivoClinicoMapper.toDto(archivo)).thenReturn(dto);

        assertEquals(List.of(dto), facade.listarArchivos());
    }

    @Test
    void listarArchivos_conErrorInesperado_lanzaArchivoClinicoException() {
        when(archivoClinicoService.listMine()).thenThrow(new RuntimeException("fallo"));

        assertThrows(ArchivoClinicoException.class, () -> facade.listarArchivos());
    }

    @Test
    void upload_conArchivoValido_devuelveElDto() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "informe.pdf", "application/pdf", "contenido".getBytes());
        ArchivoClinico guardado = new ArchivoClinico();
        ArchivoClinicoDTO dto = new ArchivoClinicoDTO();
        when(archivoClinicoService.uploadMine(file)).thenReturn(guardado);
        when(archivoClinicoMapper.toDto(guardado)).thenReturn(dto);

        assertEquals(dto, facade.upload(file));
    }

    @Test
    void upload_conArchivoNulo_lanzaDatosClinicosValidationException() {
        assertThrows(DatosClinicosValidationException.class, () -> facade.upload(null));
    }

    @Test
    void upload_conArchivoVacio_lanzaDatosClinicosValidationException() {
        MockMultipartFile file = new MockMultipartFile("file", "vacio.pdf", "application/pdf", new byte[0]);

        assertThrows(DatosClinicosValidationException.class, () -> facade.upload(file));
    }

    @Test
    void upload_conNombreDeArchivoVacio_lanzaDatosClinicosValidationException() {
        MockMultipartFile file = new MockMultipartFile("file", "", "application/pdf", "contenido".getBytes());

        assertThrows(DatosClinicosValidationException.class, () -> facade.upload(file));
    }

    @Test
    void upload_conArchivoDemasiadoGrande_lanzaDatosClinicosValidationException() {
        MockMultipartFile file = new MockMultipartFile("file", "grande.pdf", "application/pdf", new byte[11 * 1024 * 1024]);

        assertThrows(DatosClinicosValidationException.class, () -> facade.upload(file));
    }

    @Test
    void upload_conErrorDeEntradaSalida_propagaLaExcepcion() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "informe.pdf", "application/pdf", "contenido".getBytes());
        when(archivoClinicoService.uploadMine(file)).thenThrow(new IOException("disco lleno"));

        assertThrows(IOException.class, () -> facade.upload(file));
    }

    @Test
    void getArchivoClinico_conIdExistente_devuelveElDto() {
        UUID id = UUID.randomUUID();
        ArchivoClinico archivo = new ArchivoClinico();
        archivo.setId(id);
        ArchivoClinicoDTO dto = new ArchivoClinicoDTO();
        when(archivoClinicoService.listMine()).thenReturn(List.of(archivo));
        when(archivoClinicoMapper.toDto(archivo)).thenReturn(dto);

        assertEquals(dto, facade.getArchivoClinico(id));
    }

    @Test
    void getArchivoClinico_conIdInexistente_lanzaArchivoClinicoException() {
        when(archivoClinicoService.listMine()).thenReturn(List.of());

        assertThrows(ArchivoClinicoException.class, () -> facade.getArchivoClinico(UUID.randomUUID()));
    }

    @Test
    void getArchivoClinico_conIdNulo_lanzaDatosClinicosValidationException() {
        assertThrows(DatosClinicosValidationException.class, () -> facade.getArchivoClinico(null));
    }

    @Test
    void getMineResource_conIdValido_devuelveElRecurso() {
        UUID id = UUID.randomUUID();
        Resource resource = mock(Resource.class);
        when(archivoClinicoService.getMineResource(id)).thenReturn(resource);

        assertEquals(resource, facade.getMineResource(id));
    }

    @Test
    void getMineResource_conIdNulo_lanzaDatosClinicosValidationException() {
        assertThrows(DatosClinicosValidationException.class, () -> facade.getMineResource(null));
    }

    @Test
    void borrarArchivoClinico_conIdValido_delegaEnElServicio() throws IOException {
        UUID id = UUID.randomUUID();

        facade.borrarArchivoClinico(id);

        verify(archivoClinicoService, times(1)).borrarArchivo(id);
    }

    @Test
    void borrarArchivoClinico_conIdNulo_lanzaDatosClinicosValidationExceptionYNoLlamaAlServicio() throws IOException {
        assertThrows(DatosClinicosValidationException.class, () -> facade.borrarArchivoClinico(null));
        verify(archivoClinicoService, never()).borrarArchivo(any());
    }

    @Test
    void borrarArchivoClinico_conErrorDeEntradaSalida_propagaLaExcepcion() throws IOException {
        UUID id = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new IOException("fallo")).when(archivoClinicoService).borrarArchivo(id);

        assertThrows(IOException.class, () -> facade.borrarArchivoClinico(id));
    }

    // ---- historial clínico ----

    @Test
    void obtenerMiHistoria_devuelveElHistorialDelServicio() {
        HistorialClinicoDTO historial = new HistorialClinicoDTO();
        when(historiaClinicaService.obtenerHistoriaUsuarioActual()).thenReturn(historial);

        assertEquals(historial, facade.obtenerMiHistoria());
    }

    @Test
    void obtenerMiHistoria_conErrorInesperado_lanzaHistorialClinicoException() {
        when(historiaClinicaService.obtenerHistoriaUsuarioActual()).thenThrow(new RuntimeException("fallo"));

        assertThrows(HistorialClinicoException.class, () -> facade.obtenerMiHistoria());
    }

    private AntecedenteClinicoDTO antecedenteValido() {
        AntecedenteClinicoDTO dto = new AntecedenteClinicoDTO();
        dto.setCategoria("FAMILIAR");
        dto.setDescripcion("Diabetes tipo 2");
        return dto;
    }

    @Test
    void crearAntecedente_conDatosValidos_devuelveElHistorialActualizado() {
        AntecedenteClinicoDTO entrada = antecedenteValido();
        HistorialClinicoDTO resultado = new HistorialClinicoDTO();
        when(historiaClinicaService.crearAntecedente(entrada)).thenReturn(resultado);

        assertEquals(resultado, facade.crearAntecedente(entrada));
    }

    @Test
    void crearAntecedente_conDtoNulo_lanzaDatosClinicosValidationException() {
        assertThrows(DatosClinicosValidationException.class, () -> facade.crearAntecedente(null));
    }

    @Test
    void crearAntecedente_conCategoriaVacia_lanzaDatosClinicosValidationException() {
        AntecedenteClinicoDTO dto = antecedenteValido();
        dto.setCategoria("  ");

        assertThrows(DatosClinicosValidationException.class, () -> facade.crearAntecedente(dto));
    }

    @Test
    void crearAntecedente_conDescripcionDemasiadoLarga_lanzaDatosClinicosValidationException() {
        AntecedenteClinicoDTO dto = antecedenteValido();
        dto.setDescripcion("a".repeat(5001));

        assertThrows(DatosClinicosValidationException.class, () -> facade.crearAntecedente(dto));
    }

    @Test
    void editarAntecedente_conDatosValidos_devuelveElHistorialActualizado() {
        UUID id = UUID.randomUUID();
        AntecedenteClinicoDTO entrada = antecedenteValido();
        HistorialClinicoDTO resultado = new HistorialClinicoDTO();
        when(historiaClinicaService.editarAntecedente(id, entrada)).thenReturn(resultado);

        assertEquals(resultado, facade.editarAntecedente(id, entrada));
    }

    @Test
    void editarAntecedente_conIdNulo_lanzaDatosClinicosValidationException() {
        assertThrows(DatosClinicosValidationException.class, () -> facade.editarAntecedente(null, antecedenteValido()));
    }

    @Test
    void borrarAntecedente_conIdValido_devuelveElHistorialActualizado() {
        UUID id = UUID.randomUUID();
        HistorialClinicoDTO resultado = new HistorialClinicoDTO();
        when(historiaClinicaService.borrarAntecedente(id)).thenReturn(resultado);

        assertEquals(resultado, facade.borrarAntecedente(id));
    }

    @Test
    void borrarAntecedente_conIdNulo_lanzaDatosClinicosValidationException() {
        assertThrows(DatosClinicosValidationException.class, () -> facade.borrarAntecedente(null));
    }

    private AlergiaDTO alergiaValida() {
        AlergiaDTO dto = new AlergiaDTO();
        dto.setDescripcion("Alergia a la penicilina");
        return dto;
    }

    @Test
    void crearAlergia_conDatosValidos_devuelveElHistorialActualizado() {
        AlergiaDTO entrada = alergiaValida();
        HistorialClinicoDTO resultado = new HistorialClinicoDTO();
        when(historiaClinicaService.crearAlergia(entrada)).thenReturn(resultado);

        assertEquals(resultado, facade.crearAlergia(entrada));
    }

    @Test
    void crearAlergia_conDtoNulo_lanzaDatosClinicosValidationException() {
        assertThrows(DatosClinicosValidationException.class, () -> facade.crearAlergia(null));
    }

    @Test
    void crearAlergia_conDescripcionVacia_lanzaDatosClinicosValidationException() {
        AlergiaDTO dto = alergiaValida();
        dto.setDescripcion("  ");

        assertThrows(DatosClinicosValidationException.class, () -> facade.crearAlergia(dto));
    }

    @Test
    void borrarAlergia_conIdValido_devuelveElHistorialActualizado() {
        UUID id = UUID.randomUUID();
        HistorialClinicoDTO resultado = new HistorialClinicoDTO();
        when(historiaClinicaService.borrarAlergia(id)).thenReturn(resultado);

        assertEquals(resultado, facade.borrarAlergia(id));
    }

    @Test
    void borrarAlergia_conIdNulo_lanzaDatosClinicosValidationException() {
        assertThrows(DatosClinicosValidationException.class, () -> facade.borrarAlergia(null));
    }

    // ---- análisis y signos, en formato JSON ----

    @Test
    void actualizarAnalisisSangre_conJsonValido_devuelveElHistorialActualizado() {
        HistorialClinicoDTO resultado = new HistorialClinicoDTO();
        when(historiaClinicaService.actualizarAnalisisSangre("{\"glucosa\":90}")).thenReturn(resultado);

        assertEquals(resultado, facade.actualizarAnalisisSangre("{\"glucosa\":90}"));
    }

    @Test
    void actualizarAnalisisSangre_conJsonVacio_lanzaDatosClinicosValidationException() {
        assertThrows(DatosClinicosValidationException.class, () -> facade.actualizarAnalisisSangre("  "));
    }

    @Test
    void actualizarAnalisisSangre_conFormatoNoJson_lanzaDatosClinicosValidationException() {
        assertThrows(DatosClinicosValidationException.class, () -> facade.actualizarAnalisisSangre("no es json"));
    }

    @Test
    void anadirAnalisisSangre_conJsonValido_devuelveElHistorialActualizado() {
        HistorialClinicoDTO resultado = new HistorialClinicoDTO();
        when(historiaClinicaService.añadirAnalisisSangre("{\"glucosa\":90}")).thenReturn(resultado);

        assertEquals(resultado, facade.anadirAnalisisSangre("{\"glucosa\":90}"));
    }

    @Test
    void anadirAnalisisSangre_conJsonInvalido_lanzaDatosClinicosValidationException() {
        assertThrows(DatosClinicosValidationException.class, () -> facade.anadirAnalisisSangre(null));
    }

    @Test
    void actualizarSignosVitales_conJsonValido_devuelveElHistorialActualizado() {
        HistorialClinicoDTO resultado = new HistorialClinicoDTO();
        when(historiaClinicaService.actualizarSignosVitales("{\"pulso\":70}")).thenReturn(resultado);

        assertEquals(resultado, facade.actualizarSignosVitales("{\"pulso\":70}"));
    }

    @Test
    void actualizarSignosVitales_conJsonInvalido_lanzaDatosClinicosValidationException() {
        assertThrows(DatosClinicosValidationException.class, () -> facade.actualizarSignosVitales("x"));
    }

    @Test
    void anadirSignosVitales_conJsonValido_devuelveElHistorialActualizado() {
        HistorialClinicoDTO resultado = new HistorialClinicoDTO();
        when(historiaClinicaService.añadirSignosVitales("{\"pulso\":70}")).thenReturn(resultado);

        assertEquals(resultado, facade.anadirSignosVitales("{\"pulso\":70}"));
    }

    @Test
    void anadirSignosVitales_conJsonInvalido_lanzaDatosClinicosValidationException() {
        assertThrows(DatosClinicosValidationException.class, () -> facade.anadirSignosVitales(""));
    }

    @Test
    void actualizarAnalisisOrina_conJsonValido_devuelveElHistorialActualizado() {
        HistorialClinicoDTO resultado = new HistorialClinicoDTO();
        when(historiaClinicaService.actualizarAnalisisOrina("{\"ph\":6}")).thenReturn(resultado);

        assertEquals(resultado, facade.actualizarAnalisisOrina("{\"ph\":6}"));
    }

    @Test
    void actualizarAnalisisOrina_conJsonInvalido_lanzaDatosClinicosValidationException() {
        assertThrows(DatosClinicosValidationException.class, () -> facade.actualizarAnalisisOrina("x"));
    }

    @Test
    void anadirAnalisisOrina_conJsonValido_devuelveElHistorialActualizado() {
        HistorialClinicoDTO resultado = new HistorialClinicoDTO();
        when(historiaClinicaService.añadirAnalisisOrina("[1,2]")).thenReturn(resultado);

        assertEquals(resultado, facade.anadirAnalisisOrina("[1,2]"));
    }

    @Test
    void anadirAnalisisOrina_conJsonInvalido_lanzaDatosClinicosValidationException() {
        assertThrows(DatosClinicosValidationException.class, () -> facade.anadirAnalisisOrina(null));
    }

    @Test
    void actualizarAnalisisSangre_conErrorInesperado_lanzaHistorialClinicoException() {
        when(historiaClinicaService.actualizarAnalisisSangre("{\"glucosa\":90}")).thenThrow(new RuntimeException("fallo"));

        assertThrows(HistorialClinicoException.class, () -> facade.actualizarAnalisisSangre("{\"glucosa\":90}"));
    }

    // ---- datos clínicos ----

    @Test
    void borrarDatoClinico_conIdValido_delegaEnElServicio() {
        UUID id = UUID.randomUUID();

        facade.borrarDatoClinico(id);

        verify(historiaClinicaService, times(1)).borrarDatoClinico(id);
    }

    @Test
    void borrarDatoClinico_conIdNulo_lanzaDatosClinicosValidationException() {
        assertThrows(DatosClinicosValidationException.class, () -> facade.borrarDatoClinico(null));
        verify(historiaClinicaService, never()).borrarDatoClinico(any());
    }

    @Test
    void borrarDatoClinico_conErrorInesperado_lanzaHistorialClinicoException() {
        UUID id = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new RuntimeException("fallo")).when(historiaClinicaService).borrarDatoClinico(id);

        assertThrows(HistorialClinicoException.class, () -> facade.borrarDatoClinico(id));
    }
}
