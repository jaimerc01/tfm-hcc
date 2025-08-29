package com.hcc.tfm_hcc.controller.impl;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.controller.HistorialClinicoController;
import com.hcc.tfm_hcc.dto.ArchivoClinicoDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.facade.HistorialClinicoFacade;
import com.hcc.tfm_hcc.constants.ErrorMessages;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del controlador REST para la gestión integral de historiales clínicos.
 * Proporciona endpoints seguros para la manipulación de archivos clínicos y datos
 * médicos de los pacientes autenticados en el sistema HCC.
 * 
 * <p>Esta implementación gestiona:</p>
 * <ul>
 *   <li>Operaciones CRUD completas sobre archivos clínicos</li>
 *   <li>Actualización de información médica específica del paciente</li>
 *   <li>Gestión de antecedentes familiares y personales</li>
 *   <li>Administración de alergias y análisis de sangre</li>
 *   <li>Validación y procesamiento de contenido URL-encoded</li>
 * </ul>
 * 
 * <p>Características de seguridad:</p>
 * <ul>
 *   <li>Todos los endpoints requieren autenticación</li>
 *   <li>Acceso restringido a datos del usuario autenticado</li>
 *   <li>Validación exhaustiva de parámetros de entrada</li>
 *   <li>Manejo centralizado de errores y excepciones</li>
 * </ul>
 * 
 * <p>La clase implementa el patrón de inyección de dependencias mediante constructor
 * y utiliza logging estructurado para auditoría y monitoreo.</p>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 * @see HistorialClinicoController
 * @see HistorialClinicoFacade
 */
@Slf4j
@RestController
@RequestMapping("/historia")
@RequiredArgsConstructor
public class HistorialClinicoControllerImpl implements HistorialClinicoController {

    /**
     * Facade que encapsula la lógica de negocio para historiales clínicos.
     * Proporciona una capa de abstracción entre el controlador REST y los servicios
     * de dominio, facilitando la gestión de archivos y datos clínicos.
     */
    private final HistorialClinicoFacade historialClinicoFacade;

