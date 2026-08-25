package com.hcc.tfm_hcc.facade.impl;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.facade.MedicoFacade;
import com.hcc.tfm_hcc.facade.SolicitudAsignacionFacade;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.service.HistorialClinicoService;
import com.hcc.tfm_hcc.service.MedicoService;
import com.hcc.tfm_hcc.exception.PacienteNoEncontradoException;
import com.hcc.tfm_hcc.exception.SolicitudAsignacionException;
import com.hcc.tfm_hcc.exception.MedicoOperacionException;
import com.hcc.tfm_hcc.exception.MedicoValidationException;
import com.hcc.tfm_hcc.exception.UsuarioSinPermisoException;
import com.hcc.tfm_hcc.util.LogMaskUtil;

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
    private final SolicitudAsignacionFacade solicitudAsignacionFacade;
    private final HistorialClinicoService historialClinicoService;

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
    @PreAuthorize("hasRole('MEDICO')")
    public PacienteDTO buscarPacientePorDniYFechaNacimiento(String dni, String fechaNacimiento) {
        String dniLog = LogMaskUtil.enmascarar(dni);
        log.debug("Buscando paciente por DNI: {} y fecha de nacimiento: {}", dniLog, fechaNacimiento);

        try {
            validarDatosBusquedaPaciente(dni, fechaNacimiento);

            PacienteDTO paciente = medicoService.buscarPacientePorDniYFechaNacimiento(dni, fechaNacimiento);

            log.info("Paciente encontrado exitosamente: DNI {}", dniLog);
            return paciente;
        } catch (MedicoValidationException e) {
            log.warn("Error de validación en búsqueda de paciente: DNI {} - Error: {}",
                    dniLog, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado durante búsqueda de paciente: DNI {} - Error: {}",
                     dniLog, e.getMessage(), e);
            throw new MedicoOperacionException("Error interno durante la búsqueda del paciente", e);
        }
    }

    /**
     * Obtiene el historial clínico de un paciente vinculado al médico autenticado.
     *
     * @param nifPaciente NIF del paciente cuyo historial se consulta
     * @return HistorialClinicoDTO Los datos del historial clínico del paciente
     * @throws MedicoValidationException Si el NIF del paciente es inválido
     * @throws PacienteNoEncontradoException Si no existe un paciente con ese NIF
     * @throws UsuarioSinPermisoException Si el médico no tiene una relación activa con el paciente
     */
    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public HistorialClinicoDTO obtenerHistorialPaciente(String nifPaciente) {
        String nifPacienteLog = LogMaskUtil.enmascarar(nifPaciente);
        log.debug("Consultando historial clínico del paciente: {}", nifPacienteLog);

        try {
            validarNifPaciente(nifPaciente);

            HistorialClinicoDTO historial = historialClinicoService.obtenerHistorialPaciente(nifPaciente);

            log.info("Historial clínico del paciente {} consultado exitosamente", nifPacienteLog);
            return historial;
        } catch (MedicoValidationException e) {
            log.warn("Error de validación al consultar historial del paciente {}: {}", nifPacienteLog, e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            log.warn("Acceso denegado al consultar historial del paciente {}: {}", nifPacienteLog, e.getMessage());
            throw new UsuarioSinPermisoException(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            log.warn("Paciente no encontrado al consultar historial: {} - {}", nifPacienteLog, e.getMessage());
            throw new PacienteNoEncontradoException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al consultar historial del paciente {}: {}", nifPacienteLog, e.getMessage(), e);
            throw new MedicoOperacionException("Error interno durante la consulta del historial del paciente", e);
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
    @PreAuthorize("hasRole('MEDICO')")
    public SolicitudAsignacion crearSolicitudAsignacion(String nifPaciente) {
        String nifPacienteLog = LogMaskUtil.enmascarar(nifPaciente);
        log.debug("Creando solicitud de asignación para paciente: {}", nifPacienteLog);

        try {
            validarNifPaciente(nifPaciente);

            SolicitudAsignacion solicitud = solicitudAsignacionFacade.crearSolicitudAsignacion(nifPaciente);

            log.info("Solicitud de asignación creada exitosamente: ID {} para paciente {}",
                    solicitud.getId(), nifPacienteLog);
            return solicitud;
        } catch (MedicoValidationException e) {
            log.warn("Error de validación al crear solicitud de asignación: Paciente {} - Error: {}",
                    nifPacienteLog, e.getMessage());
            throw new SolicitudAsignacionException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al crear solicitud de asignación: Paciente {} - Error: {}",
                     nifPacienteLog, e.getMessage(), e);
            throw new MedicoOperacionException("Error interno durante la creación de solicitud de asignación", e);
        }
    }

    /**
     * Lista todas las solicitudes de asignación pendientes.
     * 
     * @return List<SolicitudAsignacion> Lista de solicitudes pendientes
     * @throws RuntimeException Si ocurre un error durante la consulta
     */
    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public List<SolicitudAsignacion> listarSolicitudesPendientes() {
        log.debug("Obteniendo solicitudes de asignación pendientes");
        
        try {
            List<SolicitudAsignacion> solicitudes = solicitudAsignacionFacade.listarSolicitudesPendientes();
            
            log.info("Solicitudes pendientes obtenidas: {} registros", 
                    solicitudes != null ? solicitudes.size() : 0);
            return solicitudes;
        } catch (Exception e) {
            log.error("Error inesperado al obtener solicitudes pendientes: {}", e.getMessage(), e);
            throw new MedicoOperacionException("Error interno durante la consulta de solicitudes pendientes", e);
        }
    }

    /**
     * Lista todas las solicitudes de asignación enviadas por el médico actual.
     * 
     * @return List<SolicitudAsignacion> Lista de solicitudes enviadas
     * @throws RuntimeException Si ocurre un error durante la consulta
     */
    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public List<SolicitudAsignacion> listarSolicitudesEnviadas() {
        log.debug("Obteniendo solicitudes de asignación enviadas por el médico actual");
        
        try {
            List<SolicitudAsignacion> solicitudes = solicitudAsignacionFacade.listarSolicitudesEnviadas();
            
            log.info("Solicitudes enviadas obtenidas: {} registros", 
                    solicitudes != null ? solicitudes.size() : 0);
            return solicitudes;
        } catch (Exception e) {
            log.error("Error inesperado al obtener solicitudes enviadas: {}", e.getMessage(), e);
            throw new MedicoOperacionException("Error interno durante la consulta de solicitudes enviadas", e);
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
            throw new MedicoValidationException("El DNI del paciente es obligatorio");
        }
        
        if (fechaNacimiento == null || fechaNacimiento.trim().isEmpty()) {
            throw new MedicoValidationException("La fecha de nacimiento es obligatoria");
        }
        
        // Validar formato de fecha básico
        if (!fechaNacimiento.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new MedicoValidationException("La fecha de nacimiento debe tener formato yyyy-MM-dd");
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
            throw new MedicoValidationException("El NIF del paciente es obligatorio");
        }
    }
}
