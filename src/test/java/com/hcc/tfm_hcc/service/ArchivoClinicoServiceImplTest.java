package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.ArchivoClinicoRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.impl.ArchivoClinicoServiceImpl;

class ArchivoClinicoServiceImplTest {

    @Mock
    private ArchivoClinicoRepository archivoClinicoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ArchivoClinicoServiceImpl service;

    @TempDir
    Path tempDir;

    private AutoCloseable mocks;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());

        ReflectionTestUtils.setField(service, "baseDir", tempDir.toString());
        ReflectionTestUtils.setField(service, "maxSizeBytes", 1024L * 1024L);
        ReflectionTestUtils.setField(service, "allowedTypes", "");

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(usuario, null));
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        mocks.close();
    }

    @Test
    void listMine_returnsCurrentUserFiles() {
        ArchivoClinico archivo = new ArchivoClinico();
        archivo.setId(UUID.randomUUID());

        when(archivoClinicoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId()))
                .thenReturn(List.of(archivo));

        List<ArchivoClinico> result = service.listMine();

        assertEquals(1, result.size());
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

    @Test
    void uploadMine_throwsWhenFileExceedsMaxSize() {
        ReflectionTestUtils.setField(service, "maxSizeBytes", 4L);
        MultipartFile file = new MockMultipartFile("file", "grande.pdf", "application/pdf", new byte[] {1, 2, 3, 4, 5});

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.uploadMine(file));

        assertEquals(ErrorMessages.ERROR_TAMAÑO_EXCEDIDO, ex.getMessage());
    }

    @Test
    void uploadMine_throwsWhenFilenameIsUnsafe() {
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        MultipartFile file = new MockMultipartFile("file", "../secreto.pdf", "application/pdf", "contenido".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.uploadMine(file));

        assertEquals(ErrorMessages.ERROR_NOMBRE_INVALIDO, ex.getMessage());
    }

    @Test
    void uploadMine_throwsWhenContentTypeIsNotAllowed() {
        ReflectionTestUtils.setField(service, "allowedTypes", "application/pdf");
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));

        MultipartFile file = new MockMultipartFile("file", "nota.txt", "text/plain", "contenido".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.uploadMine(file));

        assertEquals(ErrorMessages.ERROR_TIPO_NO_PERMITIDO, ex.getMessage());
    }

    @Test
    void uploadMine_savesMetadataAndFile() throws IOException {
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(archivoClinicoRepository.save(any(ArchivoClinico.class))).thenAnswer(invocation -> {
            ArchivoClinico toSave = invocation.getArgument(0);
            toSave.setId(UUID.randomUUID());
            return toSave;
        });

        MultipartFile file = new MockMultipartFile("file", "informe.pdf", "application/pdf", "contenido".getBytes());

        ArchivoClinico saved = service.uploadMine(file);

        assertNotNull(saved.getId());
        assertEquals("informe.pdf", saved.getNombreOriginal());
        assertEquals("application/pdf", saved.getContentType());
        assertEquals(file.getSize(), saved.getSizeBytes());
        assertTrue(Files.exists(Path.of(saved.getRutaAlmacenada())));

        ArgumentCaptor<ArchivoClinico> captor = ArgumentCaptor.forClass(ArchivoClinico.class);
        verify(archivoClinicoRepository).save(captor.capture());
        assertEquals(usuario.getId(), captor.getValue().getUsuario().getId());
    }

    @Test
    void getMineResource_returnsResourceWhenOwnedAndReadable() throws IOException {
        Path archivoFisico = tempDir.resolve("reporte.pdf");
        Files.writeString(archivoFisico, "contenido");

        ArchivoClinico archivo = new ArchivoClinico();
        archivo.setId(UUID.randomUUID());
        archivo.setUsuario(usuario);
        archivo.setNombreOriginal("reporte.pdf");
        archivo.setRutaAlmacenada(archivoFisico.toString());

        when(archivoClinicoRepository.findByIdAndUsuarioId(archivo.getId(), usuario.getId()))
                .thenReturn(Optional.of(archivo));

        Resource resource = service.getMineResource(archivo.getId());

        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
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
    void deleteMine_removesDatabaseRecordAndPhysicalFile() throws IOException {
        Path archivoFisico = tempDir.resolve("a-eliminar.pdf");
        Files.writeString(archivoFisico, "contenido");

        ArchivoClinico archivo = new ArchivoClinico();
        UUID archivoId = UUID.randomUUID();
        archivo.setId(archivoId);
        archivo.setUsuario(usuario);
        archivo.setNombreOriginal("a-eliminar.pdf");
        archivo.setRutaAlmacenada(archivoFisico.toString());

        when(archivoClinicoRepository.findByIdAndUsuarioId(archivoId, usuario.getId()))
                .thenReturn(Optional.of(archivo));

        service.deleteMine(archivoId);

        verify(archivoClinicoRepository).delete(archivo);
        assertTrue(Files.notExists(archivoFisico));
    }

    @Test
    void deleteMine_doesNotFailWhenPhysicalFileDoesNotExist() {
        ArchivoClinico archivo = new ArchivoClinico();
        UUID archivoId = UUID.randomUUID();
        archivo.setId(archivoId);
        archivo.setUsuario(usuario);
        archivo.setNombreOriginal("inexistente.pdf");
        archivo.setRutaAlmacenada(tempDir.resolve("inexistente.pdf").toString());

        when(archivoClinicoRepository.findByIdAndUsuarioId(archivoId, usuario.getId()))
                .thenReturn(Optional.of(archivo));

        assertDoesNotThrow(() -> service.deleteMine(archivoId));
        verify(archivoClinicoRepository).delete(archivo);
    }
}
