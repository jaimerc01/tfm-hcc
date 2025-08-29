package com.hcc.tfm_hcc.controller.impl;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hcc.tfm_hcc.controller.MedicoController;
import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.facade.MedicoFacade;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;

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
 *   <li>Validación de autorización mediante @PreAuthorize</li>
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
@RequestMapping("/medico")
@RequiredArgsConstructor
public class MedicoControllerImpl implements MedicoController {

    /** Facade para operaciones médicas */
    private final MedicoFacade medicoFacade;

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasRole('MEDICO')")
    @GetMapping("/pacientes/buscar")
    public ResponseEntity<PacienteDTO> buscarPaciente(@RequestParam("dni") String dni, 
                                                      @RequestParam("fechaNacimiento") String fechaNacimiento) {
        log.info("Buscando paciente con DNI: {} y fecha nacimiento: {}", dni, fechaNacimiento);
        
        try {
            PacienteDTO paciente = medicoFacade.buscarPacientePorDniYFechaNacimiento(dni, fechaNacimiento);
            if (paciente == null) {
                log.warn("No se encontró paciente con DNI: {} y fecha: {}", dni, fechaNacimiento);
                return ResponseEntity.notFound().build();
            }
            
            log.info("Paciente encontrado exitosamente: {}", paciente.getNif());
            return ResponseEntity.ok(paciente);
            
        } catch (Exception e) {
            log.error("Error al buscar paciente con DNI: {}, error: {}", dni, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasRole('MEDICO')")
    @PostMapping("/solicitudes-asignacion")
    public ResponseEntity<SolicitudAsignacion> crearSolicitudAsignacion(@RequestParam("nifPaciente") String nifPaciente) {
        log.info("Creando solicitud de asignación para paciente NIF: {}", nifPaciente);
        
        try {
            SolicitudAsignacion solicitud = medicoFacade.crearSolicitudAsignacion(nifPaciente);
            if (solicitud == null) {
                log.warn("No se pudo crear solicitud para paciente NIF: {}", nifPaciente);
                return ResponseEntity.badRequest().build();
            }
            
            log.info("Solicitud de asignación creada exitosamente: ID {}", solicitud.getId());
            return ResponseEntity.ok(solicitud);
            
        } catch (com.hcc.tfm_hcc.exception.SolicitudExistenteException ex) {
            log.warn("Solicitud existente para paciente NIF: {}, mensaje: {}", nifPaciente, ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null); // Retornamos null en el body para mantener el tipo correcto
                    
        } catch (Exception ex) {
            log.error("Error al crear solicitud para paciente NIF: {}, error: {}", nifPaciente, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasRole('MEDICO')")
    @GetMapping("/solicitudes-asignacion/pendientes")
    public ResponseEntity<List<SolicitudAsignacion>> listarSolicitudesPendientes() {
        log.info("Listando solicitudes pendientes para médico autenticado");
        
        try {
            List<SolicitudAsignacion> lista = medicoFacade.listarSolicitudesPendientes();
            log.info("Se encontraron {} solicitudes pendientes", lista.size());
            return ResponseEntity.ok(lista);
            
        } catch (Exception e) {
            log.error("Error al listar solicitudes pendientes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasRole('MEDICO')")
    @GetMapping("/solicitudes-asignacion/enviadas")
    public ResponseEntity<List<SolicitudAsignacion>> listarSolicitudesEnviadas() {
        log.info("Listando solicitudes enviadas para médico autenticado");
        
        try {
            List<SolicitudAsignacion> lista = medicoFacade.listarSolicitudesEnviadas();
            log.info("Se encontraron {} solicitudes enviadas", lista.size());
            return ResponseEntity.ok(lista);
            
        } catch (Exception e) {
            log.error("Error al listar solicitudes enviadas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
