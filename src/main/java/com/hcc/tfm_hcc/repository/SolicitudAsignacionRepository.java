package com.hcc.tfm_hcc.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.hcc.tfm_hcc.model.SolicitudAsignacion;

/**
 * Repositorio para la gestión de solicitudes de asignación médico-paciente en el sistema HCC.
 * Proporciona operaciones de persistencia y consulta para las solicitudes de vinculación
 * entre médicos y pacientes del sistema.
 * 
 * <p>Este repositorio permite:</p>
 * <ul>
 *   <li>Verificar la existencia de solicitudes por médico, paciente y estado</li>
 *   <li>Consultar solicitudes por estado específico y participantes</li>
 *   <li>Obtener historiales completos de solicitudes enviadas y recibidas</li>
 *   <li>Mantener trazabilidad de todas las interacciones médico-paciente</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface SolicitudAsignacionRepository extends CrudRepository<SolicitudAsignacion, UUID> {
    
    /**
     * Verifica si existe una solicitud de asignación entre un médico y paciente específicos con un estado determinado.
     * Útil para evitar solicitudes duplicadas y validar estados de relaciones.
     * 
     * @param medicoNif NIF del médico que envía la solicitud
     * @param pacienteNif NIF del paciente que recibe la solicitud
     * @param estado Estado de la solicitud a verificar (ej: "PENDIENTE", "ACEPTADA", "RECHAZADA")
     * @return true si existe una solicitud con los parámetros especificados, false en caso contrario
     */
    boolean existsByMedicoNifAndPacienteNifAndEstado(String medicoNif, String pacienteNif, String estado);
    
    /**
     * Busca todas las solicitudes enviadas por un médico específico que tengan un estado determinado.
     * Útil para mostrar solicitudes pendientes o filtrar por estado específico.
     * 
     * @param medicoNif NIF del médico del cual obtener las solicitudes
     * @param estado Estado de las solicitudes a buscar
     * @return Lista de SolicitudAsignacion enviadas por el médico con el estado especificado
     */
    List<SolicitudAsignacion> findByMedicoNifAndEstado(String medicoNif, String estado);
    
    /**
     * Busca todas las solicitudes recibidas por un paciente específico que tengan un estado determinado.
     * Útil para mostrar solicitudes pendientes de respuesta del paciente.
     * 
     * @param pacienteNif NIF del paciente del cual obtener las solicitudes
     * @param estado Estado de las solicitudes a buscar
     * @return Lista de SolicitudAsignacion recibidas por el paciente con el estado especificado
     */
    List<SolicitudAsignacion> findByPacienteNifAndEstado(String pacienteNif, String estado);
    
    /**
     * Obtiene el historial completo de solicitudes recibidas por un paciente específico.
     * Los resultados se ordenan por fecha de creación de forma descendente (más recientes primero).
     * 
     * @param pacienteNif NIF del paciente del cual obtener el historial de solicitudes
     * @return Lista de SolicitudAsignacion recibidas por el paciente, ordenadas por fecha descendente
     */
    List<SolicitudAsignacion> findByPacienteNifOrderByFechaCreacionDesc(String pacienteNif);

    /**
     * Obtiene el historial completo de solicitudes enviadas por un médico específico.
     * Los resultados se ordenan por fecha de creación de forma descendente (más recientes primero).
     * 
     * @param medicoNif NIF del médico del cual obtener el historial de solicitudes
     * @return Lista de SolicitudAsignacion enviadas por el médico, ordenadas por fecha descendente
     */
    List<SolicitudAsignacion> findByMedicoNifOrderByFechaCreacionDesc(String medicoNif);
}
