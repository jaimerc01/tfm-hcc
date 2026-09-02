package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.model.ArchivoClinico;
import com.hcc.tfm_hcc.model.AuditoriaCambio;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.ArchivoClinicoRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.impl.ArchivoClinicoServiceImpl;

class ArchivoClinicoServiceImplTest {

    @Mock
    private ArchivoClinicoRepository archivoClinicoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuditoriaCambioService auditoriaCambioService;

    /**
     * Cifrado "de mentira" para los tests: en vez de AES/GCM real, antepone un
     * prefijo fijo al escribir y lo retira al leer. Evita depender de una clave
     * AES real solo para probar la orquestación de ArchivoClinicoServiceImpl,
     * que es lo que estos tests verifican (no el algoritmo de cifrado en sí,
     * que ya se cubre en las pruebas de ArchivoCifradoServiceImpl).
     */
    private static final String PREFIJO_CIFRADO_FALSO = "CIFRADO:";

    /**
     * Cifrado "de mentira" para el nombre de archivo: antepone un prefijo fijo,
     * igual que {@link #PREFIJO_CIFRADO_FALSO} pero para FieldEncryptionService.
     */
    private static final String PREFIJO_NOMBRE_FALSO = "NOMBRE_CIFRADO:";

    @Mock
    private ArchivoCifradoService archivoCifradoService;

    @Mock
    private FieldEncryptionService fieldEncryptionService;

    @Mock
    private com.hcc.tfm_hcc.service.RelacionMedicoPacienteService relacionMedicoPacienteService;

    @InjectMocks
    private ArchivoClinicoServiceImpl service;

    private AutoCloseable mocks;
    private Usuario usuario;

    @SuppressWarnings("null")
    @BeforeEach
    void setUp() throws IOException {
        mocks = MockitoAnnotations.openMocks(this);

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());

        ReflectionTestUtils.setField(service, "maxSizeBytes", 1024L * 1024L);
        ReflectionTestUtils.setField(service, "allowedTypes", "");
        ReflectionTestUtils.setField(service, "allowedExtensions", "");

        doAnswer(invocation -> {
            InputStream entrada = invocation.getArgument(0);
            OutputStream salida = invocation.getArgument(1);
            salida.write(PREFIJO_CIFRADO_FALSO.getBytes());
            entrada.transferTo(salida);
            return null;
        }).when(archivoCifradoService).cifrar(any(InputStream.class), any(OutputStream.class));

        when(archivoCifradoService.descifrar(any(InputStream.class))).thenAnswer(invocation -> {
            InputStream entrada = invocation.getArgument(0);
            byte[] contenido = entrada.readAllBytes();
            String texto = new String(contenido).substring(PREFIJO_CIFRADO_FALSO.length());
            return new ByteArrayInputStream(texto.getBytes());
        });

