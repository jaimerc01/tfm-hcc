package com.hcc.tfm_hcc.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;

/**
 * Controlador REST para operaciones médicas en el sistema HCC.
 * Proporciona endpoints específicos para médicos incluyendo búsqueda de pacientes
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
 * <p>Todos los endpoints requieren autenticación y rol de médico.</p>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface MedicoController {
    
    /**
     * Busca un paciente específico utilizando su DNI y fecha de nacimiento.
     * Esta operación permite verificar la identidad del paciente antes de
     * proceder con solicitudes de asignación o consultas médicas.
     * 
     * @param dni Documento Nacional de Identidad del paciente
     * @param fechaNacimiento Fecha de nacimiento del paciente en formato string
     * @return ResponseEntity con el PacienteDTO del paciente encontrado
     * @throws IllegalArgumentException si los datos de identificación no son válidos
     */
    @GetMapping("/pacientes/buscar")
    ResponseEntity<PacienteDTO> buscarPaciente(@RequestParam("dni") String dni, 
                                               @RequestParam("fechaNacimiento") String fechaNacimiento);
    
    /**
     * Crea una nueva solicitud de asignación entre el médico autenticado y un paciente.
     * La solicitud queda pendiente hasta que sea aprobada por el sistema o el paciente.
     * 
     * @param nifPaciente NIF del paciente al que se solicita asignación
     * @return ResponseEntity con la SolicitudAsignacion creada
     * @throws IllegalArgumentException si el NIF del paciente no es válido
     * @throws IllegalStateException si ya existe una solicitud activa para este paciente
     */
    @PostMapping("/solicitudes-asignacion")
    ResponseEntity<SolicitudAsignacion> crearSolicitudAsignacion(@RequestParam("nifPaciente") String nifPaciente);
    
    /**
     * Lista todas las solicitudes de asignación pendientes que requieren
     * aprobación para el médico autenticado.
     * 
     * @return ResponseEntity con lista de SolicitudAsignacion con estado pendiente
     */
    @GetMapping("/solicitudes-asignacion/pendientes")
    ResponseEntity<List<SolicitudAsignacion>> listarSolicitudesPendientes();
    
    /**
     * Lista todas las solicitudes de asignación enviadas por el médico autenticado,
     * independientemente de su estado actual (pendiente, aprobada, rechazada).
     * 
     * @return ResponseEntity con lista de SolicitudAsignacion enviadas por el médico
     */
    @GetMapping("/solicitudes-asignacion/enviadas")
    ResponseEntity<List<SolicitudAsignacion>> listarSolicitudesEnviadas();
}
