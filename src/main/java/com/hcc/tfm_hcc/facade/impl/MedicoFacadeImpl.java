package com.hcc.tfm_hcc.facade.impl;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.converter.AnotacionMedicaConverter;
import com.hcc.tfm_hcc.dto.AnotacionMedicaDTO;
import com.hcc.tfm_hcc.dto.ArchivoClinicoDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.dto.PropuestaCambioClinicoDTO;
import com.hcc.tfm_hcc.dto.PropuestaCambioClinicoRequestDTO;
import com.hcc.tfm_hcc.converter.PropuestaCambioClinicoConverter;
import com.hcc.tfm_hcc.facade.MedicoFacade;
import com.hcc.tfm_hcc.facade.NotificacionFacade;
import com.hcc.tfm_hcc.mapper.ArchivoClinicoMapper;
import com.hcc.tfm_hcc.model.AnotacionMedica;
import com.hcc.tfm_hcc.model.ArchivoClinico;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.service.AnotacionMedicaService;
import com.hcc.tfm_hcc.service.ArchivoClinicoService;
import com.hcc.tfm_hcc.service.HistorialClinicoService;
import com.hcc.tfm_hcc.service.MedicoService;
import com.hcc.tfm_hcc.service.PropuestaCambioClinicoService;
import com.hcc.tfm_hcc.service.SolicitudAsignacionService;
import com.hcc.tfm_hcc.exception.ArchivoClinicoException;
import com.hcc.tfm_hcc.exception.PacienteNoEncontradoException;
import com.hcc.tfm_hcc.exception.PropuestaCambioClinicoException;
import com.hcc.tfm_hcc.exception.PropuestaCambioNoEncontradaException;
import com.hcc.tfm_hcc.exception.SolicitudAsignacionException;
import com.hcc.tfm_hcc.exception.SolicitudExistenteException;
import com.hcc.tfm_hcc.exception.MedicoOperacionException;
import com.hcc.tfm_hcc.exception.MedicoValidationException;
import com.hcc.tfm_hcc.exception.UsuarioNoAutenticadoException;
import com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException;
import com.hcc.tfm_hcc.exception.UsuarioSinPermisoException;
import com.hcc.tfm_hcc.util.LogMaskUtil;
import com.hcc.tfm_hcc.util.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación de la fachada para operaciones médicas.
 * 
 * <p>Esta clase actúa como una capa de fachada entre los controladores y los servicios
 * médicos ({@code MedicoService}, {@code HistorialClinicoService},
 * {@code SolicitudAsignacionService}), proporcionando una interfaz simplificada para las
 * operaciones relacionadas con la gestión de médicos, pacientes y solicitudes de
 * asignación. Orquesta directamente los servicios; no delega en otras fachadas.</p>
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
    private final SolicitudAsignacionService solicitudAsignacionService;
    private final HistorialClinicoService historialClinicoService;
    private final AnotacionMedicaService anotacionMedicaService;
    private final AnotacionMedicaConverter anotacionMedicaConverter;
    private final ArchivoClinicoService archivoClinicoService;
    private final ArchivoClinicoMapper archivoClinicoMapper;
    private final NotificacionFacade notificacionFacade;
    private final PropuestaCambioClinicoService propuestaCambioClinicoService;
    private final PropuestaCambioClinicoConverter propuestaCambioClinicoConverter;

    /** Textos del aviso que recibe el paciente cuando un médico le añade una anotación. */
    private static final String MENSAJE_ANOTACION_PREFIJO = "Tu médico ";
    private static final String MENSAJE_ANOTACION_SUFIJO = " ha añadido una nueva anotación a tu historial";

    /** Textos del aviso que recibe el paciente cuando un médico le sube un documento. */
    private static final String MENSAJE_ARCHIVO_PREFIJO = "Tu médico ";
    private static final String MENSAJE_ARCHIVO_SUFIJO = " ha añadido un nuevo documento a tu historial";

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
     * Lista los pacientes con una relación activa con el médico autenticado.
     *
     * @return List<PacienteDTO> Lista de pacientes actualmente asignados al médico
     * @throws RuntimeException Si ocurre un error durante la consulta
     */
    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public List<PacienteDTO> listarMisPacientes() {
        log.debug("Listando pacientes asignados al médico autenticado");

        String nifMedico = SecurityUtils.getCurrentUserNif();
        if (nifMedico == null) {
            log.warn("Usuario actual sin NIF al listar pacientes asignados");
            return List.of();
        }

        try {
            List<PacienteDTO> pacientes = medicoService.listarMisPacientes(nifMedico);
            log.info("Pacientes asignados obtenidos: {} registros", pacientes.size());
            return pacientes;
        } catch (Exception e) {
            log.error("Error inesperado al listar pacientes asignados: {}", e.getMessage(), e);
            throw new MedicoOperacionException("Error interno durante la consulta de pacientes asignados", e);
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

        String nifMedico = SecurityUtils.getCurrentUserNif();
        if (nifMedico == null) {
            log.warn("Usuario actual sin NIF al crear solicitud de asignación");
            throw new UsuarioNoAutenticadoException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }

        try {
            validarNifPaciente(nifPaciente);

            SolicitudAsignacion solicitud = solicitudAsignacionService.crearSolicitud(nifMedico, nifPaciente);

            log.info("Solicitud de asignación creada exitosamente: ID {} para paciente {}",
                    solicitud.getId(), nifPacienteLog);
            return solicitud;
        } catch (MedicoValidationException e) {
            log.warn("Error de validación al crear solicitud de asignación: Paciente {} - Error: {}",
                    nifPacienteLog, e.getMessage());
            throw new SolicitudAsignacionException(e.getMessage(), e);
        } catch (SolicitudExistenteException e) {
            // Conflicto legítimo (ya hay una solicitud pendiente): debe llegar al
            // controlador como tal (409), no quedar sepultado en un MedicoOperacionException.
            log.warn("Solicitud de asignación ya existente para el paciente {}: {}", nifPacienteLog, e.getMessage());
            throw e;
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

        String nifMedico = SecurityUtils.getCurrentUserNif();
        if (nifMedico == null) {
            log.warn("Usuario actual sin NIF al listar solicitudes pendientes");
            return List.of();
        }

        try {
            List<SolicitudAsignacion> solicitudes =
                    solicitudAsignacionService.listarSolicitudesPendientesPorMedico(nifMedico);

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

        String nifMedico = SecurityUtils.getCurrentUserNif();
        if (nifMedico == null) {
            log.warn("Usuario actual sin NIF al listar solicitudes enviadas");
            return List.of();
        }

        try {
            List<SolicitudAsignacion> solicitudes =
                    solicitudAsignacionService.listarSolicitudesEnviadasPorMedico(nifMedico);

            log.info("Solicitudes enviadas obtenidas: {} registros",
                    solicitudes != null ? solicitudes.size() : 0);
            return solicitudes;
        } catch (Exception e) {
            log.error("Error inesperado al obtener solicitudes enviadas: {}", e.getMessage(), e);
            throw new MedicoOperacionException("Error interno durante la consulta de solicitudes enviadas", e);
        }
    }

    // ===============================
    // MÉTODOS DE ANOTACIONES MÉDICAS
    // ===============================

    /**
     * Escribe una anotación médica sobre un paciente vinculado al médico autenticado.
     *
     * @param nifPaciente NIF del paciente sobre el que se escribe la anotación
     * @param mensaje contenido de la anotación
     * @return AnotacionMedica La anotación médica creada
     * @throws MedicoValidationException Si el mensaje está vacío
     * @throws PacienteNoEncontradoException Si no existe un paciente con ese NIF
     * @throws UsuarioSinPermisoException Si el médico no tiene una relación activa con el paciente,
     *         o el paciente ha limitado el tratamiento de sus datos
     */
    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public AnotacionMedica crearAnotacion(String nifPaciente, String mensaje) {
        String nifPacienteLog = LogMaskUtil.enmascarar(nifPaciente);
        log.debug("Creando anotación médica para paciente: {}", nifPacienteLog);

        String nifMedico = SecurityUtils.getCurrentUserNif();
        if (nifMedico == null) {
            log.warn("Usuario actual sin NIF al crear anotación médica");
            throw new UsuarioNoAutenticadoException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }

        try {
            validarNifPaciente(nifPaciente);

            AnotacionMedica anotacion = anotacionMedicaService.crearAnotacion(nifMedico, nifPaciente, mensaje);
            notificarAnotacionAlPaciente(anotacion);

            log.info("Anotación médica creada exitosamente: ID {} para paciente {}", anotacion.getId(), nifPacienteLog);
            return anotacion;
        } catch (MedicoValidationException e) {
            log.warn("Error de validación al crear anotación médica: Paciente {} - Error: {}", nifPacienteLog, e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            log.warn("Acceso denegado al crear anotación médica para el paciente {}: {}", nifPacienteLog, e.getMessage());
            throw new UsuarioSinPermisoException(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            log.warn("Paciente no encontrado al crear anotación médica: {} - {}", nifPacienteLog, e.getMessage());
            throw new PacienteNoEncontradoException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al crear anotación médica para el paciente {}: {}", nifPacienteLog, e.getMessage(), e);
            throw new MedicoOperacionException("Error interno durante la creación de la anotación médica", e);
        }
    }

    /**
     * Emite el aviso al paciente de que ha recibido una nueva anotación. Un fallo al
     * notificar no invalida la anotación ya creada, así que se registra y se continúa.
     */
    private void notificarAnotacionAlPaciente(AnotacionMedica anotacion) {
        Usuario medico = anotacion.getMedico();
        Usuario paciente = anotacion.getPaciente();
        if (medico == null || paciente == null) {
            return;
        }
        String nombreMedico = medico.getNombre() != null ? medico.getNombre() : medico.getNif();
        try {
            notificacionFacade.crearNotificacionParaUsuario(paciente.getNif(),
                    MENSAJE_ANOTACION_PREFIJO + nombreMedico + MENSAJE_ANOTACION_SUFIJO);
        } catch (Exception e) {
            log.warn("No se pudo notificar la anotación al paciente {}: {}",
                    LogMaskUtil.enmascarar(paciente.getNif()), e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public List<AnotacionMedicaDTO> listarAnotacionesPaciente(String nifPaciente) {
        String nifPacienteLog = LogMaskUtil.enmascarar(nifPaciente);
        log.debug("Listando anotaciones escritas para el paciente {}", nifPacienteLog);

        String nifMedico = SecurityUtils.getCurrentUserNif();
        if (nifMedico == null) {
            log.warn("Usuario actual sin NIF al listar anotaciones médicas");
            throw new UsuarioNoAutenticadoException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }

        try {
            validarNifPaciente(nifPaciente);

            List<AnotacionMedica> anotaciones =
                    anotacionMedicaService.listarAnotacionesEscritasPorMedico(nifMedico, nifPaciente);
            return anotacionMedicaConverter.toDtoList(anotaciones);
        } catch (MedicoValidationException e) {
            log.warn("NIF de paciente inválido al listar anotaciones médicas: {}", e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            log.warn("Acceso denegado al listar anotaciones médicas del paciente {}: {}", nifPacienteLog, e.getMessage());
            throw new UsuarioSinPermisoException(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            log.warn("Paciente no encontrado al listar anotaciones médicas: {} - {}", nifPacienteLog, e.getMessage());
            throw new PacienteNoEncontradoException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al listar anotaciones médicas del paciente {}: {}", nifPacienteLog, e.getMessage(), e);
            throw new MedicoOperacionException("Error interno al listar las anotaciones del paciente", e);
        }
    }

    // ===============================
    // MÉTODOS DE DOCUMENTOS DEL PACIENTE
    // ===============================

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public List<ArchivoClinicoDTO> listarArchivosPaciente(String nifPaciente) {
        String nifPacienteLog = LogMaskUtil.enmascarar(nifPaciente);
        log.debug("Listando documentos del paciente {}", nifPacienteLog);
        try {
            return archivoClinicoService.listForPaciente(nifPaciente).stream()
                    .map(archivoClinicoMapper::toDto)
                    .toList();
        } catch (UsuarioSinPermisoException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al listar documentos del paciente {}: {}", nifPacienteLog, e.getMessage(), e);
            throw new MedicoOperacionException("Error interno al listar los documentos del paciente", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public ArchivoClinicoDTO subirArchivoPaciente(String nifPaciente, MultipartFile file) throws IOException {
        String nifPacienteLog = LogMaskUtil.enmascarar(nifPaciente);
        log.debug("Subiendo documento para el paciente {}", nifPacienteLog);
        try {
            ArchivoClinico guardado = archivoClinicoService.uploadForPaciente(nifPaciente, file);
            notificarDocumentoAlPaciente(nifPaciente);
            log.info("Documento clínico subido para el paciente {}: ID {}", nifPacienteLog, guardado.getId());
            return archivoClinicoMapper.toDto(guardado);
        } catch (UsuarioSinPermisoException | IllegalArgumentException e) {
            throw e;
        } catch (IOException e) {
            log.error("Error de cifrado al subir documento para el paciente {}: {}", nifPacienteLog, e.getMessage(), e);
            throw new ArchivoClinicoException("No se pudo guardar el documento", e);
        } catch (Exception e) {
            log.error("Error al subir documento para el paciente {}: {}", nifPacienteLog, e.getMessage(), e);
            throw new MedicoOperacionException("Error interno al subir el documento del paciente", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public ArchivoClinicoDTO obtenerArchivoPaciente(String nifPaciente, UUID archivoId) {
        return archivoClinicoService.listForPaciente(nifPaciente).stream()
                .filter(archivo -> archivo.getId().equals(archivoId))
                .findFirst()
                .map(archivoClinicoMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ERROR_ARCHIVO_NO_EXISTE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public Resource descargarArchivoPaciente(String nifPaciente, UUID archivoId) {
        return archivoClinicoService.getPacienteResource(nifPaciente, archivoId);
    }

    /**
     * Avisa al paciente de que su médico le ha añadido un documento. Un fallo al
     * notificar no invalida la subida ya realizada.
     */
    private void notificarDocumentoAlPaciente(String nifPaciente) {
        try {
            Usuario medico = SecurityUtils.getCurrentUser();
            String nombreMedico = medico != null && medico.getNombre() != null ? medico.getNombre()
                    : (medico != null ? medico.getNif() : "");
            notificacionFacade.crearNotificacionParaUsuario(nifPaciente,
                    MENSAJE_ARCHIVO_PREFIJO + nombreMedico + MENSAJE_ARCHIVO_SUFIJO);
        } catch (Exception e) {
            log.warn("No se pudo notificar el documento al paciente {}: {}",
                    LogMaskUtil.enmascarar(nifPaciente), e.getMessage());
        }
    }

    // ===============================
    // MÉTODOS DE PROPUESTAS DE CAMBIO CLÍNICO
    // ===============================

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public PropuestaCambioClinicoDTO proponerCambioClinico(String nifPaciente, PropuestaCambioClinicoRequestDTO request) {
        String nifPacienteLog = LogMaskUtil.enmascarar(nifPaciente);
        log.debug("Registrando propuesta de cambio clínico para el paciente {}", nifPacienteLog);

        String nifMedico = SecurityUtils.getCurrentUserNif();
        if (nifMedico == null) {
            log.warn("Usuario actual sin NIF al registrar propuesta de cambio clínico");
            throw new UsuarioNoAutenticadoException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }

        try {
            validarNifPaciente(nifPaciente);
            var propuesta = propuestaCambioClinicoService.crearPropuesta(nifMedico, nifPaciente, request);
            log.info("Propuesta de cambio clínico {} registrada para el paciente {}", propuesta.getId(), nifPacienteLog);
            return propuestaCambioClinicoConverter.toDto(propuesta);
        } catch (MedicoValidationException | PropuestaCambioClinicoException | PropuestaCambioNoEncontradaException
                 | UsuarioSinPermisoException | UsuarioNoEncontradoException e) {
            log.warn("Propuesta de cambio clínico rechazada para el paciente {}: {}", nifPacienteLog, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al registrar propuesta de cambio clínico para el paciente {}: {}",
                    nifPacienteLog, e.getMessage(), e);
            throw new MedicoOperacionException("Error interno durante el registro de la propuesta de cambio clínico", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public List<PropuestaCambioClinicoDTO> listarPropuestasCambioParaPaciente(String nifPaciente) {
        String nifPacienteLog = LogMaskUtil.enmascarar(nifPaciente);
        log.debug("Listando propuestas de cambio clínico enviadas al paciente {}", nifPacienteLog);

        String nifMedico = SecurityUtils.getCurrentUserNif();
        if (nifMedico == null) {
            log.warn("Usuario actual sin NIF al listar propuestas de cambio clínico");
            throw new UsuarioNoAutenticadoException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }

        try {
            validarNifPaciente(nifPaciente);
            return propuestaCambioClinicoConverter.toDtoList(
                    propuestaCambioClinicoService.listarPropuestasEnviadasParaPaciente(nifMedico, nifPaciente));
        } catch (MedicoValidationException e) {
            log.warn("NIF de paciente inválido al listar propuestas de cambio clínico: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al listar propuestas de cambio clínico del paciente {}: {}",
                    nifPacienteLog, e.getMessage(), e);
            throw new MedicoOperacionException("Error interno al listar las propuestas de cambio clínico", e);
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
