package com.hcc.tfm_hcc.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.dto.AlergiaDTO;
import com.hcc.tfm_hcc.dto.AntecedenteClinicoDTO;
import com.hcc.tfm_hcc.dto.DatoClinicoEntradaDTO;
import com.hcc.tfm_hcc.dto.ArchivoClinicoDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;

/**
 * Controlador REST para la gestión integral de historiales clínicos.
 * Proporciona endpoints para la gestión de archivos clínicos y datos
 * del historial médico de los pacientes autenticados.
 * 
 * <p>Funcionalidades principales:</p>
 * <ul>
 *   <li>Gestión completa de archivos clínicos (subida, descarga, eliminación)</li>
 *   <li>Manipulación de datos del historial clínico</li>
 *   <li>Actualización de información médica específica</li>
 *   <li>Gestión de antecedentes, alergias y análisis de sangre</li>
 * </ul>
 * 
 * <p>Todos los endpoints requieren autenticación de usuario.</p>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface HistorialClinicoController {
    
    /**
     * Lista todos los archivos clínicos del usuario autenticado.
     * 
     * @return Lista de ArchivoClinicoDTO con los archivos del usuario actual
     */
    List<ArchivoClinicoDTO> listarArchivos();
    
    /**
     * Sube un nuevo archivo clínico para el usuario autenticado.
     * 
     * @param file Archivo multipart a subir al sistema
     * @return ResponseEntity con el ArchivoClinicoDTO del archivo subido
     * @throws IOException si hay error en el procesamiento del archivo
     */
    ResponseEntity<ArchivoClinicoDTO> subirArchivo(@RequestParam("file") MultipartFile file) throws IOException;
    
    /**
     * Descarga un archivo clínico específico del usuario autenticado.
     * 
     * @param id ID único del archivo clínico a descargar
     * @return ResponseEntity con el Resource del archivo para descarga
     */
    ResponseEntity<Resource> descargarArchivo(@PathVariable("id") UUID id);
    
    /**
     * Elimina un archivo clínico del usuario autenticado.
     * 
     * @param id ID único del archivo clínico a eliminar
     * @return ResponseEntity con el ID del archivo eliminado
     * @throws IOException si hay error al eliminar el archivo físico
     */
    ResponseEntity<UUID> eliminarArchivo(@PathVariable("id") UUID id) throws IOException;
    
    /**
     * Obtiene el historial clínico completo del usuario autenticado.
     * 
     * @return ResponseEntity con el HistorialClinicoDTO del usuario actual
     */
    ResponseEntity<HistorialClinicoDTO> getMiHistoria();
    
    /**
     * Crea un nuevo antecedente clínico (personal o familiar) en el historial.
     *
     * @param antecedenteDTO Categoría y descripción del antecedente
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    ResponseEntity<HistorialClinicoDTO> crearAntecedente(@RequestBody AntecedenteClinicoDTO antecedenteDTO);

    /**
     * Edita un antecedente clínico existente.
     *
     * @param id ID del antecedente a editar
     * @param antecedenteDTO Categoría y descripción actualizadas
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    ResponseEntity<HistorialClinicoDTO> editarAntecedente(@PathVariable("id") UUID id, @RequestBody AntecedenteClinicoDTO antecedenteDTO);

    /**
     * Elimina un antecedente clínico específico.
     *
     * @param id ID del antecedente a eliminar
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    ResponseEntity<HistorialClinicoDTO> borrarAntecedente(@PathVariable("id") UUID id);

    /**
     * Crea una nueva alergia o intolerancia en el historial clínico.
     *
     * @param alergiaDTO Descripción de la alergia
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    ResponseEntity<HistorialClinicoDTO> crearAlergia(@RequestBody AlergiaDTO alergiaDTO);

    /**
     * Elimina una alergia específica.
     *
     * @param id ID de la alergia a eliminar
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    ResponseEntity<HistorialClinicoDTO> borrarAlergia(@PathVariable("id") UUID id);

    /**
     * Actualiza los análisis de sangre en el historial clínico.
     * 
     * @param analisis Mediciones de análisis de sangre
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    ResponseEntity<HistorialClinicoDTO> actualizarAnalisisSangre(@RequestBody List<DatoClinicoEntradaDTO> analisis);
    
    /**
     * Crea nuevos análisis de sangre en el historial clínico.
     * 
     * <p>Este endpoint permite agregar nuevos datos de análisis de sangre
     * al historial clínico del usuario autenticado. Los datos se procesan
     * y se asignan automáticamente los rangos de referencia correspondientes
     * cuando están disponibles en el sistema.</p>
     * 
     * @param analisis Mediciones de análisis de sangre
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    ResponseEntity<HistorialClinicoDTO> crearAnalisisSangre(@RequestBody List<DatoClinicoEntradaDTO> analisis);

    /**
     * Actualiza los signos vitales en el historial clínico.
     *
     * @param signosVitales Mediciones de signos vitales
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    ResponseEntity<HistorialClinicoDTO> actualizarSignosVitales(@RequestBody List<DatoClinicoEntradaDTO> signosVitales);

    /**
     * Crea nuevos signos vitales en el historial clínico sin eliminar los existentes.
     *
     * @param signosVitales Mediciones de signos vitales
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    ResponseEntity<HistorialClinicoDTO> crearSignosVitales(@RequestBody List<DatoClinicoEntradaDTO> signosVitales);

    /**
     * Actualiza el análisis de orina en el historial clínico.
     *
     * @param analisisOrina Mediciones de análisis de orina
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    ResponseEntity<HistorialClinicoDTO> actualizarAnalisisOrina(@RequestBody List<DatoClinicoEntradaDTO> analisisOrina);

    /**
     * Crea nuevos datos de análisis de orina en el historial clínico sin eliminar los existentes.
     *
     * @param analisisOrina Mediciones de análisis de orina
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    ResponseEntity<HistorialClinicoDTO> crearAnalisisOrina(@RequestBody List<DatoClinicoEntradaDTO> analisisOrina);

    /**
     * Elimina un dato clínico específico del historial.
     * 
     * @param id ID único del dato clínico a eliminar
     * @return ResponseEntity con el ID del dato clínico eliminado para confirmación
     */
    ResponseEntity<UUID> borrarDatoClinico(@PathVariable("id") UUID id);
}
