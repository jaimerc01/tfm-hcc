package com.hcc.tfm_hcc.facade;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.dto.AnotacionMedicaDTO;
import com.hcc.tfm_hcc.dto.ArchivoClinicoDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.model.AnotacionMedica;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;

/**
 * Facade para la gestión médica en el sistema HCC.
 * Proporciona operaciones específicas para médicos incluyendo búsqueda de pacientes
 * y gestión de solicitudes de asignación médico-paciente.
 * 
 * <p>Funcionalidades principales:</p>
 * <ul>
 *   <li>Búsqueda y validación de pacientes por datos de identificación</li>
 *   <li>Creación de solicitudes de asignación médico-paciente</li>
 *   <li>Consulta de solicitudes pendientes de aprobación</li>
 *   <li>Seguimiento de solicitudes enviadas por el médico</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface MedicoFacade {
    
    /**
     * Busca un paciente específico utilizando su DNI y fecha de nacimiento.
     * Esta operación permite verificar la identidad del paciente antes de
     * proceder con solicitudes de asignación o consultas médicas.
     * 
     * @param dni Documento Nacional de Identidad del paciente
     * @param fechaNacimiento Fecha de nacimiento del paciente en formato string
     * @return PacienteDTO con los datos del paciente encontrado
     * @throws IllegalArgumentException si los datos de identificación no son válidos
     */
    PacienteDTO buscarPacientePorDniYFechaNacimiento(String dni, String fechaNacimiento) throws IllegalArgumentException;

    /**
     * Lista los pacientes con una relación activa con el médico autenticado.
     *
     * @return Lista de PacienteDTO con los pacientes actualmente asignados
     */
    List<PacienteDTO> listarMisPacientes();

    /**
     * Obtiene el historial clínico de un paciente vinculado al médico autenticado.
     *
     * @param nifPaciente NIF del paciente cuyo historial se consulta
     * @return HistorialClinicoDTO con los datos médicos del paciente
     * @throws IllegalArgumentException si el NIF es inválido o el paciente no existe
     */
    HistorialClinicoDTO obtenerHistorialPaciente(String nifPaciente);

    /**
     * Crea una nueva solicitud de asignación entre el médico autenticado y un paciente.
     * La solicitud queda pendiente hasta que sea aprobada por el sistema o el paciente.
     * 
     * @param nifPaciente NIF del paciente al que se solicita asignación
     * @return SolicitudAsignacion creada con estado pendiente
     * @throws IllegalArgumentException si el NIF del paciente no es válido
     * @throws IllegalStateException si ya existe una solicitud activa para este paciente
     */
    SolicitudAsignacion crearSolicitudAsignacion(String nifPaciente) throws IllegalArgumentException, IllegalStateException;

    /**
     * Lista todas las solicitudes de asignación pendientes que requieren
     * aprobación para el médico autenticado.
     * 
     * @return Lista de SolicitudAsignacion con estado pendiente
     */
    List<SolicitudAsignacion> listarSolicitudesPendientes();
    
    /**
     * Lista todas las solicitudes de asignación enviadas por el médico autenticado,
     * independientemente de su estado actual (pendiente, aprobada, rechazada).
     *
     * @return Lista de SolicitudAsignacion enviadas por el médico
     */
    List<SolicitudAsignacion> listarSolicitudesEnviadas();

    /**
     * Escribe una anotación médica sobre un paciente vinculado al médico autenticado.
     * Solo se permite si existe una relación médico-paciente activa y el paciente no
     * ha limitado el tratamiento de sus datos. El paciente recibe una notificación.
     *
     * @param nifPaciente NIF del paciente sobre el que se escribe la anotación
     * @param mensaje contenido de la anotación
     * @return AnotacionMedica creada
     * @throws IllegalArgumentException si el mensaje está vacío
     * @throws com.hcc.tfm_hcc.exception.PacienteNoEncontradoException si no existe un paciente con ese NIF
     * @throws com.hcc.tfm_hcc.exception.UsuarioSinPermisoException si no hay relación activa con el
     *         paciente, o el paciente ha limitado el tratamiento de sus datos
     */
    AnotacionMedica crearAnotacion(String nifPaciente, String mensaje);

    /**
     * Lista las anotaciones que el médico autenticado ha escrito sobre un paciente
     * vinculado a él, de la más reciente a la más antigua.
     *
     * @param nifPaciente NIF del paciente
     * @return lista de AnotacionMedicaDTO escritas por el médico sobre ese paciente
     * @throws com.hcc.tfm_hcc.exception.PacienteNoEncontradoException si no existe un paciente con ese NIF
     * @throws com.hcc.tfm_hcc.exception.UsuarioSinPermisoException si no hay relación activa con el
     *         paciente, o el paciente ha limitado el tratamiento de sus datos
     */
    List<AnotacionMedicaDTO> listarAnotacionesPaciente(String nifPaciente);

    /**
     * Lista los documentos clínicos de un paciente vinculado al médico autenticado.
     *
     * @param nifPaciente NIF del paciente
     * @return lista de ArchivoClinicoDTO del paciente
     * @throws com.hcc.tfm_hcc.exception.UsuarioSinPermisoException si no hay relación activa con el paciente
     */
    List<ArchivoClinicoDTO> listarArchivosPaciente(String nifPaciente);

    /**
     * Sube un documento clínico al historial de un paciente vinculado al médico
     * autenticado. El paciente recibe una notificación.
     *
     * @param nifPaciente NIF del paciente
     * @param file documento a subir
     * @return ArchivoClinicoDTO del documento creado
     * @throws IOException si falla el cifrado del contenido
     * @throws com.hcc.tfm_hcc.exception.UsuarioSinPermisoException si no hay relación activa con el paciente
     */
    ArchivoClinicoDTO subirArchivoPaciente(String nifPaciente, MultipartFile file) throws IOException;

    /**
     * Obtiene los metadatos de un documento clínico de un paciente vinculado al médico autenticado.
     *
     * @param nifPaciente NIF del paciente
     * @param archivoId ID del documento
     * @return ArchivoClinicoDTO con el nombre original y el tipo de contenido
     */
    ArchivoClinicoDTO obtenerArchivoPaciente(String nifPaciente, UUID archivoId);

    /**
     * Descarga el contenido de un documento clínico de un paciente vinculado al médico autenticado.
     *
     * @param nifPaciente NIF del paciente
     * @param archivoId ID del documento
     * @return recurso con el contenido descifrado del documento
     */
    Resource descargarArchivoPaciente(String nifPaciente, UUID archivoId);
}
