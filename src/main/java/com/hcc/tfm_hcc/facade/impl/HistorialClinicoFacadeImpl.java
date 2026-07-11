package com.hcc.tfm_hcc.facade.impl;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.dto.ArchivoClinicoDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.facade.HistorialClinicoFacade;
import com.hcc.tfm_hcc.mapper.ArchivoClinicoMapper;
import com.hcc.tfm_hcc.model.ArchivoClinico;
import com.hcc.tfm_hcc.exception.ArchivoClinicoException;
import com.hcc.tfm_hcc.exception.DatosClinicosValidationException;
import com.hcc.tfm_hcc.exception.HistorialClinicoException;
import com.hcc.tfm_hcc.service.ArchivoClinicoService;
import com.hcc.tfm_hcc.service.HistorialClinicoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación de la fachada para operaciones de historial clínico.
 * 
 * <p>Esta clase actúa como una capa de fachada entre los controladores y los servicios
 * de historial clínico, proporcionando una interfaz simplificada para las operaciones
 * relacionadas con la gestión de historiales médicos y archivos clínicos.</p>
 * 
 * <p>Funcionalidades principales:</p>
 * <ul>
 *   <li>Gestión de archivos clínicos (subida, descarga, eliminación)</li>
 *   <li>Actualización de datos del historial clínico</li>
 *   <li>Gestión de identificación, antecedentes, alergias y análisis de sangre</li>
 *   <li>Operaciones CRUD sobre datos clínicos</li>
 *   <li>Validación de permisos y acceso a datos</li>
 * </ul>
 * 
 * <p>La clase utiliza inyección de dependencias por constructor y logging estructurado
 * para seguir las mejores prácticas de desarrollo empresarial.</p>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HistorialClinicoFacadeImpl implements HistorialClinicoFacade {

    // ===============================
    // DEPENDENCIAS INYECTADAS
    // ===============================
    
    /**
     * Servicio para operaciones de archivos clínicos.
     */
    private final ArchivoClinicoService archivoClinicoService;
    
    /**
     * Mapper para conversión entre entidades y DTOs de archivo clínico.
     */
    private final ArchivoClinicoMapper archivoClinicoMapper;
    
    /**
     * Servicio para operaciones del historial clínico.
     */
    private final HistorialClinicoService historiaClinicaService;

    // ===============================
    // MÉTODOS DE ARCHIVOS CLÍNICOS
    // ===============================

    /**
     * Lista todos los archivos clínicos del usuario autenticado.
     * 
     * @return List<ArchivoClinicoDTO> Lista de archivos clínicos del usuario
     * @throws RuntimeException Si ocurre un error durante la consulta
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public List<ArchivoClinicoDTO> listarArchivos() {
        log.debug("Obteniendo lista de archivos clínicos del usuario autenticado");
        
        try {
            List<ArchivoClinico> archivos = archivoClinicoService.listMine();
            
            List<ArchivoClinicoDTO> resultado = archivos.stream()
                    .map(archivoClinicoMapper::toDto)
                    .toList();
            
            log.info("Archivos clínicos obtenidos: {} registros", resultado.size());
            return resultado;
        } catch (Exception e) {
            log.error("Error inesperado al obtener archivos clínicos del usuario: {}", e.getMessage(), e);
            throw new ArchivoClinicoException("Error interno durante la consulta de archivos clínicos", e);
        }
    }

    /**
     * Sube un nuevo archivo clínico para el usuario autenticado.
     * 
     * @param file El archivo a subir
     * @return ArchivoClinicoDTO El archivo clínico subido con su información
     * @throws DatosClinicosValidationException Si el archivo es inválido
     * @throws IOException Si ocurre un error durante la subida del archivo
     * @throws ArchivoClinicoException Si ocurre un error inesperado
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public ArchivoClinicoDTO upload(MultipartFile file) throws IOException {
        log.debug("Iniciando subida de archivo clínico: {}", 
                file != null ? file.getOriginalFilename() : "null");
        
        try {
            validarArchivo(file);
            
            ArchivoClinico saved = archivoClinicoService.uploadMine(file);
            ArchivoClinicoDTO resultado = archivoClinicoMapper.toDto(saved);
            
            log.info("Archivo clínico subido exitosamente: {} - ID: {}", 
                    file != null ? file.getOriginalFilename() : "unknown", 
                    resultado != null ? resultado.getId() : "unknown");
            return resultado;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al subir archivo: {} - Error: {}", 
                    file != null ? file.getOriginalFilename() : "null", e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("Error de E/S al subir archivo: {} - Error: {}", 
                     file != null ? file.getOriginalFilename() : "null", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al subir archivo: {} - Error: {}", 
                     file != null ? file.getOriginalFilename() : "null", e.getMessage(), e);
            throw new ArchivoClinicoException("Error interno durante la subida del archivo", e);
        }
    }

    /**
     * Obtiene la información de un archivo clínico específico del usuario autenticado.
     * 
     * @param id ID del archivo clínico
     * @return ArchivoClinicoDTO Los datos del archivo clínico
     * @throws DatosClinicosValidationException Si el ID es inválido
     * @throws ArchivoClinicoException Si el archivo no existe o ocurre un error durante la consulta
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public ArchivoClinicoDTO getArchivoClinico(UUID id) {
        log.debug("Obteniendo archivo clínico: {}", id);
        
        try {
            validarId(id);
            
            ArchivoClinico archivoClinico = archivoClinicoService.listMine().stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new ArchivoClinicoException("No existe el archivo especificado"));
            
            ArchivoClinicoDTO resultado = archivoClinicoMapper.toDto(archivoClinico);
            
            log.info("Archivo clínico obtenido exitosamente: {}", id);
            return resultado;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al obtener archivo: {} - Error: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al obtener archivo: {} - Error: {}", id, e.getMessage(), e);
            throw new ArchivoClinicoException("Error interno durante la consulta del archivo", e);
        }
    }

    /**
     * Obtiene el recurso físico de un archivo clínico del usuario autenticado.
     * 
     * @param id ID del archivo clínico
     * @return Resource El recurso del archivo para descarga
    * @throws DatosClinicosValidationException Si el ID es inválido
    * @throws ArchivoClinicoException Si ocurre un error durante la consulta
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public Resource getMineResource(UUID id) {
        log.debug("Obteniendo recurso de archivo clínico: {}", id);
        
        try {
            validarId(id);
            
            Resource resource = archivoClinicoService.getMineResource(id);
            
            log.info("Recurso de archivo obtenido exitosamente: {}", id);
            return resource;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al obtener recurso de archivo: {} - Error: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al obtener recurso de archivo: {} - Error: {}", id, e.getMessage(), e);
            throw new ArchivoClinicoException("Error interno durante la consulta del recurso", e);
        }
    }

    /**
     * Elimina un archivo clínico del usuario autenticado.
     * 
     * @param id ID del archivo clínico a eliminar
     * @throws DatosClinicosValidationException Si el ID es inválido
     * @throws IOException Si ocurre un error durante la eliminación del archivo
     * @throws ArchivoClinicoException Si ocurre un error inesperado
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public void borrarArchivoClinico(UUID id) throws IOException {
        log.debug("Eliminando archivo clínico: {}", id);
        
        try {
            validarId(id);
            
            archivoClinicoService.borrarArchivo(id);
            
            log.info("Archivo clínico eliminado exitosamente: {}", id);
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al eliminar archivo: {} - Error: {}", id, e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("Error de E/S al eliminar archivo: {} - Error: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al eliminar archivo: {} - Error: {}", id, e.getMessage(), e);
            throw new ArchivoClinicoException("Error interno durante la eliminación del archivo", e);
        }
    }

    // ===============================
    // MÉTODOS DE HISTORIAL CLÍNICO
    // ===============================

    /**
     * Obtiene el historial clínico completo del usuario autenticado.
     * 
     * @return HistorialClinicoDTO Los datos del historial clínico del usuario
    * @throws HistorialClinicoException Si ocurre un error durante la consulta
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO obtenerMiHistoria() {
        log.debug("Obteniendo historial clínico del usuario autenticado");
        
        try {
            HistorialClinicoDTO historial = historiaClinicaService.obtenerHistoriaUsuarioActual();
            
            log.info("Historial clínico obtenido exitosamente para el usuario autenticado");
            return historial;
        } catch (Exception e) {
            log.error("Error inesperado al obtener historial clínico: {}", e.getMessage(), e);
            throw new HistorialClinicoException("Error interno durante la consulta del historial clínico", e);
        }
    }

    /**
     * Actualiza la información de identificación en el historial clínico.
     * 
     * @param historialClinicoDTO Datos de identificación
     * @return HistorialClinicoDTO El historial clínico actualizado
    * @throws DatosClinicosValidationException Si el JSON de identificación es inválido
    * @throws HistorialClinicoException Si ocurre un error durante la actualización
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO actualizarIdentificacion(HistorialClinicoDTO historialClinicoDTO) {
        log.debug("Actualizando identificación en historial clínico");
        
        try {            
            HistorialClinicoDTO resultado = historiaClinicaService.actualizarIdentificacion(historialClinicoDTO);
            
            log.info("Identificación actualizada exitosamente en historial clínico");
            return resultado;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al actualizar identificación: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al actualizar identificación: {}", e.getMessage(), e);
            throw new HistorialClinicoException("Error interno durante la actualización de identificación", e);
        }
    }

    /**
     * Actualiza los antecedentes familiares en el historial clínico.
     * 
     * @param antecedentesFamiliares Datos de antecedentes familiares
     * @return HistorialClinicoDTO El historial clínico actualizado
    * @throws DatosClinicosValidationException Si los datos son inválidos
    * @throws HistorialClinicoException Si ocurre un error durante la actualización
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO actualizarAntecedentes(String antecedentesFamiliares) {
        log.debug("Actualizando antecedentes familiares en historial clínico");
        
        try {
            validarTexto(antecedentesFamiliares, "antecedentes familiares");
            
            HistorialClinicoDTO resultado = historiaClinicaService.actualizarAntecedentes(antecedentesFamiliares);
            
            log.info("Antecedentes familiares actualizados exitosamente en historial clínico");
            return resultado;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al actualizar antecedentes: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al actualizar antecedentes: {}", e.getMessage(), e);
            throw new HistorialClinicoException("Error interno durante la actualización de antecedentes", e);
        }
    }

    /**
     * Actualiza la información de alergias en el historial clínico.
     * 
     * @param alergiasJson JSON con los datos de alergias
     * @return HistorialClinicoDTO El historial clínico actualizado
    * @throws DatosClinicosValidationException Si el JSON de alergias es inválido
    * @throws HistorialClinicoException Si ocurre un error durante la actualización
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO actualizarAlergias(String alergiasJson) {
        log.debug("Actualizando alergias en historial clínico");
        
        try {
            validarJson(alergiasJson, "alergias");
            
            HistorialClinicoDTO resultado = historiaClinicaService.actualizarAlergias(alergiasJson);
            
            log.info("Alergias actualizadas exitosamente en historial clínico");
            return resultado;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al actualizar alergias: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al actualizar alergias: {}", e.getMessage(), e);
            throw new HistorialClinicoException("Error interno durante la actualización de alergias", e);
        }
    }

    /**
     * Añade nuevas alergias sin eliminar las existentes.
     * 
     * @param alergiasJson Texto con las nuevas alergias (una por línea)
     * @return HistorialClinicoDTO El historial clínico actualizado
    * @throws DatosClinicosValidationException Si los datos de alergias son inválidos
    * @throws HistorialClinicoException Si ocurre un error durante la operación
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO anadirAlergias(String alergiasJson) {
        log.debug("Añadiendo nuevas alergias en historial clínico");
        
        try {
            validarJson(alergiasJson, "alergias");
            
            HistorialClinicoDTO resultado = historiaClinicaService.añadirAlergias(alergiasJson);
            
            log.info("Alergias añadidas exitosamente en historial clínico");
            return resultado;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al añadir alergias: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al añadir alergias: {}", e.getMessage(), e);
            throw new HistorialClinicoException("Error interno durante la adición de alergias", e);
        }
    }

    /**
     * Actualiza los análisis de sangre en el historial clínico.
     * 
     * @param analisisJson JSON con los datos de análisis de sangre
     * @return HistorialClinicoDTO El historial clínico actualizado
     * @throws DatosClinicosValidationException Si el JSON de análisis es inválido
     * @throws HistorialClinicoException Si ocurre un error durante la actualización
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO actualizarAnalisisSangre(String analisisJson) {
        log.debug("Actualizando análisis de sangre en historial clínico");
        
        try {
            validarJson(analisisJson, "análisis de sangre");
            
            HistorialClinicoDTO resultado = historiaClinicaService.actualizarAnalisisSangre(analisisJson);
            
            log.info("Análisis de sangre actualizados exitosamente en historial clínico");
            return resultado;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al actualizar análisis de sangre: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al actualizar análisis de sangre: {}", e.getMessage(), e);
            throw new HistorialClinicoException("Error interno durante la actualización de análisis", e);
        }
    }
    
    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO anadirAnalisisSangre(String analisisJson) {
        log.debug("Añadiendo nuevos análisis de sangre al historial clínico");
        
        try {
            validarJson(analisisJson, "análisis de sangre");
            
            HistorialClinicoDTO resultado = historiaClinicaService.añadirAnalisisSangre(analisisJson);
            
            log.info("Análisis de sangre añadidos exitosamente al historial clínico");
            return resultado;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al añadir análisis de sangre: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al añadir análisis de sangre: {}", e.getMessage(), e);
            throw new HistorialClinicoException("Error interno durante la adición de análisis", e);
        }
    }

    // ===============================
    // MÉTODOS DE GESTIÓN DE DATOS
    // ===============================

    /**
     * Borra un dato clínico específico del historial.
     * 
     * @param id ID del dato clínico a borrar
     * @throws DatosClinicosValidationException Si el ID es inválido
     * @throws HistorialClinicoException Si ocurre un error durante la eliminación
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public void borrarDatoClinico(UUID id) {
        log.debug("Borrando dato clínico: {}", id);
        
        try {
            validarId(id);
            
            historiaClinicaService.borrarDatoClinico(id);
            
            log.info("Dato clínico borrado exitosamente: {}", id);
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al borrar dato clínico: {} - Error: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al borrar dato clínico: {} - Error: {}", id, e.getMessage(), e);
            throw new HistorialClinicoException("Error interno durante la eliminación del dato clínico", e);
        }
    }

    /**
     * Borra un antecedente específico por su índice.
     * 
     * @param index Índice del antecedente a borrar
     * @return HistorialClinicoDTO El historial clínico actualizado
     * @throws DatosClinicosValidationException Si el índice es inválido
     * @throws HistorialClinicoException Si ocurre un error durante la eliminación
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO borrarAntecedente(int index) {
        log.debug("Borrando antecedente en índice: {}", index);
        
        try {
            validarIndice(index);
            
            HistorialClinicoDTO resultado = historiaClinicaService.borrarAntecedente(index);
            
            log.info("Antecedente borrado exitosamente en índice: {}", index);
            return resultado;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al borrar antecedente: índice {} - Error: {}", index, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al borrar antecedente: índice {} - Error: {}", index, e.getMessage(), e);
            throw new HistorialClinicoException("Error interno durante la eliminación del antecedente", e);
        }
    }

    /**
     * Edita un antecedente específico por su índice.
     * 
     * @param index Índice del antecedente a editar
     * @param texto Nuevo texto para el antecedente
     * @return HistorialClinicoDTO El historial clínico actualizado
     * @throws DatosClinicosValidationException Si el índice o texto son inválidos
     * @throws HistorialClinicoException Si ocurre un error durante la edición
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public HistorialClinicoDTO editarAntecedente(int index, String texto) {
        log.debug("Editando antecedente en índice: {} con texto: {}", index, 
                texto != null && texto.length() > 50 ? texto.substring(0, 50) + "..." : texto);
        
        try {
            validarIndice(index);
            validarTexto(texto, "texto del antecedente");
            
            HistorialClinicoDTO resultado = historiaClinicaService.editarAntecedente(index, texto);
            
            log.info("Antecedente editado exitosamente en índice: {}", index);
            return resultado;
        } catch (DatosClinicosValidationException e) {
            log.warn("Error de validación al editar antecedente: índice {} - Error: {}", index, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al editar antecedente: índice {} - Error: {}", index, e.getMessage(), e);
            throw new HistorialClinicoException("Error interno durante la edición del antecedente", e);
        }
    }

    // ===============================
    // MÉTODOS UTILITARIOS PRIVADOS
    // ===============================

    /**
     * Valida un archivo para subida.
     * 
     * @param file Archivo a validar
     * @throws DatosClinicosValidationException Si el archivo es inválido
     */
    private void validarArchivo(MultipartFile file) {
        if (file == null) {
            throw new DatosClinicosValidationException("El archivo no puede ser nulo");
        }
        
        if (file.isEmpty()) {
            throw new DatosClinicosValidationException("El archivo no puede estar vacío");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new DatosClinicosValidationException("El archivo debe tener un nombre válido");
        }
        
        // Validar tamaño máximo (por ejemplo, 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new DatosClinicosValidationException("El archivo no puede superar los 10MB");
        }
    }

    /**
     * Valida un ID UUID.
     * 
     * @param id ID a validar
     * @throws DatosClinicosValidationException Si el ID es inválido
     */
    private void validarId(UUID id) {
        if (id == null) {
            throw new DatosClinicosValidationException("El ID no puede ser nulo");
        }
    }

    /**
     * Valida un JSON y su tipo.
     * 
     * @param json JSON a validar
     * @param tipo Tipo de datos que representa el JSON
     * @throws DatosClinicosValidationException Si el JSON es inválido
     */
    private void validarJson(String json, String tipo) {
        if (json == null || json.trim().isEmpty()) {
            throw new DatosClinicosValidationException("Los datos de " + tipo + " no pueden ser nulos o vacíos");
        }
        
        // Validación básica de formato JSON
        String trimmed = json.trim();
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            throw new DatosClinicosValidationException("Los datos de " + tipo + " deben tener formato JSON válido");
        }
    }

    /**
     * Valida un texto y su tipo.
     * 
     * @param texto Texto a validar
     * @param tipo Tipo de datos que representa el texto
     * @throws DatosClinicosValidationException Si el texto es inválido
     */
    private void validarTexto(String texto, String tipo) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new DatosClinicosValidationException("El " + tipo + " no puede ser nulo o vacío");
        }
        
        if (texto.length() > 5000) {
            throw new DatosClinicosValidationException("El " + tipo + " no puede superar los 5000 caracteres");
        }
    }

    /**
     * Valida un índice para operaciones de array.
     * 
     * @param index Índice a validar
     * @throws DatosClinicosValidationException Si el índice es inválido
     */
    private void validarIndice(int index) {
        if (index < 0) {
            throw new DatosClinicosValidationException("El índice no puede ser negativo");
        }
    }
}