    /**
     * {@inheritDoc}
     * 
     * <p>Implementación que obtiene todos los archivos clínicos asociados
     * al usuario autenticado actualmente en la sesión.</p>
     * 
     * @return Lista de DTOs con metadatos de archivos del usuario actual
     */
    @Override
    @GetMapping("/archivos")
    public List<ArchivoClinicoDTO> listMine() {
        log.debug("Solicitando lista de archivos clínicos para el usuario autenticado");
        
        try {
            List<ArchivoClinicoDTO> archivos = historialClinicoFacade.listMine();
            log.info("Se obtuvieron {} archivos clínicos para el usuario", archivos.size());
            return archivos;
        } catch (Exception e) {
            log.error("Error al obtener archivos clínicos del usuario: {}", e.getMessage(), e);
            throw new RuntimeException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementación que procesa la subida de archivos clínicos con validación
     * exhaustiva del contenido y almacenamiento seguro en el sistema.</p>
     * 
     * <p>Características del procesamiento:</p>
     * <ul>
     *   <li>Validación de tipo y tamaño de archivo</li>
     *   <li>Asociación automática con el usuario autenticado</li>
     *   <li>Generación de metadatos del archivo</li>
     *   <li>Almacenamiento en ubicación segura</li>
     * </ul>
     * 
     * @param file Archivo multipart con el contenido clínico a subir
     * @return ResponseEntity con ArchivoClinicoDTO del archivo procesado
     * @throws IOException si hay error en el procesamiento del archivo
     */
    @Override
    @PostMapping(path = "/archivos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArchivoClinicoDTO> upload(@RequestParam("file") MultipartFile file) throws IOException {
        log.debug("Iniciando subida de archivo clínico: {}", 
                 file != null ? file.getOriginalFilename() : "null");
        
        try {
            validarArchivoSubida(file);
            
            ArchivoClinicoDTO dto = historialClinicoFacade.upload(file);
            log.info("Archivo clínico subido exitosamente con ID: {}", dto.getId());
            
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación en subida de archivo: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("Error de I/O al subir archivo {}: {}", 
                     file != null ? file.getOriginalFilename() : "null", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al subir archivo: {}", e.getMessage(), e);
            throw new IOException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementación que procesa la descarga segura de archivos clínicos
     * con validación de permisos y configuración apropiada de headers HTTP.</p>
     * 
     * <p>Características de la descarga:</p>
     * <ul>
     *   <li>Verificación de pertenencia del archivo al usuario</li>
     *   <li>Configuración automática de Content-Type</li>
     *   <li>Soporte para nombres de archivo Unicode</li>
     *   <li>Headers de seguridad y CORS apropiados</li>
     * </ul>
     * 
     * @param id Identificador único del archivo clínico a descargar
     * @return ResponseEntity con el Resource del archivo para descarga
     */
    @Override
    @GetMapping("/files/{id}")
    public ResponseEntity<Resource> download(@PathVariable("id") UUID id) {
        log.debug("Solicitando descarga de archivo clínico con ID: {}", id);
        
        try {
            ArchivoClinicoDTO meta = historialClinicoFacade.getMine(id);
            Resource resource = historialClinicoFacade.getMineResource(id);
            
            String nombreArchivo = obtenerNombreArchivoSeguro(meta.getNombreOriginal());
            String contentDisposition = construirContentDisposition(nombreArchivo);
            MediaType mediaType = determinarMediaType(meta.getContentType());
            
            log.info("Descargando archivo clínico: {} (ID: {})", nombreArchivo, id);
            
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .header("Access-Control-Expose-Headers", HttpHeaders.CONTENT_DISPOSITION)
                    .body(resource);
                    
        } catch (IllegalArgumentException e) {
            log.warn("Archivo clínico no encontrado para descarga: ID {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error al descargar archivo clínico con ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementación que elimina de forma segura archivos clínicos
     * del usuario autenticado, incluyendo tanto los metadatos como
     * el archivo físico del sistema de almacenamiento.</p>
     * 
     * @param id Identificador único del archivo clínico a eliminar
     * @return ResponseEntity vacío confirmando la eliminación exitosa
     * @throws IOException si hay error al eliminar el archivo físico
     */
    @Override
    @DeleteMapping("/files/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) throws IOException {
        log.debug("Solicitando eliminación de archivo clínico con ID: {}", id);
        
        try {
            historialClinicoFacade.delete(id);
            log.info("Archivo clínico eliminado exitosamente: ID {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Archivo clínico no encontrado para eliminación: ID {}", id);
            throw e;
        } catch (IOException e) {
            log.error("Error de I/O al eliminar archivo clínico con ID {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al eliminar archivo clínico con ID {}: {}", id, e.getMessage(), e);
            throw new IOException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementación que recupera el historial clínico completo del usuario
     * autenticado, incluyendo datos de identificación, antecedentes, alergias
     * y análisis de sangre.</p>
     * 
     * @return ResponseEntity con el HistorialClinicoDTO del usuario actual
     */
    @Override
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HistorialClinicoDTO> getMiHistoria() {
        log.debug("Solicitando historial clínico del usuario autenticado");
        
        try {
            HistorialClinicoDTO dto = historialClinicoFacade.obtenerMiHistoria();
            if (dto == null) {
                log.info("No se encontró historial clínico, retornando DTO vacío");
                dto = new HistorialClinicoDTO();
            }
            
            log.info("Historial clínico obtenido exitosamente para el usuario");
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Error al obtener historial clínico del usuario: {}", e.getMessage(), e);
            throw new RuntimeException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementación que actualiza la información de identificación
     * del paciente en su historial clínico.</p>
     * 
     * @param identificacionJson Datos de identificación en formato JSON
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @Override
    @PutMapping("/identificacion")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HistorialClinicoDTO> actualizarIdentificacion(@RequestBody String identificacionJson) {
        log.debug("Actualizando información de identificación del usuario");
        
        try {
            HistorialClinicoDTO resultado = historialClinicoFacade.actualizarIdentificacion(identificacionJson);
            log.info("Información de identificación actualizada exitosamente");
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al actualizar información de identificación: {}", e.getMessage(), e);
            throw new RuntimeException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementación que actualiza los antecedentes familiares del paciente
     * con soporte para contenido URL-encoded y validación de entrada.</p>
     * 
     * @param antecedentesFamiliares Texto con los antecedentes familiares
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @Override
    @PutMapping("/antecedentes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HistorialClinicoDTO> actualizarAntecedentes(@RequestBody String antecedentesFamiliares) {
        log.debug("Actualizando antecedentes familiares del usuario");
        
        try {
            String payload = procesarContenidoUrlEncoded(antecedentesFamiliares);
            HistorialClinicoDTO resultado = historialClinicoFacade.actualizarAntecedentes(payload);
            log.info("Antecedentes familiares actualizados exitosamente");
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al actualizar antecedentes familiares: {}", e.getMessage(), e);
            throw new RuntimeException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementación que actualiza la información de alergias del paciente
     * con procesamiento de datos JSON y validación de entrada.</p>
     * 
     * @param alergiasJson Datos de alergias en formato JSON
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @Override
    @PutMapping("/alergias")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HistorialClinicoDTO> actualizarAlergias(@RequestBody String alergiasJson) {
        log.debug("Actualizando información de alergias del usuario");
        
        try {
            String payload = procesarContenidoUrlEncoded(alergiasJson);
            HistorialClinicoDTO resultado = historialClinicoFacade.actualizarAlergias(payload);
            log.info("Información de alergias actualizada exitosamente");
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al actualizar información de alergias: {}", e.getMessage(), e);
            throw new RuntimeException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementación que actualiza los análisis de sangre del paciente
     * con procesamiento de datos JSON y validación de entrada.</p>
     * 
     * @param analisisJson Datos de análisis de sangre en formato JSON
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @Override
    @PutMapping("/analisis-sangre")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HistorialClinicoDTO> actualizarAnalisisSangre(@RequestBody String analisisJson) {
        log.debug("Actualizando análisis de sangre del usuario");
        
        try {
            String payload = procesarContenidoUrlEncoded(analisisJson);
            HistorialClinicoDTO resultado = historialClinicoFacade.actualizarAnalisisSangre(payload);
            log.info("Análisis de sangre actualizados exitosamente");
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al actualizar análisis de sangre: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al actualizar análisis de sangre: {}", e.getMessage(), e);
            throw new RuntimeException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementación que elimina un dato clínico específico del historial
     * del usuario autenticado.</p>
     * 
     * @param id Identificador único del dato clínico a eliminar
     * @return ResponseEntity vacío confirmando la eliminación
     */
    @Override
    @DeleteMapping("/datos-clinicos/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> borrarDatoClinico(@PathVariable("id") UUID id) {
        log.debug("Solicitando eliminación de dato clínico con ID: {}", id);
        
        try {
            historialClinicoFacade.borrarDatoClinico(id);
            log.info("Dato clínico eliminado exitosamente: ID {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Dato clínico no encontrado para eliminación: ID {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error al eliminar dato clínico con ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementación que elimina un antecedente específico por su índice
     * y retorna el historial actualizado.</p>
     * 
     * @param index Índice del antecedente a eliminar
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @Override
    @DeleteMapping("/antecedentes/{index}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HistorialClinicoDTO> borrarAntecedente(@PathVariable("index") int index) {
        log.debug("Solicitando eliminación de antecedente en índice: {}", index);
        
        try {
            HistorialClinicoDTO resultado = historialClinicoFacade.borrarAntecedente(index);
            log.info("Antecedente eliminado exitosamente en índice: {}", index);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al eliminar antecedente en índice {}: {}", index, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al eliminar antecedente en índice {}: {}", index, e.getMessage(), e);
            throw new RuntimeException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementación que edita un antecedente específico por su índice
     * con procesamiento de contenido URL-encoded.</p>
     * 
     * @param index Índice del antecedente a editar
     * @param texto Nuevo texto para el antecedente
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @Override
    @PutMapping("/antecedentes/{index}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HistorialClinicoDTO> editarAntecedente(@PathVariable("index") int index, @RequestBody String texto) {
        log.debug("Solicitando edición de antecedente en índice: {}", index);
        
        try {
            String payload = procesarContenidoUrlEncoded(texto);
            HistorialClinicoDTO resultado = historialClinicoFacade.editarAntecedente(index, payload);
            log.info("Antecedente editado exitosamente en índice: {}", index);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al editar antecedente en índice {}: {}", index, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al editar antecedente en índice {}: {}", index, e.getMessage(), e);
            throw new RuntimeException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    // ===============================
    // MÉTODOS UTILITARIOS PRIVADOS
    // ===============================

    /**
     * Valida que el archivo para subida sea válido.
     * 
     * @param file Archivo a validar
     * @throws IllegalArgumentException si el archivo no es válido
     */
    private void validarArchivoSubida(MultipartFile file) throws IllegalArgumentException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("archivo"));
        }
        
        String nombreArchivo = file.getOriginalFilename();
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("nombre de archivo"));
        }
    }

    /**
     * Obtiene un nombre de archivo seguro para descarga.
     * 
     * @param nombreOriginal Nombre original del archivo
     * @return Nombre de archivo seguro
     */
    private String obtenerNombreArchivoSeguro(String nombreOriginal) {
        return nombreOriginal == null ? "archivo" : nombreOriginal;
    }

    /**
     * Construye el header Content-Disposition para descarga de archivos.
     * 
     * @param nombreArchivo Nombre del archivo
     * @return Content-Disposition header
     */
    private String construirContentDisposition(String nombreArchivo) {
        // Fallback ASCII/quoted filename and RFC 5987 filename*
        String quoted = nombreArchivo.replace("\\", "_")
                                   .replace("\r", " ")
                                   .replace("\n", " ")
                                   .replace("\"", "'");
        String encoded = URLEncoder.encode(nombreArchivo, StandardCharsets.UTF_8)
                                 .replace("+", "%20");
        return "attachment; filename=\"" + quoted + "\"; filename*=UTF-8''" + encoded;
    }

    /**
     * Determina el MediaType apropiado para un archivo.
     * 
     * @param contentType Content-Type del archivo
     * @return MediaType apropiado
     */
    private MediaType determinarMediaType(String contentType) {
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (contentType != null) {
            try {
                mediaType = MediaType.parseMediaType(contentType);
            } catch (Exception e) {
                log.warn("No se pudo parsear content-type: {}", contentType);
            }
        }
        return mediaType;
    }

    /**
     * Procesa contenido que puede estar URL-encoded.
     * 
     * @param contenido Contenido a procesar
     * @return Contenido decodificado si corresponde
     */
    private String procesarContenidoUrlEncoded(String contenido) {
        if (contenido == null) {
            return null;
        }
        
        String payload = contenido;
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = attrs != null ? attrs.getRequest() : null;
        String contentType = req != null ? req.getContentType() : null;
        
        if (contentType != null && contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
            payload = URLDecoder.decode(contenido, StandardCharsets.UTF_8);
        } else if (contenido.contains("%") || contenido.contains("+")) {
            try {
                payload = URLDecoder.decode(contenido, StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.debug("No se pudo decodificar contenido URL-encoded: {}", e.getMessage());
            }
        }
        
        return payload;
    }
}