        when(fieldEncryptionService.cifrar(anyString()))
                .thenAnswer(invocation -> PREFIJO_NOMBRE_FALSO + invocation.getArgument(0, String.class));
        when(fieldEncryptionService.descifrar(anyString())).thenAnswer(invocation -> {
            String valor = invocation.getArgument(0, String.class);
            return valor.startsWith(PREFIJO_NOMBRE_FALSO) ? valor.substring(PREFIJO_NOMBRE_FALSO.length()) : valor;
        });

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(usuario, null));
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        mocks.close();
    }

    @Test
    void listMine_returnsCurrentUserFilesWithDecryptedName() {
        ArchivoClinico archivo = new ArchivoClinico();
        archivo.setId(UUID.randomUUID());
        archivo.setNombreOriginal(PREFIJO_NOMBRE_FALSO + "informe.pdf");

        when(archivoClinicoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId()))
                .thenReturn(List.of(archivo));

        List<ArchivoClinico> result = service.listMine();

        assertEquals(1, result.size());
        assertEquals("informe.pdf", result.get(0).getNombreOriginal());
        verify(archivoClinicoRepository).findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId());
    }

    @Test
    void uploadMine_throwsWhenFileIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.uploadMine(null));

        assertEquals(ErrorMessages.ERROR_NO_ARCHIVO, ex.getMessage());
    }

    @Test
    void uploadMine_throwsWhenFileIsEmpty() {
        MultipartFile file = new MockMultipartFile("file", "vacio.pdf", "application/pdf", new byte[0]);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.uploadMine(file));

        assertEquals(ErrorMessages.ERROR_ARCHIVO_VACIO, ex.getMessage());
    }

    @SuppressWarnings("null")
    @Test
    void uploadMine_throwsWhenFileExceedsMaxSize() {
        ReflectionTestUtils.setField(service, "maxSizeBytes", 4L);
        MultipartFile file = new MockMultipartFile("file", "grande.pdf", "application/pdf", new byte[] {1, 2, 3, 4, 5});

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.uploadMine(file));

        assertEquals(ErrorMessages.ERROR_TAMAÑO_EXCEDIDO, ex.getMessage());
    }

    @SuppressWarnings("null")
    @Test
    void uploadMine_throwsWhenFilenameIsUnsafe() {
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        MultipartFile file = new MockMultipartFile("file", "../secreto.pdf", "application/pdf", "contenido".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.uploadMine(file));

        assertEquals(ErrorMessages.ERROR_NOMBRE_INVALIDO, ex.getMessage());
    }

    @SuppressWarnings("null")
    @Test
    void uploadMine_throwsWhenContentTypeIsNotAllowed() {
        ReflectionTestUtils.setField(service, "allowedTypes", "application/pdf");
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));

        MultipartFile file = new MockMultipartFile("file", "nota.pdf", "text/plain", "contenido".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.uploadMine(file));

        assertEquals(ErrorMessages.ERROR_TIPO_NO_PERMITIDO, ex.getMessage());
    }

    @SuppressWarnings("null")
    @Test
    void uploadMine_throwsWhenExtensionIsNotAllowed() {
        ReflectionTestUtils.setField(service, "allowedExtensions", "pdf,png,jpg");
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));

        MultipartFile file = new MockMultipartFile("file", "malware.exe", "application/pdf", "contenido".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.uploadMine(file));

        assertEquals(ErrorMessages.ERROR_EXTENSION_NO_PERMITIDA, ex.getMessage());
    }

    @SuppressWarnings("null")
    @Test
    void uploadMine_conExtensionEnMayusculasPermitida_noFalla() throws IOException {
        ReflectionTestUtils.setField(service, "allowedExtensions", "pdf,png");
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(archivoClinicoRepository.save(org.mockito.ArgumentMatchers.any(ArchivoClinico.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        MultipartFile file = new MockMultipartFile("file", "Informe.PDF", "application/pdf", "contenido".getBytes());

        assertEquals("Informe.PDF", service.uploadMine(file).getNombreOriginal());
    }

    @SuppressWarnings("null")
    @Test
    void uploadMine_savesEncryptedMetadataAndContent() throws IOException {
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        // Devuelve una copia (como haría una lectura real tras persistir) para que la
        // mutación posterior del servicio (descifrar el nombre en el objeto devuelto)
        // no afecte al objeto capturado por ArgumentCaptor, que debe reflejar tal cual
        // lo que se envió a MongoDB (con el nombre todavía cifrado).
        when(archivoClinicoRepository.save(any(ArchivoClinico.class))).thenAnswer(invocation -> {
            ArchivoClinico original = invocation.getArgument(0);
            ArchivoClinico copia = new ArchivoClinico();
            copia.setId(original.getId());
            copia.setUsuarioId(original.getUsuarioId());
            copia.setNombreOriginal(original.getNombreOriginal());
            copia.setContentType(original.getContentType());
            copia.setSizeBytes(original.getSizeBytes());
            copia.setContenido(original.getContenido());
            copia.setFechaCreacion(original.getFechaCreacion());
            return copia;
        });

        MultipartFile file = new MockMultipartFile("file", "informe.pdf", "application/pdf", "contenido".getBytes());

        ArchivoClinico saved = service.uploadMine(file);

        assertNotNull(saved.getId());
        assertEquals("informe.pdf", saved.getNombreOriginal());
        assertEquals("application/pdf", saved.getContentType());
        assertEquals(file.getSize(), saved.getSizeBytes());
        assertArrayEquals((PREFIJO_CIFRADO_FALSO + "contenido").getBytes(), saved.getContenido());

        ArgumentCaptor<ArchivoClinico> captor = ArgumentCaptor.forClass(ArchivoClinico.class);
        verify(archivoClinicoRepository).save(captor.capture());
        assertEquals(usuario.getId(), captor.getValue().getUsuarioId());
        assertEquals(PREFIJO_NOMBRE_FALSO + "informe.pdf", captor.getValue().getNombreOriginal());

        verify(auditoriaCambioService).registrarCambio(
                eq(usuario.getId().toString()),
                eq(usuario.getId().toString()),
                isNull(),
                eq("SUBIDA_ARCHIVO_CLINICO"),
                eq("archivo_clinico"),
                eq(saved.getId().toString()),
                isNull(),
                isNull(),
                eq(AuditoriaCambio.TipoOperacion.CREATE),
                any());
    }

    @Test
    void getMineResource_returnsResourceWhenOwnedAndReadable() throws IOException {
        ArchivoClinico archivo = new ArchivoClinico();
        archivo.setId(UUID.randomUUID());
        archivo.setUsuarioId(usuario.getId());
        archivo.setNombreOriginal(PREFIJO_NOMBRE_FALSO + "reporte.pdf");
        archivo.setContenido((PREFIJO_CIFRADO_FALSO + "contenido").getBytes());

        when(archivoClinicoRepository.findByIdAndUsuarioId(archivo.getId(), usuario.getId()))
                .thenReturn(Optional.of(archivo));

        Resource resource = service.getMineResource(archivo.getId());

        assertTrue(resource.isReadable());
        assertArrayEquals("contenido".getBytes(), resource.getInputStream().readAllBytes());
    }

    @Test
    void getMineResource_throwsWhenFileIsNotOwned() {
        UUID archivoId = UUID.randomUUID();
        when(archivoClinicoRepository.findByIdAndUsuarioId(archivoId, usuario.getId()))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getMineResource(archivoId));

        assertEquals(ErrorMessages.ERROR_ARCHIVO_NO_EXISTE, ex.getMessage());
    }

    @Test
    void getMineResource_throwsWhenContentIsMissing() {
        ArchivoClinico archivo = new ArchivoClinico();
        archivo.setId(UUID.randomUUID());
        archivo.setUsuarioId(usuario.getId());
        archivo.setNombreOriginal(PREFIJO_NOMBRE_FALSO + "sin-contenido.pdf");

        when(archivoClinicoRepository.findByIdAndUsuarioId(archivo.getId(), usuario.getId()))
                .thenReturn(Optional.of(archivo));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.getMineResource(archivo.getId()));

        assertEquals(ErrorMessages.ERROR_ARCHIVO_NO_ACCESIBLE, ex.getMessage());
    }

    @Test
    void deleteMine_removesDocumentFromRepository() {
        ArchivoClinico archivo = new ArchivoClinico();
        UUID archivoId = UUID.randomUUID();
        archivo.setId(archivoId);
        archivo.setUsuarioId(usuario.getId());
        archivo.setNombreOriginal(PREFIJO_NOMBRE_FALSO + "a-eliminar.pdf");

        when(archivoClinicoRepository.findByIdAndUsuarioId(archivoId, usuario.getId()))
                .thenReturn(Optional.of(archivo));

        assertDoesNotThrow(() -> service.borrarArchivo(archivoId));

        verify(archivoClinicoRepository).delete(archivo);
        verify(auditoriaCambioService).registrarCambio(
                eq(usuario.getId().toString()),
                eq(usuario.getId().toString()),
                isNull(),
                eq("BORRADO_ARCHIVO_CLINICO"),
                eq("archivo_clinico"),
                eq(archivoId.toString()),
                isNull(),
                isNull(),
                eq(AuditoriaCambio.TipoOperacion.DELETE),
                any());
    }

    @Test
    void deleteMine_throwsWhenFileIsNotOwned() {
        UUID archivoId = UUID.randomUUID();
        when(archivoClinicoRepository.findByIdAndUsuarioId(archivoId, usuario.getId()))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.borrarArchivo(archivoId));

        assertEquals(ErrorMessages.ERROR_ARCHIVO_NO_EXISTE, ex.getMessage());
    }

    // ---- Documentos de un paciente asignado (acceso del médico) ----

    private Usuario pacienteAsignado() {
        usuario.setNif("11111111A");
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        Usuario paciente = new Usuario();
        paciente.setId(UUID.randomUUID());
        paciente.setNif("22222222B");
        when(relacionMedicoPacienteService.verificarAccesoMedicoActivo("11111111A", "22222222B"))
                .thenReturn(paciente);
        return paciente;
    }

    @Test
    void listForPaciente_conAccesoActivo_devuelveLosArchivosDelPaciente() {
        Usuario paciente = pacienteAsignado();
        ArchivoClinico archivo = new ArchivoClinico();
        archivo.setId(UUID.randomUUID());
        archivo.setNombreOriginal(PREFIJO_NOMBRE_FALSO + "analitica.pdf");
        when(archivoClinicoRepository.findByUsuarioIdOrderByFechaCreacionDesc(paciente.getId()))
                .thenReturn(List.of(archivo));

        List<ArchivoClinico> result = service.listForPaciente("22222222B");

        assertEquals(1, result.size());
        assertEquals("analitica.pdf", result.get(0).getNombreOriginal());
    }

    @Test
    void listForPaciente_sinAccesoActivo_propagaLaExcepcion() {
        usuario.setNif("11111111A");
        when(relacionMedicoPacienteService.verificarAccesoMedicoActivo("11111111A", "22222222B"))
                .thenThrow(new com.hcc.tfm_hcc.exception.UsuarioSinPermisoException("acceso denegado"));

        assertThrows(com.hcc.tfm_hcc.exception.UsuarioSinPermisoException.class,
                () -> service.listForPaciente("22222222B"));
    }

    @Test
    void uploadForPaciente_conAccesoActivo_guardaElArchivoConElPacienteComoPropietarioYAudita() throws IOException {
        Usuario paciente = pacienteAsignado();
        when(archivoClinicoRepository.save(any(ArchivoClinico.class))).thenAnswer(inv -> {
            ArchivoClinico a = inv.getArgument(0);
            if (a.getId() == null) a.setId(UUID.randomUUID());
            return a;
        });
        MultipartFile file = new MockMultipartFile("file", "informe.pdf", "application/pdf", "contenido".getBytes());

        ArchivoClinico guardado = service.uploadForPaciente("22222222B", file);

        assertEquals(paciente.getId(), guardado.getUsuarioId());
        verify(auditoriaCambioService).registrarCambio(
                eq(usuario.getId().toString()),
                eq(paciente.getId().toString()),
                eq(usuario.getId().toString()),
                eq("SUBIDA_ARCHIVO_CLINICO"),
                eq("archivo_clinico"),
                anyString(),
                isNull(),
                isNull(),
                eq(AuditoriaCambio.TipoOperacion.CREATE),
                any());
    }

    @Test
    void getPacienteResource_conArchivoAjenoAlPaciente_lanzaIllegalArgumentException() {
        Usuario paciente = pacienteAsignado();
        UUID archivoId = UUID.randomUUID();
        when(archivoClinicoRepository.findByIdAndUsuarioId(archivoId, paciente.getId()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.getPacienteResource("22222222B", archivoId));
    }
}
