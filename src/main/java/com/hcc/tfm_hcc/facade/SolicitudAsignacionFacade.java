package com.hcc.tfm_hcc.facade;

import java.util.List;

import com.hcc.tfm_hcc.model.SolicitudAsignacion;

/**
 * Facade para la gestión de solicitudes de asignación.
 */
public interface SolicitudAsignacionFacade {    
    
    /**
     * Crea una solicitud de asignación médico-paciente.
     * La solicitud es creada por el médico autenticado actualmente en el sistema.
     * 
     * @param nifPaciente NIF (Número de Identificación Fiscal) del paciente
     * @return SolicitudAsignacion creada con estado pendiente
     * @throws IllegalArgumentException si el NIF del paciente es inválido
     * @throws RuntimeException si el médico no está autenticado o el paciente no existe
     */
    SolicitudAsignacion crearSolicitudAsignacion(String nifPaciente);

    /**
     * Lista todas las solicitudes de asignación pendientes para el médico autenticado.
     * Estas son las solicitudes que requieren aprobación por parte del sistema o el paciente.
     * 
     * @return Lista de SolicitudAsignacion con estado pendiente
     * @throws RuntimeException si el médico no está autenticado o ocurre un error durante la consulta
     */
    List<SolicitudAsignacion> listarSolicitudesPendientes();

    /**
     * Lista todas las solicitudes de asignación enviadas por el médico autenticado,
     * independientemente de su estado actual (pendiente, aprobada, rechazada).
     * Esto permite al médico hacer seguimiento de todas sus solicitudes.
     * 
     * @return Lista de SolicitudAsignacion enviadas por el médico actual
     * @throws RuntimeException si el médico no está autenticado o ocurre un error durante la consulta
     */
    List<SolicitudAsignacion> listarSolicitudesEnviadas();
}
