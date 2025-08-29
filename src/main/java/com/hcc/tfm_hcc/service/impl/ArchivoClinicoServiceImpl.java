package com.hcc.tfm_hcc.service.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.model.ArchivoClinico;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.ArchivoClinicoRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;

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
 *   <li>Almacenamiento seguro en el sistema de archivos</li>
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

    // Constantes de logging
    private static final String LOG_LISTANDO_ARCHIVOS = "Listando archivos del usuario: {}";
    private static final String LOG_ARCHIVOS_ENCONTRADOS = "Se encontraron {} archivos para el usuario: {}";
    private static final String LOG_SUBIENDO_ARCHIVO = "Iniciando subida de archivo: {} por usuario: {}";
    private static final String LOG_ARCHIVO_SUBIDO = "Archivo subido exitosamente: {} con ID: {} por usuario: {}";
    private static final String LOG_DESCARGANDO_ARCHIVO = "Descargando archivo ID: {} por usuario: {}";
    private static final String LOG_ARCHIVO_DESCARGADO = "Archivo descargado exitosamente: {} por usuario: {}";
    private static final String LOG_ELIMINANDO_ARCHIVO = "Eliminando archivo ID: {} por usuario: {}";
    private static final String LOG_ARCHIVO_ELIMINADO = "Archivo eliminado exitosamente: {} por usuario: {}";

    // Dependencias inyectadas por constructor
    private final ArchivoClinicoRepository archivoClinicoRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.uploads.base-dir:uploads}")
    private String baseDir;

    @Value("${app.uploads.max-size-bytes:10485760}") // 10 MB por defecto
    private long maxSizeBytes;

    @Value("${app.uploads.allowed-types:}") // vacío = sin restricción
    private String allowedTypes;

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
        return usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO));
    }

    /**
     * Construye el directorio específico del usuario
     */
    private Path userDir(UUID userId) {
        return Paths.get(baseDir, userId.toString());
    }

    /**
     * Lista todos los archivos clínicos del usuario autenticado.
     *
     * @return lista de archivos clínicos ordenados por fecha de creación descendente
     */
    @Override
    @Transactional(readOnly = true)
    public List<ArchivoClinico> listMine() {
        UUID userId = getCurrentUserId();
        log.debug(LOG_LISTANDO_ARCHIVOS, userId);
        
        List<ArchivoClinico> archivos = archivoClinicoRepository.findByUsuarioIdOrderByFechaCreacionDesc(userId);
        log.info(LOG_ARCHIVOS_ENCONTRADOS, archivos.size(), userId);
        
        return archivos;
    }

    /**
     * Sube un archivo clínico para el usuario autenticado.
     *
     * @param file el archivo a subir
     * @return el registro del archivo clínico creado
     * @throws IOException si ocurre un error durante el almacenamiento
     * @throws IllegalArgumentException si el archivo es inválido
     */
    @Override
    @Transactional
    public ArchivoClinico uploadMine(MultipartFile file) throws IOException {
        validarArchivoSubida(file);
        
        Usuario usuario = getCurrentUser();
        String nombreOriginal = procesarNombreArchivo(file.getOriginalFilename());
        
        log.debug(LOG_SUBIENDO_ARCHIVO, nombreOriginal, usuario.getId());
        
        validarTipoArchivo(file.getContentType());
        
        String nombreAlmacenado = generarNombreAlmacenado(nombreOriginal);
        Path rutaDestino = almacenarArchivo(file, usuario.getId(), nombreAlmacenado);
        
        ArchivoClinico archivo = crearRegistroArchivo(file, usuario, nombreOriginal, rutaDestino);
        log.info(LOG_ARCHIVO_SUBIDO, nombreOriginal, archivo.getId(), usuario.getId());
        
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
     * Valida el tipo de contenido del archivo
     */
    private void validarTipoArchivo(String contentType) {
        if (allowedTypes == null || allowedTypes.isBlank()) {
            return; // Sin restricciones
        }

        String[] tiposPermitidos = allowedTypes.split(ALLOWED_TYPES_SEPARATOR);
        boolean tipoPermitido = false;
        
        for (String tipo : tiposPermitidos) {
            String tipoLimpio = tipo.trim();
            if (tipoLimpio.isEmpty()) continue;
            
            if (WILDCARD_CONTENT_TYPE.equals(tipoLimpio)) {
                tipoPermitido = true;
                break;
            }
            
            if (contentType != null && esContentTypeCompatible(contentType, tipoLimpio)) {
                tipoPermitido = true;
                break;
            }
        }
        
        if (!tipoPermitido) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_TIPO_NO_PERMITIDO);
        }
    }

    /**
     * Verifica si el content type es compatible con el tipo permitido
     */
    private boolean esContentTypeCompatible(String contentType, String tipoPermitido) {
        return contentType.equalsIgnoreCase(tipoPermitido) || 
               contentType.toLowerCase().startsWith(tipoPermitido.toLowerCase().replace("*", ""));
    }

    /**
     * Genera un nombre único para almacenar el archivo
     */
    private String generarNombreAlmacenado(String nombreOriginal) {
        String extension = extraerExtension(nombreOriginal);
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * Extrae la extensión del archivo
     */
    private String extraerExtension(String nombreArchivo) {
        int ultimoPunto = nombreArchivo.lastIndexOf('.');
        if (ultimoPunto > 0 && ultimoPunto < nombreArchivo.length() - 1) {
            return nombreArchivo.substring(ultimoPunto);
        }
        return "";
    }

    /**
     * Almacena físicamente el archivo en el sistema de archivos
     */
    private Path almacenarArchivo(MultipartFile file, UUID userId, String nombreAlmacenado) throws IOException {
        Path directorio = userDir(userId);
        Files.createDirectories(directorio);
        
        Path rutaDestino = directorio.resolve(nombreAlmacenado);
        
        try {
            Files.copy(file.getInputStream(), rutaDestino);
            return rutaDestino;
        } catch (IOException ex) {
            throw new IOException(ErrorMessages.ERROR_GUARDAR_ARCHIVO, ex);
        }
    }

    /**
     * Crea el registro del archivo en la base de datos
     */
    private ArchivoClinico crearRegistroArchivo(MultipartFile file, Usuario usuario, 
                                              String nombreOriginal, Path rutaDestino) {
        ArchivoClinico archivo = new ArchivoClinico();
        archivo.setUsuario(usuario);
        archivo.setNombreOriginal(nombreOriginal);
        archivo.setContentType(file.getContentType());
        archivo.setSizeBytes(file.getSize());
        archivo.setRutaAlmacenada(rutaDestino.toString());
        archivo.setFechaCreacion(LocalDateTime.now());
        archivo.setFechaUltimaModificacion(LocalDateTime.now());
        
        return archivoClinicoRepository.save(archivo);
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
        
        log.info(LOG_ARCHIVO_DESCARGADO, archivo.getNombreOriginal(), userId);
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
     * Crea un Resource a partir de un archivo clínico
     */
    private Resource crearResourceDesdeArchivo(ArchivoClinico archivo) {
        try {
            Path rutaArchivo = Paths.get(archivo.getRutaAlmacenada());
            Resource resource = new UrlResource(rutaArchivo.toUri());
            
            validarResourceAccesible(resource);
            
            return resource;
        } catch (MalformedURLException e) {
            throw new IllegalStateException(ErrorMessages.ERROR_RUTA_INVALIDA, e);
        }
    }

    /**
     * Valida que el resource sea accesible
     */
    private void validarResourceAccesible(Resource resource) {
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalStateException(ErrorMessages.ERROR_ARCHIVO_NO_ACCESIBLE);
        }
    }

    /**
     * Elimina un archivo clínico del usuario autenticado.
     *
     * @param id el ID del archivo clínico a eliminar
     * @throws IOException si ocurre un error durante la eliminación
     * @throws IllegalArgumentException si el archivo no existe o no pertenece al usuario
     */
    @Override
    @Transactional
    public void deleteMine(UUID id) throws IOException {
        UUID userId = getCurrentUserId();
        log.debug(LOG_ELIMINANDO_ARCHIVO, id, userId);
        
        ArchivoClinico archivo = obtenerArchivoUsuario(id, userId);
        String nombreOriginal = archivo.getNombreOriginal();
        
        eliminarRegistroYArchivo(archivo);
        log.info(LOG_ARCHIVO_ELIMINADO, nombreOriginal, userId);
    }

    /**
     * Elimina tanto el registro de la base de datos como el archivo físico
     */
    private void eliminarRegistroYArchivo(ArchivoClinico archivo) throws IOException {
        Path rutaArchivo = Paths.get(archivo.getRutaAlmacenada());
        
        // Eliminar primero el registro de la base de datos
        archivoClinicoRepository.delete(archivo);
        
        // Luego eliminar el archivo físico si existe
        eliminarArchivoFisico(rutaArchivo);
    }

    /**
     * Elimina el archivo físico del sistema de archivos
     */
    private void eliminarArchivoFisico(Path rutaArchivo) throws IOException {
        if (Files.exists(rutaArchivo)) {
            Files.delete(rutaArchivo);
        }
    }
}
