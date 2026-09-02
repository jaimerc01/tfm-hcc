package com.hcc.tfm_hcc.controller.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.controller.HistorialClinicoController;
import com.hcc.tfm_hcc.constants.RestUrls;
import com.hcc.tfm_hcc.dto.AlergiaDTO;
import com.hcc.tfm_hcc.dto.AntecedenteClinicoDTO;
import com.hcc.tfm_hcc.dto.ArchivoClinicoDTO;
import com.hcc.tfm_hcc.dto.DatoClinicoEntradaDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.facade.HistorialClinicoFacade;
import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.exception.ArchivoClinicoException;
import com.hcc.tfm_hcc.exception.HistorialClinicoException;
import com.hcc.tfm_hcc.exception.DatosClinicosValidationException;

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
@RequestMapping(RestUrls.HISTORIA_BASE)
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
     */
    @Override
    @GetMapping(RestUrls.HISTORIA_ARCHIVOS)
    public List<ArchivoClinicoDTO> listarArchivos() throws ArchivoClinicoException {
        log.debug("Listando archivos clínicos del usuario autenticado");
        try {
            List<ArchivoClinicoDTO> archivos = historialClinicoFacade.listarArchivos();
            log.info("Se obtuvieron {} archivos clínicos para el usuario", archivos.size());
            return archivos;
        } catch (Exception e) {
            log.error("Error al obtener archivos clínicos del usuario: {}", e.getMessage(), e);
            throw new ArchivoClinicoException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
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
     */
    @Override
    @PostMapping(path = RestUrls.HISTORIA_ARCHIVOS, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArchivoClinicoDTO> subirArchivo(@RequestParam("file") MultipartFile file) 
            throws IOException, ArchivoClinicoException, DatosClinicosValidationException {
        log.debug("Iniciando subida de archivo clínico: {}", file != null ? file.getOriginalFilename() : "null");
        
        try {
            validarArchivoSubida(file);
            ArchivoClinicoDTO dto = historialClinicoFacade.upload(file);
            log.info("Archivo clínico subido exitosamente con ID: {}", dto.getId());
            return ResponseEntity.ok(dto);
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación en subida de archivo: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("Error de I/O al subir archivo {}: {}", file != null ? file.getOriginalFilename() : "null", e.getMessage(), e);
            throw new ArchivoClinicoException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        } catch (Exception e) {
            log.error("Error inesperado al subir archivo: {}", e.getMessage(), e);
            throw new ArchivoClinicoException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
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
    @GetMapping(RestUrls.HISTORIA_ARCHIVO_ID)
    public ResponseEntity<Resource> descargarArchivo(@PathVariable("id") UUID id) throws ArchivoClinicoException, DatosClinicosValidationException {
        log.debug("Solicitando descarga de archivo clínico con ID: {}", id);
        
        try {
            ArchivoClinicoDTO archivoClinico = historialClinicoFacade.getArchivoClinico(id);
            Resource resource = historialClinicoFacade.getMineResource(id);
            
            String nombreArchivo = obtenerNombreArchivoSeguro(archivoClinico.getNombreOriginal());
            String contentDisposition = construirContentDisposition(nombreArchivo);
            MediaType mediaType = determinarMediaType(archivoClinico.getContentType());
            
            log.info("Descargando archivo clínico: {} (ID: {})", nombreArchivo, id);
            
            if (mediaType == null) {
                log.warn("Tipo de contenido no reconocido para el archivo: {}", nombreArchivo);
                throw new DatosClinicosValidationException(ErrorMessages.ERROR_TIPO_CONTENIDO_NO_RECONOCIDO);
            }
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .header("Access-Control-Expose-Headers", HttpHeaders.CONTENT_DISPOSITION)
                    .body(resource);
        } catch (DatosClinicosValidationException e) {
            log.warn("Archivo clínico no encontrado para descarga: ID {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error al descargar archivo clínico con ID {}: {}", id, e.getMessage(), e);
            throw new ArchivoClinicoException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
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
     * @return ResponseEntity con el ID del archivo eliminado
     * @throws IOException si hay error al eliminar el archivo físico
     */
    @Override
    @DeleteMapping(RestUrls.HISTORIA_ARCHIVO_ID)
    public ResponseEntity<UUID> eliminarArchivo(@PathVariable("id") UUID id) throws IOException, DatosClinicosValidationException, ArchivoClinicoException {
        log.debug("Solicitando eliminación de archivo clínico con ID: {}", id);
        
        try {
            historialClinicoFacade.borrarArchivoClinico(id);
            log.info("Archivo clínico eliminado exitosamente: ID {}", id);
            return ResponseEntity.ok(id);
        } catch (DatosClinicosValidationException e) {
            log.warn("Archivo clínico no encontrado para eliminación: ID {}", id);
            throw e;
        } catch (IOException e) {
            log.error("Error de I/O al eliminar archivo clínico con ID {}: {}", id, e.getMessage(), e);
            throw new ArchivoClinicoException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        } catch (Exception e) {
            log.error("Error inesperado al eliminar archivo clínico con ID {}: {}", id, e.getMessage(), e);
            throw new ArchivoClinicoException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
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
    public ResponseEntity<HistorialClinicoDTO> getMiHistoria() throws HistorialClinicoException {
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
            throw new HistorialClinicoException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementación que crea un nuevo antecedente clínico (personal o familiar)
     * del paciente.</p>
     *
     * @param antecedenteDTO Categoría y descripción del antecedente
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @Override
    @PostMapping(RestUrls.HISTORIA_ANTECEDENTES)
    public ResponseEntity<HistorialClinicoDTO> crearAntecedente(@RequestBody AntecedenteClinicoDTO antecedenteDTO) throws HistorialClinicoException {
        log.debug("Creando antecedente clínico del usuario");

        try {
            HistorialClinicoDTO resultado = historialClinicoFacade.crearAntecedente(antecedenteDTO);
            log.info("Antecedente clínico creado exitosamente");
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al crear antecedente clínico: {}", e.getMessage(), e);
            throw new HistorialClinicoException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementación que edita un antecedente clínico existente del paciente.</p>
     *
     * @param id ID del antecedente a editar
     * @param antecedenteDTO Categoría y descripción actualizadas
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @Override
    @PutMapping(RestUrls.HISTORIA_ANTECEDENTE_ID)
    public ResponseEntity<HistorialClinicoDTO> editarAntecedente(@PathVariable("id") UUID id, @RequestBody AntecedenteClinicoDTO antecedenteDTO) throws HistorialClinicoException {
        log.debug("Editando antecedente clínico: {}", id);

        try {
            HistorialClinicoDTO resultado = historialClinicoFacade.editarAntecedente(id, antecedenteDTO);
            log.info("Antecedente clínico editado exitosamente: {}", id);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al editar antecedente clínico {}: {}", id, e.getMessage(), e);
            throw new HistorialClinicoException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementación que elimina un antecedente clínico específico del paciente.</p>
     *
     * @param id ID del antecedente a eliminar
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @Override
    @DeleteMapping(RestUrls.HISTORIA_ANTECEDENTE_ID)
    public ResponseEntity<HistorialClinicoDTO> borrarAntecedente(@PathVariable("id") UUID id) throws HistorialClinicoException {
        log.debug("Eliminando antecedente clínico: {}", id);

        try {
            HistorialClinicoDTO resultado = historialClinicoFacade.borrarAntecedente(id);
            log.info("Antecedente clínico eliminado exitosamente: {}", id);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al eliminar antecedente clínico {}: {}", id, e.getMessage(), e);
            throw new HistorialClinicoException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementación que crea una nueva alergia o intolerancia del paciente.</p>
     *
     * @param alergiaDTO Descripción de la alergia
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @Override
    @PostMapping(RestUrls.HISTORIA_ALERGIAS)
    public ResponseEntity<HistorialClinicoDTO> crearAlergia(@RequestBody AlergiaDTO alergiaDTO) throws HistorialClinicoException {
        log.debug("Creando alergia del usuario");

        try {
            HistorialClinicoDTO resultado = historialClinicoFacade.crearAlergia(alergiaDTO);
            log.info("Alergia creada exitosamente");
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al crear alergia: {}", e.getMessage(), e);
            throw new HistorialClinicoException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementación que elimina una alergia específica del paciente.</p>
     *
     * @param id ID de la alergia a eliminar
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @Override
    @DeleteMapping(RestUrls.HISTORIA_ALERGIA_ID)
    public ResponseEntity<HistorialClinicoDTO> borrarAlergia(@PathVariable("id") UUID id) throws HistorialClinicoException {
        log.debug("Eliminando alergia: {}", id);

        try {
            HistorialClinicoDTO resultado = historialClinicoFacade.borrarAlergia(id);
            log.info("Alergia eliminada exitosamente: {}", id);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al eliminar alergia {}: {}", id, e.getMessage(), e);
            throw new HistorialClinicoException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
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
    @PutMapping(RestUrls.HISTORIA_ANALISIS_SANGRE)
    public ResponseEntity<HistorialClinicoDTO> actualizarAnalisisSangre(@RequestBody List<DatoClinicoEntradaDTO> analisis) throws HistorialClinicoException, DatosClinicosValidationException {
        log.debug("Actualizando análisis de sangre del usuario");
        
        try {
            HistorialClinicoDTO resultado = historialClinicoFacade.actualizarAnalisisSangre(analisis);
            log.info("Análisis de sangre actualizados exitosamente");
            return ResponseEntity.ok(resultado);
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al actualizar análisis de sangre: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al actualizar análisis de sangre: {}", e.getMessage(), e);
            throw new HistorialClinicoException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Añade nuevos análisis de sangre sin eliminar los previos del historial clínico.</p>
     */
    @Override
    @PostMapping(RestUrls.HISTORIA_ANALISIS_SANGRE)
    public ResponseEntity<HistorialClinicoDTO> crearAnalisisSangre(@RequestBody List<DatoClinicoEntradaDTO> analisis) throws HistorialClinicoException, DatosClinicosValidationException {
        log.debug("Añadiendo nuevos análisis de sangre del usuario");
        
        try {
            HistorialClinicoDTO resultado = historialClinicoFacade.anadirAnalisisSangre(analisis);
            log.info("Análisis de sangre añadidos exitosamente");
            return ResponseEntity.ok(resultado);
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al añadir análisis de sangre: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al añadir análisis de sangre: {}", e.getMessage(), e);
            throw new HistorialClinicoException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementación que actualiza los signos vitales del paciente
     * con procesamiento de datos JSON y validación de entrada.</p>
     *
     * @param signosVitalesJson Datos de signos vitales en formato JSON
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @Override
    @PutMapping(RestUrls.HISTORIA_SIGNOS_VITALES)
    public ResponseEntity<HistorialClinicoDTO> actualizarSignosVitales(@RequestBody List<DatoClinicoEntradaDTO> signosVitales) throws HistorialClinicoException, DatosClinicosValidationException {
        log.debug("Actualizando signos vitales del usuario");

        try {
            HistorialClinicoDTO resultado = historialClinicoFacade.actualizarSignosVitales(signosVitales);
            log.info("Signos vitales actualizados exitosamente");
            return ResponseEntity.ok(resultado);
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al actualizar signos vitales: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al actualizar signos vitales: {}", e.getMessage(), e);
            throw new HistorialClinicoException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementación que añade nuevos signos vitales sin eliminar los existentes.</p>
     *
     * @param signosVitalesJson Datos de signos vitales en formato JSON
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @Override
    @PostMapping(RestUrls.HISTORIA_SIGNOS_VITALES)
    public ResponseEntity<HistorialClinicoDTO> crearSignosVitales(@RequestBody List<DatoClinicoEntradaDTO> signosVitales) throws HistorialClinicoException, DatosClinicosValidationException {
        log.debug("Añadiendo nuevos signos vitales del usuario");

        try {
            HistorialClinicoDTO resultado = historialClinicoFacade.anadirSignosVitales(signosVitales);
            log.info("Signos vitales añadidos exitosamente");
            return ResponseEntity.ok(resultado);
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al añadir signos vitales: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al añadir signos vitales: {}", e.getMessage(), e);
            throw new HistorialClinicoException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementación que actualiza el análisis de orina del paciente
     * con procesamiento de datos JSON y validación de entrada.</p>
     *
     * @param analisisOrinaJson Datos de análisis de orina en formato JSON
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @Override
    @PutMapping(RestUrls.HISTORIA_ANALISIS_ORINA)
    public ResponseEntity<HistorialClinicoDTO> actualizarAnalisisOrina(@RequestBody List<DatoClinicoEntradaDTO> analisisOrina) throws HistorialClinicoException, DatosClinicosValidationException {
        log.debug("Actualizando análisis de orina del usuario");

        try {
            HistorialClinicoDTO resultado = historialClinicoFacade.actualizarAnalisisOrina(analisisOrina);
            log.info("Análisis de orina actualizado exitosamente");
            return ResponseEntity.ok(resultado);
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al actualizar análisis de orina: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al actualizar análisis de orina: {}", e.getMessage(), e);
            throw new HistorialClinicoException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementación que añade nuevos datos de análisis de orina sin eliminar los existentes.</p>
     *
     * @param analisisOrinaJson Datos de análisis de orina en formato JSON
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @Override
    @PostMapping(RestUrls.HISTORIA_ANALISIS_ORINA)
    public ResponseEntity<HistorialClinicoDTO> crearAnalisisOrina(@RequestBody List<DatoClinicoEntradaDTO> analisisOrina) throws HistorialClinicoException, DatosClinicosValidationException {
        log.debug("Añadiendo nuevos datos de análisis de orina del usuario");

        try {
            HistorialClinicoDTO resultado = historialClinicoFacade.anadirAnalisisOrina(analisisOrina);
            log.info("Análisis de orina añadido exitosamente");
            return ResponseEntity.ok(resultado);
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al añadir análisis de orina: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al añadir análisis de orina: {}", e.getMessage(), e);
            throw new HistorialClinicoException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementación que elimina un dato clínico específico del historial
     * del usuario autenticado.</p>
     * 
     * @param id Identificador único del dato clínico a eliminar
     * @return ResponseEntity con el ID del dato clínico eliminado para confirmación
     */
    @Override
    @DeleteMapping(RestUrls.HISTORIA_DATOS_CLINICOS_ID)
    public ResponseEntity<UUID> borrarDatoClinico(@PathVariable("id") UUID id) throws HistorialClinicoException, DatosClinicosValidationException {
        log.debug("Solicitando eliminación de dato clínico con ID: {}", id);
        
        try {
            historialClinicoFacade.borrarDatoClinico(id);
            log.info("Dato clínico eliminado exitosamente: ID {}", id);
            return ResponseEntity.ok(id);
        } catch (DatosClinicosValidationException e) {
            log.warn("Dato clínico no encontrado para eliminación: ID {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error al eliminar dato clínico con ID {}: {}", id, e.getMessage(), e);
            throw new HistorialClinicoException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
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
    private void validarArchivoSubida(MultipartFile file) throws DatosClinicosValidationException {
        if (file == null || file.isEmpty()) {
            throw new DatosClinicosValidationException(ErrorMessages.campoRequerido("archivo"));
        }
        
        String nombreArchivo = file.getOriginalFilename();
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            throw new DatosClinicosValidationException(ErrorMessages.campoRequerido("nombre de archivo"));
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
            } catch (Exception _) {
                log.warn("No se pudo parsear content-type: {}", contentType);
            }
        }
        return mediaType;
    }
}
