package com.hcc.tfm_hcc.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.model.ArchivoClinico;
import com.hcc.tfm_hcc.model.AuditoriaCambio;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.ArchivoClinicoRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.ArchivoCifradoService;
import com.hcc.tfm_hcc.service.AuditoriaCambioService;
import com.hcc.tfm_hcc.service.FieldEncryptionService;
import com.hcc.tfm_hcc.service.RelacionMedicoPacienteService;
import com.hcc.tfm_hcc.util.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de archivos clínicos.
 *
 * <p>Esta clase proporciona la implementación de los servicios relacionados
 * con la gestión de archivos clínicos de los usuarios.</p>
 *
 * <p>Responsabilidades principales:</p>
 * <ul>
 *   <li>Gestión de subida de archivos con validaciones de seguridad</li>
 *   <li>Almacenamiento seguro en MongoDB, con el contenido cifrado con AES/GCM</li>
 *   <li>Control de acceso basado en propietario</li>
 *   <li>Gestión del ciclo de vida de archivos</li>
 * </ul>
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 2024
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArchivoClinicoServiceImpl implements com.hcc.tfm_hcc.service.ArchivoClinicoService {

    // Constantes de configuración
    private static final String ARCHIVO_DEFAULT_NAME = "archivo";
    private static final String WILDCARD_CONTENT_TYPE = "*/*";
    private static final String PATH_SEPARATOR = "..";
    private static final String FORWARD_SLASH = "/";
    private static final String BACKSLASH = "\\";
    private static final String ALLOWED_TYPES_SEPARATOR = ",";
    private static final String ZONE_ID_EUROPA_MADRID = "Europe/Madrid";

    // Constantes de logging
    private static final String LOG_LISTANDO_ARCHIVOS = "Listando archivos del usuario: {}";
    private static final String LOG_ARCHIVOS_ENCONTRADOS = "Se encontraron {} archivos para el usuario: {}";
    private static final String LOG_SUBIENDO_ARCHIVO = "Iniciando subida de archivo: {} por usuario: {}";
    private static final String LOG_ARCHIVO_SUBIDO = "Archivo subido exitosamente: {} con ID: {} por usuario: {}";
    private static final String LOG_DESCARGANDO_ARCHIVO = "Descargando archivo ID: {} por usuario: {}";
    private static final String LOG_ARCHIVO_DESCARGADO = "Archivo descargado exitosamente: {} por usuario: {}";
    private static final String LOG_ELIMINANDO_ARCHIVO = "Eliminando archivo ID: {} por usuario: {}";
    private static final String LOG_ARCHIVO_ELIMINADO = "Archivo eliminado exitosamente: {} por usuario: {}";

    private static final String BORRADO_ARCHIVO_CLINICO = "BORRADO_ARCHIVO_CLINICO";
    private static final String SUBIDA_ARCHIVO_CLINICO = "SUBIDA_ARCHIVO_CLINICO";
    private static final String ARCHIVO_CLINICO = "archivo_clinico";
    private static final String SUBIDA_ARCHIVO_POR_MEDICO = "Subida de archivo clínico por el médico";

    // Dependencias inyectadas por constructor
    private final ArchivoClinicoRepository archivoClinicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaCambioService auditoriaCambioService;
    private final ArchivoCifradoService archivoCifradoService;
    private final FieldEncryptionService fieldEncryptionService;
    private final RelacionMedicoPacienteService relacionMedicoPacienteService;

    @Value("${app.uploads.max-size-bytes:10485760}") // 10 MB por defecto
    private long maxSizeBytes;

    /** Tipos MIME admitidos, separados por comas (vacío = sin restricción). */
    @Value("${app.uploads.allowed-types:}")
    private String allowedTypes;

    /** Extensiones admitidas, separadas por comas y sin punto (vacío = sin restricción). */
    @Value("${app.uploads.allowed-extensions:}")
    private String allowedExtensions;

    /**
     * Obtiene el ID del usuario autenticado actualmente
     */
    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) {
            throw new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }
        return ((Usuario) auth.getPrincipal()).getId();
    }

    /**
     * Obtiene el usuario autenticado completo desde la base de datos
     */
    private Usuario getCurrentUser() {
        UUID userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }
        return usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO));
    }

    /**
     * Lista todos los archivos clínicos del usuario autenticado, con el
     * nombre original ya descifrado.
     *
     * @return lista de archivos clínicos ordenados por fecha de creación descendente
     */
    @Override
    @Transactional(readOnly = true)
    public List<ArchivoClinico> listMine() {
        UUID userId = getCurrentUserId();
        log.debug(LOG_LISTANDO_ARCHIVOS, userId);

        List<ArchivoClinico> archivos = archivoClinicoRepository.findByUsuarioIdOrderByFechaCreacionDesc(userId);
        archivos.forEach(this::descifrarNombre);
        log.info(LOG_ARCHIVOS_ENCONTRADOS, archivos.size(), userId);

        return archivos;
    }

    /**
     * Sube un archivo clínico para el usuario autenticado.
     *
     * @param file el archivo a subir
     * @return el registro del archivo clínico creado
     * @throws IOException si ocurre un error durante el cifrado del contenido
     * @throws IllegalArgumentException si el archivo es inválido
     */
    @Override
    @Transactional
    public ArchivoClinico uploadMine(MultipartFile file) throws IOException {
        validarArchivoSubida(file);

        Usuario usuario = getCurrentUser();
        String nombreOriginal = procesarNombreArchivo(file.getOriginalFilename());

        log.debug(LOG_SUBIENDO_ARCHIVO, nombreOriginal, usuario.getId());

        validarExtensionArchivo(nombreOriginal);
        validarTipoArchivo(file.getContentType());

        byte[] contenidoCifrado = cifrarContenido(file);
        ArchivoClinico archivo = crearRegistroArchivo(file, usuario, nombreOriginal, contenidoCifrado);
        log.info(LOG_ARCHIVO_SUBIDO, nombreOriginal, archivo.getId(), usuario.getId());

        auditoriaCambioService.registrarCambio(
            usuario.getId().toString(),
            usuario.getId().toString(),
            null,
            SUBIDA_ARCHIVO_CLINICO,
            ARCHIVO_CLINICO,
            archivo.getId().toString(),
            null,
            null,
            AuditoriaCambio.TipoOperacion.CREATE,
            "Subida de archivo clínico"
        );

        descifrarNombre(archivo);
        return archivo;
    }

    /**
     * Valida que el archivo sea válido para subir
     */
    private void validarArchivoSubida(MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_NO_ARCHIVO);
        }
        if (file.isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_ARCHIVO_VACIO);
        }
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_TAMAÑO_EXCEDIDO);
        }
    }

    /**
     * Procesa y valida el nombre del archivo
     */
    private String procesarNombreArchivo(String nombreOriginal) {
        String nombre = nombreOriginal;
        if (nombre == null || nombre.isBlank()) {
            nombre = ARCHIVO_DEFAULT_NAME;
        }

        nombre = StringUtils.cleanPath(nombre);

        if (esNombreArchivoInseguro(nombre)) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_NOMBRE_INVALIDO);
        }

        return nombre;
    }

    /**
     * Verifica si el nombre del archivo es inseguro
     */
    private boolean esNombreArchivoInseguro(String nombre) {
        return nombre.contains(PATH_SEPARATOR) ||
               nombre.startsWith(FORWARD_SLASH) ||
               nombre.startsWith(BACKSLASH);
    }

    /**
     * Valida la extensión del archivo contra la lista blanca configurada
     * ({@code app.uploads.allowed-extensions}). Complementa a {@link #validarTipoArchivo(String)}:
     * el {@code Content-Type} lo controla el cliente, la extensión al menos limita lo que
     * quedará almacenado y se servirá después.
     */
    private void validarExtensionArchivo(String nombre) {
        if (this.allowedExtensions == null || this.allowedExtensions.isBlank()) {
            return; // Sin restricciones
        }

        int punto = nombre.lastIndexOf('.');
        String extension = (punto >= 0 && punto < nombre.length() - 1)
                ? nombre.substring(punto + 1).toLowerCase()
                : "";

        boolean permitida = Arrays.stream(this.allowedExtensions.split(ALLOWED_TYPES_SEPARATOR))
                .map(String::trim)
                .map(String::toLowerCase)
                .anyMatch(extension::equals);

        if (!permitida) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_EXTENSION_NO_PERMITIDA);
        }
    }

    /**
     * Valida el tipo de contenido del archivo
     */
    private void validarTipoArchivo(String contentType) {
        if (this.allowedTypes == null || this.allowedTypes.isBlank()) {
            return; // Sin restricciones
        }

        String[] tiposPermitidos = this.allowedTypes.split(ALLOWED_TYPES_SEPARATOR);
        boolean tipoPermitido = false;

        for (String tipo : tiposPermitidos) {
            String tipoLimpio = tipo.trim();

            if (WILDCARD_CONTENT_TYPE.equals(tipoLimpio) || (contentType != null && contentType.startsWith(tipoLimpio))) {
                tipoPermitido = true;
                break;
            }
        }

        if (!tipoPermitido) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_TIPO_NO_PERMITIDO);
        }
    }

    /**
     * Cifra el contenido del archivo subido con AES/GCM (ver
     * {@link ArchivoCifradoService}), listo para guardarse en MongoDB.
     */
    private byte[] cifrarContenido(MultipartFile file) throws IOException {
        try (InputStream entrada = file.getInputStream();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            archivoCifradoService.cifrar(entrada, salida);
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new IOException(ErrorMessages.ERROR_GUARDAR_ARCHIVO, ex);
        }
    }

    /**
     * Crea el registro del archivo clínico, con el nombre original cifrado
     * antes de guardarlo en MongoDB.
     */
    private ArchivoClinico crearRegistroArchivo(MultipartFile file, Usuario usuario,
                                              String nombreOriginal, byte[] contenidoCifrado) {
        ArchivoClinico archivo = new ArchivoClinico();
        archivo.setId(UUID.randomUUID());
        archivo.setUsuarioId(usuario.getId());
        archivo.setNombreOriginal(fieldEncryptionService.cifrar(nombreOriginal));
        archivo.setContentType(file.getContentType());
        archivo.setSizeBytes(file.getSize());
        archivo.setContenido(contenidoCifrado);
        archivo.setFechaCreacion(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPA_MADRID)));

        ArchivoClinico guardado = archivoClinicoRepository.save(archivo);
        guardado.setNombreOriginal(nombreOriginal);
        return guardado;
    }

    /**
     * Descifra el nombre original de un archivo clínico leído de MongoDB,
     * dejándolo en claro en el propio objeto en memoria.
     */
    private void descifrarNombre(ArchivoClinico archivo) {
        archivo.setNombreOriginal(fieldEncryptionService.descifrar(archivo.getNombreOriginal()));
    }

    /**
     * Obtiene el recurso de un archivo clínico del usuario autenticado.
     *
     * @param id el ID del archivo clínico
     * @return el recurso del archivo
     * @throws IllegalArgumentException si el archivo no existe o no pertenece al usuario
     */
    @Override
    @Transactional(readOnly = true)
    public Resource getMineResource(UUID id) {
        UUID userId = getCurrentUserId();
        log.debug(LOG_DESCARGANDO_ARCHIVO, id, userId);

        ArchivoClinico archivo = obtenerArchivoUsuario(id, userId);
        Resource resource = crearResourceDesdeArchivo(archivo);

        log.info(LOG_ARCHIVO_DESCARGADO, fieldEncryptionService.descifrar(archivo.getNombreOriginal()), userId);
        return resource;
    }

    /**
     * Obtiene un archivo que pertenece al usuario especificado
     */
    private ArchivoClinico obtenerArchivoUsuario(UUID archivoId, UUID userId) {
        return archivoClinicoRepository.findByIdAndUsuarioId(archivoId, userId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ERROR_ARCHIVO_NO_EXISTE));
    }

    /**
     * Crea un Resource a partir de un archivo clínico, descifrando su contenido
     * binario (ver {@link ArchivoCifradoService}) antes de servirlo al usuario.
     */
    private Resource crearResourceDesdeArchivo(ArchivoClinico archivo) {
        if (archivo.getContenido() == null) {
            throw new IllegalStateException(ErrorMessages.ERROR_ARCHIVO_NO_ACCESIBLE);
        }

        try {
            InputStream contenidoDescifrado = archivoCifradoService.descifrar(
                    new ByteArrayInputStream(archivo.getContenido()));
            return new InputStreamResource(contenidoDescifrado);
        } catch (IOException e) {
            throw new IllegalStateException(ErrorMessages.ERROR_ARCHIVO_NO_ACCESIBLE, e);
        }
    }

    /**
     * Elimina un archivo clínico del usuario autenticado.
     *
     * @param id el ID del archivo clínico a eliminar
     * @throws IllegalArgumentException si el archivo no existe o no pertenece al usuario
     */
    @Override
    @Transactional
    public void borrarArchivo(UUID id) {
        UUID userId = getCurrentUserId();
        log.debug(LOG_ELIMINANDO_ARCHIVO, id, userId);

        ArchivoClinico archivo = obtenerArchivoUsuario(id, userId);
        String nombreOriginal = fieldEncryptionService.descifrar(archivo.getNombreOriginal());

        archivoClinicoRepository.delete(archivo);
        auditoriaCambioService.registrarCambio(
            archivo.getUsuarioId().toString(),
            archivo.getUsuarioId().toString(),
            null,
            BORRADO_ARCHIVO_CLINICO,
            ARCHIVO_CLINICO,
            archivo.getId().toString(),
            null,
            null,
            AuditoriaCambio.TipoOperacion.DELETE,
            "Eliminación de archivo clínico"
        );

        log.info(LOG_ARCHIVO_ELIMINADO, nombreOriginal, userId);
    }

    // ===============================
    // ARCHIVOS DE UN PACIENTE ASIGNADO (acceso del médico)
    // ===============================

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ArchivoClinico> listForPaciente(String nifPaciente) {
        Usuario paciente = verificarAccesoMedico(nifPaciente);
        List<ArchivoClinico> archivos = archivoClinicoRepository.findByUsuarioIdOrderByFechaCreacionDesc(paciente.getId());
        archivos.forEach(this::descifrarNombre);
        return archivos;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ArchivoClinico uploadForPaciente(String nifPaciente, MultipartFile file) throws IOException {
        Usuario paciente = verificarAccesoMedico(nifPaciente);
        Usuario medico = getCurrentUser();

        validarArchivoSubida(file);
        String nombreOriginal = procesarNombreArchivo(file.getOriginalFilename());
        validarExtensionArchivo(nombreOriginal);
        validarTipoArchivo(file.getContentType());

        byte[] contenidoCifrado = cifrarContenido(file);
        ArchivoClinico archivo = crearRegistroArchivo(file, paciente, nombreOriginal, contenidoCifrado);
        log.info("Archivo clínico subido para el paciente {} por el médico {}", paciente.getId(), medico.getId());

        auditoriaCambioService.registrarCambio(
            medico.getId().toString(),
            paciente.getId().toString(),
            medico.getId().toString(),
            SUBIDA_ARCHIVO_CLINICO,
            ARCHIVO_CLINICO,
            archivo.getId().toString(),
            null,
            null,
            AuditoriaCambio.TipoOperacion.CREATE,
            SUBIDA_ARCHIVO_POR_MEDICO
        );

        descifrarNombre(archivo);
        return archivo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Resource getPacienteResource(String nifPaciente, UUID id) {
        Usuario paciente = verificarAccesoMedico(nifPaciente);
        ArchivoClinico archivo = obtenerArchivoUsuario(id, paciente.getId());
        return crearResourceDesdeArchivo(archivo);
    }

    /**
     * Comprueba que el médico autenticado tiene acceso asistencial activo sobre el
     * paciente y devuelve la entidad del paciente.
     */
    private Usuario verificarAccesoMedico(String nifPaciente) {
        String nifMedico = SecurityUtils.getCurrentUserNif();
        if (nifMedico == null) {
            throw new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }
        return relacionMedicoPacienteService.verificarAccesoMedicoActivo(nifMedico, nifPaciente);
    }
}
