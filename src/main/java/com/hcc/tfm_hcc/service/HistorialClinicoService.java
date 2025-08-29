package com.hcc.tfm_hcc.service;

import java.util.UUID;

import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;

/**
 * Servicio para la gestión del historial clínico de usuarios.
 * 
 * <p>Este servicio proporciona operaciones para la gestión completa del historial
 * clínico de los usuarios, incluyendo datos de identificación, antecedentes familiares,
 * alergias y análisis de sangre.</p>
 * 
 * <p>Características principales:</p>
 * <ul>
 *   <li>Gestión completa del historial clínico personal</li>
 *   <li>Actualización segura de datos médicos</li>
 *   <li>Control de acceso basado en el usuario autenticado</li>
 *   <li>Operaciones CRUD sobre antecedentes familiares</li>
 *   <li>Gestión de alergias y análisis de sangre en formato JSON</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 2024
 */
public interface HistorialClinicoService {
    
    /**
     * Obtiene el historial clínico completo del usuario autenticado actualmente.
     *
     * @return el DTO del historial clínico del usuario actual
     * @throws IllegalStateException si no hay usuario autenticado
     */
    HistorialClinicoDTO obtenerHistoriaUsuarioActual();
    
    /**
     * Actualiza los datos de identificación del historial clínico.
     *
     * @param identificacionJson los datos de identificación en formato JSON
     * @return el DTO del historial clínico actualizado
     * @throws IllegalArgumentException si el JSON es inválido
     */
    HistorialClinicoDTO actualizarIdentificacion(String identificacionJson);
    
    /**
     * Actualiza los antecedentes familiares del historial clínico.
     *
     * @param antecedentesFamiliares los antecedentes familiares en formato texto
     * @return el DTO del historial clínico actualizado
     * @throws IllegalArgumentException si los datos son inválidos
     */
    HistorialClinicoDTO actualizarAntecedentes(String antecedentesFamiliares);
    
    /**
     * Actualiza las alergias del historial clínico.
     *
     * @param alergiasJson las alergias en formato JSON
     * @return el DTO del historial clínico actualizado
     * @throws IllegalArgumentException si el JSON es inválido
     */
    HistorialClinicoDTO actualizarAlergias(String alergiasJson);
    
    /**
     * Actualiza los análisis de sangre del historial clínico.
     *
     * @param analisisJson los análisis de sangre en formato JSON
     * @return el DTO del historial clínico actualizado
     * @throws IllegalArgumentException si el JSON es inválido
     */
    HistorialClinicoDTO actualizarAnalisisSangre(String analisisJson);
    
    /**
     * Borra un dato clínico específico del historial.
     *
     * @param id el ID del dato clínico a borrar
     * @throws IllegalArgumentException si el ID no existe o no pertenece al usuario
     */
    void borrarDatoClinico(UUID id);
    
    /**
     * Borra un antecedente familiar específico por su índice.
     *
     * @param index el índice del antecedente a borrar
     * @return el DTO del historial clínico actualizado
     * @throws IllegalArgumentException si el índice es inválido
     */
    HistorialClinicoDTO borrarAntecedente(int index);
    
    /**
     * Edita un antecedente familiar específico por su índice.
     *
     * @param index el índice del antecedente a editar
     * @param texto el nuevo texto del antecedente
     * @return el DTO del historial clínico actualizado
     * @throws IllegalArgumentException si el índice es inválido o el texto es nulo
     */
    HistorialClinicoDTO editarAntecedente(int index, String texto);
}
