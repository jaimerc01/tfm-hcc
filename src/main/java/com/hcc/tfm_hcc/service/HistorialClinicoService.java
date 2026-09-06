package com.hcc.tfm_hcc.service;

import java.util.List;
import java.util.UUID;

import com.hcc.tfm_hcc.dto.AlergiaDTO;
import com.hcc.tfm_hcc.dto.AntecedenteClinicoDTO;
import com.hcc.tfm_hcc.dto.DatoClinicoEntradaDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.model.HistorialClinico;
import com.hcc.tfm_hcc.model.PropuestaCambioClinico;

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
     * Edita un dato clínico cuantitativo ya guardado del historial del usuario autenticado
     * (parámetro, valor, unidad y fecha), reasignando el rango de referencia. Es la forma de
     * corregir un valor introducido por error, sin tener que borrarlo y volver a crearlo.
     *
     * @param id el ID del dato clínico a editar
     * @param datos los nuevos valores de la medición
     * @return el DTO del historial clínico actualizado
     * @throws IllegalArgumentException si el ID no existe, no pertenece al usuario o el valor no es numérico
     */
    HistorialClinicoDTO editarDatoClinico(UUID id, DatoClinicoEntradaDTO datos);

    /**
     * Borra un dato clínico específico del historial.
     *
     * @param id el ID del dato clínico a borrar
     * @throws IllegalArgumentException si el ID no existe o no pertenece al usuario
     */
    void borrarDatoClinico(UUID id);

    // ===============================
    // APLICACIÓN DE PROPUESTAS DE CAMBIO (uso interno del flujo médico → confirmación del paciente)
    // ===============================

    /**
     * Devuelve una descripción legible del recurso del historial de un paciente sobre el que
     * actuaría una propuesta de edición o borrado, validando de paso que dicho recurso existe y
     * pertenece a ese paciente. Se usa para dejar constancia del "valor actual" en la propuesta.
     *
     * @param pacienteId ID del paciente propietario del historial
     * @param dominio apartado del historial ({@code ANTECEDENTE}, {@code ALERGIA}, análisis...)
     * @param recursoId ID del antecedente / alergia / dato clínico objetivo
     * @return descripción legible del valor actual del recurso
     * @throws IllegalArgumentException si el recurso no existe o no pertenece al paciente
     */
    String describirRecursoHistorial(UUID pacienteId, PropuestaCambioClinico.Dominio dominio, UUID recursoId);

    /**
     * Devuelve el historial clínico de un paciente, creándolo si aún no existe. Se usa al
     * registrar una propuesta de alta de un médico, para poder vincularla al historial.
     *
     * @param pacienteId ID del paciente
     * @return el historial clínico del paciente (existente o recién creado)
     */
    HistorialClinico asegurarHistorial(UUID pacienteId);

    /**
     * Aplica sobre el historial de un paciente un cambio en un antecedente propuesto por un
     * médico y ya aceptado por el paciente. La auditoría queda registrada con el médico como
     * autor y el motivo indicado por él.
     *
     * @param pacienteId ID del paciente
     * @param medicoId ID del médico autor de la propuesta
     * @param operacion CREATE, UPDATE o DELETE
     * @param recursoId ID del antecedente objetivo (para UPDATE y DELETE)
     * @param datos datos del antecedente propuesto (para CREATE y UPDATE)
     * @param motivo motivo del cambio indicado por el médico
     * @return el DTO del historial clínico actualizado
     */
    HistorialClinicoDTO aplicarCambioAntecedente(UUID pacienteId, UUID medicoId,
            PropuestaCambioClinico.Operacion operacion, UUID recursoId, AntecedenteClinicoDTO datos, String motivo);

    /**
     * Aplica sobre el historial de un paciente un cambio en una alergia propuesto por un médico
     * y ya aceptado por el paciente. Las alergias solo admiten alta y borrado.
     *
     * @param pacienteId ID del paciente
     * @param medicoId ID del médico autor de la propuesta
     * @param operacion CREATE o DELETE
     * @param recursoId ID de la alergia objetivo (para DELETE)
     * @param datos datos de la alergia propuesta (para CREATE)
     * @param motivo motivo del cambio indicado por el médico
     * @return el DTO del historial clínico actualizado
     */
    HistorialClinicoDTO aplicarCambioAlergia(UUID pacienteId, UUID medicoId,
            PropuestaCambioClinico.Operacion operacion, UUID recursoId, AlergiaDTO datos, String motivo);

    /**
     * Aplica sobre el historial de un paciente un cambio en un dato clínico cuantitativo
     * (análisis de sangre/orina o signos vitales) propuesto por un médico y ya aceptado por el
     * paciente.
     *
     * @param pacienteId ID del paciente
     * @param medicoId ID del médico autor de la propuesta
     * @param operacion CREATE, UPDATE o DELETE
     * @param recursoId ID del dato clínico objetivo (para UPDATE y DELETE)
     * @param datos datos de la medición propuesta (para CREATE y UPDATE)
     * @param motivo motivo del cambio indicado por el médico
     * @return el DTO del historial clínico actualizado
     */
    HistorialClinicoDTO aplicarCambioMedicion(UUID pacienteId, UUID medicoId,
            PropuestaCambioClinico.Operacion operacion, UUID recursoId, DatoClinicoEntradaDTO datos, String motivo);
}
