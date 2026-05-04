package com.hcc.tfm_hcc.facade.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.facade.MedicoFacade;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.service.MedicoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación de la fachada para operaciones médicas.
 * 
 * <p>Esta clase actúa como una capa de fachada entre los controladores y los servicios
 * médicos, proporcionando una interfaz simplificada para las operaciones relacionadas
 * con la gestión de médicos, pacientes y solicitudes de asignación.</p>
 * 
 * <p>Funcionalidades principales:</p>
 * <ul>
 *   <li>Creación y gestión de solicitudes de asignación médico-paciente</li>
 *   <li>Búsqueda de pacientes por DNI y fecha de nacimiento</li>
 *   <li>Listado de solicitudes pendientes y enviadas</li>
 *   <li>Validación de datos médicos y de pacientes</li>
 * </ul>
 * 
 * <p>La clase utiliza inyección de dependencias por constructor y logging estructurado
 * para seguir las mejores prácticas de desarrollo empresarial.</p>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicoFacadeImpl implements MedicoFacade {

    // ===============================
    // DEPENDENCIAS INYECTADAS
    // ===============================
    
    /**
     * Servicio para operaciones médicas.
     */
    private final MedicoService medicoService;

    // ===============================
    // MÉTODOS DE GESTIÓN DE PACIENTES
    // ===============================

    /**
     * Busca un paciente por su DNI y fecha de nacimiento.
     * 
     * @param dni DNI del paciente a buscar
     * @param fechaNacimiento Fecha de nacimiento del paciente (formato: yyyy-MM-dd)
     * @return PacienteDTO Los datos del paciente encontrado
     * @throws IllegalArgumentException Si los parámetros de búsqueda son inválidos
     * @throws RuntimeException Si ocurre un error durante la búsqueda
     */
    @Override
    public PacienteDTO buscarPacientePorDniYFechaNacimiento(String dni, String fechaNacimiento) {
        log.debug("Buscando paciente por DNI: {} y fecha de nacimiento: {}", dni, fechaNacimiento);
        
        try {
            validarDatosBusquedaPaciente(dni, fechaNacimiento);
            
            PacienteDTO paciente = medicoService.buscarPacientePorDniYFechaNacimiento(dni, fechaNacimiento);
            
            log.info("Paciente encontrado exitosamente: DNI {}", dni);
            return paciente;
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación en búsqueda de paciente: DNI {} - Error: {}", 
                    dni, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado durante búsqueda de paciente: DNI {} - Error: {}", 
                     dni, e.getMessage(), e);
            throw new RuntimeException("Error interno durante la búsqueda del paciente", e);
        }
    }

    // ===============================
    // MÉTODOS DE SOLICITUDES DE ASIGNACIÓN
    // ===============================

    /**
     * Crea una nueva solicitud de asignación médico-paciente.
     * 
     * @param nifPaciente NIF del paciente para la solicitud de asignación
     * @return SolicitudAsignacion La solicitud de asignación creada
     * @throws IllegalArgumentException Si el NIF del paciente es inválido
     * @throws RuntimeException Si ocurre un error durante la creación
     */
    @Override
    public SolicitudAsignacion crearSolicitudAsignacion(String nifPaciente) {
        log.debug("Creando solicitud de asignación para paciente: {}", nifPaciente);
        
        try {
            validarNifPaciente(nifPaciente);
            
            SolicitudAsignacion solicitud = medicoService.crearSolicitudAsignacion(nifPaciente);
            
            log.info("Solicitud de asignación creada exitosamente: ID {} para paciente {}", 
                    solicitud.getId(), nifPaciente);
            return solicitud;
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al crear solicitud de asignación: Paciente {} - Error: {}", 
                    nifPaciente, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al crear solicitud de asignación: Paciente {} - Error: {}", 
                     nifPaciente, e.getMessage(), e);
            throw new RuntimeException("Error interno durante la creación de solicitud de asignación", e);
        }
    }

    /**
     * Lista todas las solicitudes de asignación pendientes.
     * 
     * @return List<SolicitudAsignacion> Lista de solicitudes pendientes
     * @throws RuntimeException Si ocurre un error durante la consulta
     */
    @Override
    public List<SolicitudAsignacion> listarSolicitudesPendientes() {
        log.debug("Obteniendo solicitudes de asignación pendientes");
        
        try {
            List<SolicitudAsignacion> solicitudes = medicoService.listarSolicitudesPendientes();
            
            log.info("Solicitudes pendientes obtenidas: {} registros", 
                    solicitudes != null ? solicitudes.size() : 0);
            return solicitudes;
        } catch (Exception e) {
            log.error("Error inesperado al obtener solicitudes pendientes: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno durante la consulta de solicitudes pendientes", e);
        }
    }

    /**
     * Lista todas las solicitudes de asignación enviadas por el médico actual.
     * 
     * @return List<SolicitudAsignacion> Lista de solicitudes enviadas
     * @throws RuntimeException Si ocurre un error durante la consulta
     */
    @Override
    public List<SolicitudAsignacion> listarSolicitudesEnviadas() {
        log.debug("Obteniendo solicitudes de asignación enviadas por el médico actual");
        
        try {
            List<SolicitudAsignacion> solicitudes = medicoService.listarSolicitudesEnviadas();
            
            log.info("Solicitudes enviadas obtenidas: {} registros", 
                    solicitudes != null ? solicitudes.size() : 0);
            return solicitudes;
        } catch (Exception e) {
            log.error("Error inesperado al obtener solicitudes enviadas: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno durante la consulta de solicitudes enviadas", e);
        }
    }

    // ===============================
    // MÉTODOS UTILITARIOS PRIVADOS
    // ===============================

    /**
     * Valida los datos para búsqueda de paciente.
     * 
     * @param dni DNI del paciente
     * @param fechaNacimiento Fecha de nacimiento del paciente
     * @throws IllegalArgumentException Si los datos son inválidos
     */
    private void validarDatosBusquedaPaciente(String dni, String fechaNacimiento) {
        if (dni == null || dni.trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI del paciente es obligatorio");
        }
        
        if (fechaNacimiento == null || fechaNacimiento.trim().isEmpty()) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }
        
        // Validar formato de fecha básico
        if (!fechaNacimiento.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("La fecha de nacimiento debe tener formato yyyy-MM-dd");
        }
    }

    /**
     * Valida el NIF de un paciente.
     * 
     * @param nifPaciente NIF del paciente
     * @throws IllegalArgumentException Si el NIF es inválido
     */
    private void validarNifPaciente(String nifPaciente) {
        if (nifPaciente == null || nifPaciente.trim().isEmpty()) {
            throw new IllegalArgumentException("El NIF del paciente es obligatorio");
        }
    }
}
