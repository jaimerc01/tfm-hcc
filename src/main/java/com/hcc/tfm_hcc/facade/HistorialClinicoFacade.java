package com.hcc.tfm_hcc.facade;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.dto.AlergiaDTO;
import com.hcc.tfm_hcc.dto.AntecedenteClinicoDTO;
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
    List<ArchivoClinicoDTO> listarArchivos();
    
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
    ArchivoClinicoDTO getArchivoClinico(UUID id);
    
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
    void borrarArchivoClinico(UUID id) throws IOException;
    
    /**
     * Obtiene el historial clínico completo del usuario autenticado.
     * 
     * @return HistorialClinicoDTO con todos los datos médicos del usuario
     */
    HistorialClinicoDTO obtenerMiHistoria();
    
    /**
     * Crea un nuevo antecedente clínico (personal o familiar) en el historial.
     *
     * @param antecedenteDTO categoría y descripción del antecedente
     * @return HistorialClinicoDTO actualizado
     */
    HistorialClinicoDTO crearAntecedente(AntecedenteClinicoDTO antecedenteDTO);

    /**
     * Edita un antecedente clínico existente.
     *
     * @param id ID del antecedente a editar
     * @param antecedenteDTO categoría y descripción actualizadas
     * @return HistorialClinicoDTO actualizado
     */
    HistorialClinicoDTO editarAntecedente(UUID id, AntecedenteClinicoDTO antecedenteDTO);

    /**
     * Elimina un antecedente clínico específico.
     *
     * @param id ID del antecedente a eliminar
     * @return HistorialClinicoDTO actualizado
     */
    HistorialClinicoDTO borrarAntecedente(UUID id);

    /**
     * Crea una nueva alergia o intolerancia en el historial clínico.
     *
     * @param alergiaDTO descripción de la alergia
     * @return HistorialClinicoDTO actualizado
     */
    HistorialClinicoDTO crearAlergia(AlergiaDTO alergiaDTO);

    /**
     * Elimina una alergia específica.
     *
     * @param id ID de la alergia a eliminar
     * @return HistorialClinicoDTO actualizado
     */
    HistorialClinicoDTO borrarAlergia(UUID id);

    /**
     * Actualiza los análisis de sangre en el historial clínico.
     * 
     * @param analisisJson Datos de análisis de sangre en formato JSON
     * @return HistorialClinicoDTO actualizado
     */
    HistorialClinicoDTO actualizarAnalisisSangre(String analisisJson);
    
    /**
     * Añade nuevos análisis de sangre sin eliminar los existentes.
     * 
     * @param analisisJson Datos de análisis de sangre en formato JSON
     * @return HistorialClinicoDTO actualizado
     */
    HistorialClinicoDTO anadirAnalisisSangre(String analisisJson);

    /**
     * Actualiza los signos vitales en el historial clínico.
     *
     * @param signosVitalesJson Datos de signos vitales en formato JSON
     * @return HistorialClinicoDTO actualizado
     */
    HistorialClinicoDTO actualizarSignosVitales(String signosVitalesJson);

    /**
     * Añade nuevos signos vitales sin eliminar los existentes.
     *
     * @param signosVitalesJson Datos de signos vitales en formato JSON
     * @return HistorialClinicoDTO actualizado
     */
    HistorialClinicoDTO anadirSignosVitales(String signosVitalesJson);

    /**
     * Actualiza el análisis de orina en el historial clínico.
     *
     * @param analisisOrinaJson Datos de análisis de orina en formato JSON
     * @return HistorialClinicoDTO actualizado
     */
    HistorialClinicoDTO actualizarAnalisisOrina(String analisisOrinaJson);

    /**
     * Añade nuevos datos de análisis de orina sin eliminar los existentes.
     *
     * @param analisisOrinaJson Datos de análisis de orina en formato JSON
     * @return HistorialClinicoDTO actualizado
     */
    HistorialClinicoDTO anadirAnalisisOrina(String analisisOrinaJson);

    /**
     * Elimina un dato clínico específico del historial.
     * 
     * @param id ID único del dato clínico a eliminar
     */
    void borrarDatoClinico(UUID id);
}
