package com.hcc.tfm_hcc.controller.impl;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hcc.tfm_hcc.constants.RestUrls;
import com.hcc.tfm_hcc.controller.MedicoController;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.facade.MedicoFacade;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.exception.PacienteNoEncontradoException;
import com.hcc.tfm_hcc.exception.SolicitudAsignacionException;
import com.hcc.tfm_hcc.exception.SolicitudExistenteException;
import com.hcc.tfm_hcc.exception.MedicoOperacionException;
import com.hcc.tfm_hcc.exception.UsuarioSinPermisoException;
import com.hcc.tfm_hcc.util.LogMaskUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del controlador REST para operaciones médicas.
 * Proporciona endpoints REST para funcionalidades específicas de médicos,
 * delegando la lógica de negocio al facade correspondiente.
 * 
 * <p>Características de seguridad:</p>
 * <ul>
 *   <li>Todos los endpoints requieren rol MEDICO</li>
 *   <li>Validación de autorización delegada a la capa facade</li>
 *   <li>Logging detallado de operaciones médicas</li>
 *   <li>Gestión centralizada de errores específicos</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping(RestUrls.MEDICO_BASE)
@RequiredArgsConstructor
public class MedicoControllerImpl implements MedicoController {

    /** Facade para operaciones médicas */
    private final MedicoFacade medicoFacade;

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.MEDICO_PACIENTES_BUSCAR)
    public ResponseEntity<PacienteDTO> buscarPaciente(@RequestParam("dni") String dni, 
                                                      @RequestParam("fechaNacimiento") String fechaNacimiento) {
        String dniLog = LogMaskUtil.enmascarar(dni);
        log.info("Buscando paciente con DNI: {} y fecha nacimiento: {}", dniLog, fechaNacimiento);

        try {
            PacienteDTO paciente = medicoFacade.buscarPacientePorDniYFechaNacimiento(dni, fechaNacimiento);
            if (paciente == null) {
                log.warn("Paciente no encontrado con DNI: {} y fecha nacimiento: {}", dniLog, fechaNacimiento);
                throw new PacienteNoEncontradoException("No se encontró paciente con los datos proporcionados");
            }

            log.info("Paciente encontrado exitosamente: {}", dniLog);
            return ResponseEntity.ok(paciente);
            
        } catch (PacienteNoEncontradoException e) {
            log.warn("Error: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al buscar paciente: {}", e.getMessage(), e);
            throw new MedicoOperacionException("Error al buscar paciente", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.MEDICO_PACIENTE_HISTORIAL)
    public ResponseEntity<HistorialClinicoDTO> obtenerHistorialPaciente(@PathVariable("nif") String nif) {
        String nifLog = LogMaskUtil.enmascarar(nif);
        log.info("Consultando historial clínico del paciente NIF: {}", nifLog);

        try {
            HistorialClinicoDTO historial = medicoFacade.obtenerHistorialPaciente(nif);
            log.info("Historial clínico consultado exitosamente para el paciente: {}", nifLog);
            return ResponseEntity.ok(historial != null ? historial : new HistorialClinicoDTO());
        } catch (PacienteNoEncontradoException e) {
            log.warn("Paciente no encontrado al consultar historial: {}", nifLog);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al consultar historial del paciente {}: {}", nifLog, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (UsuarioSinPermisoException e) {
            log.warn("Acceso denegado al historial del paciente {}: {}", nifLog, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error al consultar historial del paciente {}: {}", nifLog, e.getMessage(), e);
            throw new MedicoOperacionException("Error al consultar historial del paciente", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping(RestUrls.MEDICO_SOLICITUDES_ASIGNACION)
    public ResponseEntity<SolicitudAsignacion> crearSolicitudAsignacion(@RequestParam("nifPaciente") String nifPaciente) {
        String nifPacienteLog = LogMaskUtil.enmascarar(nifPaciente);
        log.info("Creando solicitud de asignación para paciente NIF: {}", nifPacienteLog);

        try {
            SolicitudAsignacion solicitud = medicoFacade.crearSolicitudAsignacion(nifPaciente);
            if (solicitud == null) {
                log.warn("No se pudo crear solicitud para paciente: {}", nifPacienteLog);
                throw new SolicitudAsignacionException("No se pudo crear la solicitud de asignación");
            }

            log.info("Solicitud de asignación creada exitosamente para paciente: {}", nifPacienteLog);
            return ResponseEntity.ok(solicitud);

        } catch (SolicitudExistenteException _) {
            log.warn("Solicitud ya existe para paciente: {}", nifPacienteLog);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
                    
        } catch (SolicitudAsignacionException e) {
            log.warn("Error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error al crear solicitud de asignación: {}", e.getMessage(), e);
            throw new MedicoOperacionException("Error al crear solicitud de asignación", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.MEDICO_SOLICITUDES_PENDIENTES)
    public ResponseEntity<List<SolicitudAsignacion>> listarSolicitudesPendientes() {
        log.info("Listando solicitudes pendientes para médico autenticado");
        
        try {
            List<SolicitudAsignacion> lista = medicoFacade.listarSolicitudesPendientes();
            log.info("Se obtuvieron {} solicitudes pendientes para el médico", lista.size());
            return ResponseEntity.ok(lista);
            
        } catch (Exception e) {
            log.error("Error al listar solicitudes pendientes: {}", e.getMessage(), e);
            throw new MedicoOperacionException("Error al listar solicitudes pendientes", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.MEDICO_SOLICITUDES_ENVIADAS)
    public ResponseEntity<List<SolicitudAsignacion>> listarSolicitudesEnviadas() {
        log.info("Listando solicitudes enviadas para médico autenticado");
        
        try {
            List<SolicitudAsignacion> lista = medicoFacade.listarSolicitudesEnviadas();
            log.info("Se obtuvieron {} solicitudes enviadas para el médico", lista.size());
            return ResponseEntity.ok(lista);
            
        } catch (Exception e) {
            log.error("Error al listar solicitudes enviadas: {}", e.getMessage(), e);
            throw new MedicoOperacionException("Error al listar solicitudes enviadas", e);
        }
    }
}
