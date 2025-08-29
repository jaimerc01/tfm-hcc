package com.hcc.tfm_hcc.facade;

import java.util.List;

import com.hcc.tfm_hcc.dto.PacienteDTO;
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
}
