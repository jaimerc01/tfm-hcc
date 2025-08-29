package com.hcc.tfm_hcc.facade;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.dto.ArchivoClinicoDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;

/**
 * Facade para la gestión integral de historiales clínicos en el sistema HCC.
 * Proporciona una interfaz unificada que combina la gestión de archivos clínicos
 * y los datos del historial médico de los pacientes.
 * 
 * <p>Este facade encapsula:</p>
 * <ul>
 *   <li>Gestión completa de archivos clínicos (subida, descarga, eliminación)</li>
 *   <li>Manipulación de datos del historial clínico (identificación, antecedentes, alergias)</li>
 *   <li>Gestión de análisis de sangre y datos clínicos específicos</li>
 *   <li>Operaciones de edición y eliminación de registros médicos</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface HistorialClinicoFacade {
    
    /**
     * Lista todos los archivos clínicos del usuario autenticado.
     * 
     * @return Lista de ArchivoClinicoDTO con los archivos del usuario actual
     */
    List<ArchivoClinicoDTO> listMine();
    
    /**
     * Sube un nuevo archivo clínico para el usuario autenticado.
     * 
     * @param file Archivo multipart a subir al sistema
     * @return ArchivoClinicoDTO con la información del archivo subido
     * @throws IOException si hay error en el procesamiento o almacenamiento del archivo
     */
    ArchivoClinicoDTO upload(MultipartFile file) throws IOException;
    
    /**
     * Obtiene la información de un archivo clínico específico del usuario autenticado.
     * 
     * @param id ID único del archivo clínico a obtener
     * @return ArchivoClinicoDTO con la información del archivo
     */
    ArchivoClinicoDTO getMine(UUID id);
    
    /**
     * Obtiene el recurso físico de un archivo clínico para descarga.
     * 
     * @param id ID único del archivo clínico a descargar
     * @return Resource que representa el archivo físico
     */
    Resource getMineResource(UUID id);
    
    /**
     * Elimina un archivo clínico del usuario autenticado.
     * 
     * @param id ID único del archivo clínico a eliminar
     * @throws IOException si hay error al eliminar el archivo físico
     */
    void delete(UUID id) throws IOException;
    
    /**
     * Obtiene el historial clínico completo del usuario autenticado.
     * 
     * @return HistorialClinicoDTO con todos los datos médicos del usuario
     */
    HistorialClinicoDTO obtenerMiHistoria();
    
    /**
     * Actualiza la información de identificación en el historial clínico.
     * 
     * @param identificacionJson Datos de identificación en formato JSON
     * @return HistorialClinicoDTO actualizado
     */
    HistorialClinicoDTO actualizarIdentificacion(String identificacionJson);
    
    /**
     * Actualiza los antecedentes familiares en el historial clínico.
     * 
     * @param antecedentesFamiliares Texto con los antecedentes familiares del paciente
     * @return HistorialClinicoDTO actualizado
     */
    HistorialClinicoDTO actualizarAntecedentes(String antecedentesFamiliares);
    
    /**
     * Actualiza la información de alergias en el historial clínico.
     * 
     * @param alergiasJson Datos de alergias en formato JSON
     * @return HistorialClinicoDTO actualizado
     */
    HistorialClinicoDTO actualizarAlergias(String alergiasJson);
    
    /**
     * Actualiza los análisis de sangre en el historial clínico.
     * 
     * @param analisisJson Datos de análisis de sangre en formato JSON
     * @return HistorialClinicoDTO actualizado
     */
    HistorialClinicoDTO actualizarAnalisisSangre(String analisisJson);
    
    /**
     * Elimina un dato clínico específico del historial.
     * 
     * @param id ID único del dato clínico a eliminar
     */
    void borrarDatoClinico(UUID id);
    
    /**
     * Elimina un antecedente específico por su índice en la lista.
     * 
     * @param index Índice del antecedente a eliminar (basado en cero)
     * @return HistorialClinicoDTO actualizado
     */
    HistorialClinicoDTO borrarAntecedente(int index);
    
    /**
     * Edita un antecedente específico por su índice en la lista.
     * 
     * @param index Índice del antecedente a editar (basado en cero)
     * @param texto Nuevo texto para el antecedente
     * @return HistorialClinicoDTO actualizado
     */
    HistorialClinicoDTO editarAntecedente(int index, String texto);
}
