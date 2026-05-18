package com.hcc.tfm_hcc.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.constants.RestUrls;
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
     * @return ResponseEntity vacío confirmando la eliminación
     * @throws IOException si hay error al eliminar el archivo físico
     */
    ResponseEntity<Void> eliminarArchivo(@PathVariable("id") UUID id) throws IOException;
    
    /**
     * Obtiene el historial clínico completo del usuario autenticado.
     * 
     * @return ResponseEntity con el HistorialClinicoDTO del usuario actual
     */
    ResponseEntity<HistorialClinicoDTO> getMiHistoria();
    
    /**
     * Actualiza la información de identificación en el historial clínico.
     * 
     * @param identificacionJson Datos de identificación en formato JSON
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    ResponseEntity<HistorialClinicoDTO> actualizarIdentificacion(@RequestBody String identificacionJson);
    
    /**
     * Actualiza los antecedentes familiares en el historial clínico.
     * 
     * @param antecedentesFamiliares Texto con los antecedentes familiares
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    ResponseEntity<HistorialClinicoDTO> actualizarAntecedentes(@RequestBody String antecedentesFamiliares);
    
    /**
     * Actualiza la información de alergias en el historial clínico.
     * 
     * @param alergiasJson Datos de alergias en formato JSON
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    ResponseEntity<HistorialClinicoDTO> actualizarAlergias(@RequestBody String alergiasJson);
    
    /**
     * Actualiza los análisis de sangre en el historial clínico.
     * 
     * @param analisisJson Datos de análisis de sangre en formato JSON
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    ResponseEntity<HistorialClinicoDTO> actualizarAnalisisSangre(@RequestBody String analisisJson);
    
    /**
     * Crea nuevos análisis de sangre en el historial clínico.
     * 
     * <p>Este endpoint permite agregar nuevos datos de análisis de sangre
     * al historial clínico del usuario autenticado. Los datos se procesan
     * y se asignan automáticamente los rangos de referencia correspondientes
     * cuando están disponibles en el sistema.</p>
     * 
     * @param analisisJson Datos de análisis de sangre en formato JSON
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    ResponseEntity<HistorialClinicoDTO> crearAnalisisSangre(@RequestBody String analisisJson);
    
    /**
     * Elimina un dato clínico específico del historial.
     * 
     * @param id ID único del dato clínico a eliminar
     * @return ResponseEntity con el ID del dato clínico eliminado para confirmación
     */
    ResponseEntity<UUID> borrarDatoClinico(@PathVariable("id") UUID id);
    

    //TODO: revisar por qué se utiliza el índice para editar y eliminar antecedentes, en lugar de un ID único como el resto de datos clínicos. Posible refactorización a futuro para unificar criterios de identificación.
    /**
     * Elimina un antecedente específico por su índice.
     * 
     * @param index Índice del antecedente a eliminar
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    ResponseEntity<HistorialClinicoDTO> borrarAntecedente(@PathVariable("index") int index);
    
    /**
     * Edita un antecedente específico por su índice.
     * 
     * @param index Índice del antecedente a editar
     * @param texto Nuevo texto para el antecedente
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    ResponseEntity<HistorialClinicoDTO> editarAntecedente(@PathVariable("index") int index, @RequestBody String texto);
}
