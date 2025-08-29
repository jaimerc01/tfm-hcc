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
    @GetMapping("/files")
    List<ArchivoClinicoDTO> listMine();
    
    /**
     * Sube un nuevo archivo clínico para el usuario autenticado.
     * 
     * @param file Archivo multipart a subir al sistema
     * @return ResponseEntity con el ArchivoClinicoDTO del archivo subido
     * @throws IOException si hay error en el procesamiento del archivo
     */
    @PostMapping("/files")
    ResponseEntity<ArchivoClinicoDTO> upload(@RequestParam("file") MultipartFile file) throws IOException;
    
    /**
     * Descarga un archivo clínico específico del usuario autenticado.
     * 
     * @param id ID único del archivo clínico a descargar
     * @return ResponseEntity con el Resource del archivo para descarga
     */
    @GetMapping("/files/{id}")
    ResponseEntity<Resource> download(@PathVariable("id") UUID id);
    
    /**
     * Elimina un archivo clínico del usuario autenticado.
     * 
     * @param id ID único del archivo clínico a eliminar
     * @return ResponseEntity vacío confirmando la eliminación
     * @throws IOException si hay error al eliminar el archivo físico
     */
    @DeleteMapping("/files/{id}")
    ResponseEntity<Void> delete(@PathVariable("id") UUID id) throws IOException;
    
    /**
     * Obtiene el historial clínico completo del usuario autenticado.
     * 
     * @return ResponseEntity con el HistorialClinicoDTO del usuario actual
     */
    @GetMapping
    ResponseEntity<HistorialClinicoDTO> getMiHistoria();
    
    /**
     * Actualiza la información de identificación en el historial clínico.
     * 
     * @param identificacionJson Datos de identificación en formato JSON
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @PutMapping("/identificacion")
    ResponseEntity<HistorialClinicoDTO> actualizarIdentificacion(@RequestBody String identificacionJson);
    
    /**
     * Actualiza los antecedentes familiares en el historial clínico.
     * 
     * @param antecedentesFamiliares Texto con los antecedentes familiares
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @PutMapping("/antecedentes")
    ResponseEntity<HistorialClinicoDTO> actualizarAntecedentes(@RequestBody String antecedentesFamiliares);
    
    /**
     * Actualiza la información de alergias en el historial clínico.
     * 
     * @param alergiasJson Datos de alergias en formato JSON
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @PutMapping("/alergias")
    ResponseEntity<HistorialClinicoDTO> actualizarAlergias(@RequestBody String alergiasJson);
    
    /**
     * Actualiza los análisis de sangre en el historial clínico.
     * 
     * @param analisisJson Datos de análisis de sangre en formato JSON
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @PutMapping("/analisis-sangre")
    ResponseEntity<HistorialClinicoDTO> actualizarAnalisisSangre(@RequestBody String analisisJson);
    
    /**
     * Elimina un dato clínico específico del historial.
     * 
     * @param id ID único del dato clínico a eliminar
     * @return ResponseEntity vacío confirmando la eliminación
     */
    @DeleteMapping("/datos-clinicos/{id}")
    ResponseEntity<Void> borrarDatoClinico(@PathVariable("id") UUID id);
    
    /**
     * Elimina un antecedente específico por su índice.
     * 
     * @param index Índice del antecedente a eliminar
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @DeleteMapping("/antecedentes/{index}")
    ResponseEntity<HistorialClinicoDTO> borrarAntecedente(@PathVariable("index") int index);
    
    /**
     * Edita un antecedente específico por su índice.
     * 
     * @param index Índice del antecedente a editar
     * @param texto Nuevo texto para el antecedente
     * @return ResponseEntity con el HistorialClinicoDTO actualizado
     */
    @PutMapping("/antecedentes/{index}")
    ResponseEntity<HistorialClinicoDTO> editarAntecedente(@PathVariable("index") int index, @RequestBody String texto);
}
