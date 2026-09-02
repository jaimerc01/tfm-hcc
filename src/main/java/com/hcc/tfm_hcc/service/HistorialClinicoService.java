package com.hcc.tfm_hcc.service;

import java.util.List;
import java.util.UUID;

import com.hcc.tfm_hcc.dto.AlergiaDTO;
import com.hcc.tfm_hcc.dto.AntecedenteClinicoDTO;
import com.hcc.tfm_hcc.dto.DatoClinicoEntradaDTO;
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
     * Obtiene el historial clínico de un paciente vinculado al médico autenticado.
     *
     * <p>El médico autenticado se obtiene siempre del contexto de seguridad, nunca de
     * un parámetro. Solo se permite el acceso si existe una relación {@code MedicoPaciente}
     * con estado "ACTIVA" entre ese médico y el paciente solicitado.</p>
     *
     * @param nifPaciente NIF del paciente cuyo historial se consulta
     * @return el DTO del historial clínico del paciente, o {@code null} si el paciente no tiene historial creado aún
     * @throws IllegalArgumentException si el NIF es inválido o el paciente no existe
     * @throws IllegalStateException si el médico autenticado no tiene una relación activa con el paciente
     */
    HistorialClinicoDTO obtenerHistorialPaciente(String nifPaciente);

    /**
     * Crea un nuevo antecedente clínico (personal o familiar) en el historial.
     *
     * @param antecedenteDTO categoría y descripción del antecedente
     * @return el DTO del historial clínico actualizado
     * @throws IllegalArgumentException si los datos son inválidos
     */
    HistorialClinicoDTO crearAntecedente(AntecedenteClinicoDTO antecedenteDTO);

    /**
     * Edita un antecedente clínico existente.
     *
     * @param id el ID del antecedente a editar
     * @param antecedenteDTO categoría y descripción actualizadas
     * @return el DTO del historial clínico actualizado
     * @throws IllegalArgumentException si el antecedente no existe o no pertenece al usuario
     */
    HistorialClinicoDTO editarAntecedente(UUID id, AntecedenteClinicoDTO antecedenteDTO);

    /**
     * Borra un antecedente clínico específico.
     *
     * @param id el ID del antecedente a borrar
     * @return el DTO del historial clínico actualizado
     * @throws IllegalArgumentException si el antecedente no existe o no pertenece al usuario
     */
    HistorialClinicoDTO borrarAntecedente(UUID id);

    /**
     * Crea una nueva alergia o intolerancia en el historial.
     *
     * @param alergiaDTO descripción de la alergia
     * @return el DTO del historial clínico actualizado
     * @throws IllegalArgumentException si los datos son inválidos
     */
    HistorialClinicoDTO crearAlergia(AlergiaDTO alergiaDTO);

    /**
     * Borra una alergia específica.
     *
     * @param id el ID de la alergia a borrar
     * @return el DTO del historial clínico actualizado
     * @throws IllegalArgumentException si la alergia no existe o no pertenece al usuario
     */
    HistorialClinicoDTO borrarAlergia(UUID id);

    /**
     * Actualiza los análisis de sangre del historial clínico.
     * Reemplaza todos los análisis existentes con los nuevos.
     *
     * @param analisis las mediciones de análisis de sangre
     * @return el DTO del historial clínico actualizado
     * @throws IllegalArgumentException si alguna medición es inválida
     */
    HistorialClinicoDTO actualizarAnalisisSangre(List<DatoClinicoEntradaDTO> analisis);

    /**
     * Añade nuevos análisis de sangre al historial clínico sin eliminar los existentes.
     *
     * @param analisis las nuevas mediciones de análisis de sangre
     * @return el DTO del historial clínico actualizado
     * @throws IllegalArgumentException si alguna medición es inválida
     */
    HistorialClinicoDTO añadirAnalisisSangre(List<DatoClinicoEntradaDTO> analisis);

    /**
     * Actualiza los signos vitales del historial clínico.
     * Reemplaza todos los signos vitales existentes con los nuevos.
     *
     * @param signosVitales las mediciones de signos vitales
     * @return el DTO del historial clínico actualizado
     * @throws IllegalArgumentException si alguna medición es inválida
     */
    HistorialClinicoDTO actualizarSignosVitales(List<DatoClinicoEntradaDTO> signosVitales);

    /**
     * Añade nuevos signos vitales al historial clínico sin eliminar los existentes.
     *
     * @param signosVitales las nuevas mediciones de signos vitales
     * @return el DTO del historial clínico actualizado
     * @throws IllegalArgumentException si alguna medición es inválida
     */
    HistorialClinicoDTO añadirSignosVitales(List<DatoClinicoEntradaDTO> signosVitales);

    /**
     * Actualiza el análisis de orina del historial clínico.
     * Reemplaza todos los datos de orina existentes con los nuevos.
     *
     * @param analisisOrina las mediciones de análisis de orina
     * @return el DTO del historial clínico actualizado
     * @throws IllegalArgumentException si alguna medición es inválida
     */
    HistorialClinicoDTO actualizarAnalisisOrina(List<DatoClinicoEntradaDTO> analisisOrina);

    /**
     * Añade nuevos datos de análisis de orina al historial clínico sin eliminar los existentes.
     *
     * @param analisisOrina las nuevas mediciones de análisis de orina
     * @return el DTO del historial clínico actualizado
     * @throws IllegalArgumentException si alguna medición es inválida
     */
    HistorialClinicoDTO añadirAnalisisOrina(List<DatoClinicoEntradaDTO> analisisOrina);

    /**
     * Borra un dato clínico específico del historial.
     *
     * @param id el ID del dato clínico a borrar
     * @throws IllegalArgumentException si el ID no existe o no pertenece al usuario
     */
    void borrarDatoClinico(UUID id);
}
