package com.hcc.tfm_hcc.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.dto.AnotacionMedicaDTO;
import com.hcc.tfm_hcc.dto.AnotacionMedicaRequestDTO;
import com.hcc.tfm_hcc.dto.ArchivoClinicoDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.dto.PropuestaCambioClinicoDTO;
import com.hcc.tfm_hcc.dto.PropuestaCambioClinicoRequestDTO;
import com.hcc.tfm_hcc.dto.SolicitudAsignacionDTO;
import org.springframework.web.bind.annotation.RequestBody;

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
    ResponseEntity<PacienteDTO> buscarPaciente(@RequestParam("dni") String dni,
                                               @RequestParam("fechaNacimiento") String fechaNacimiento);

    /**
     * Lista los pacientes con una relación activa con el médico autenticado.
     *
     * @return ResponseEntity con la lista de PacienteDTO de los pacientes asignados
     */
    ResponseEntity<List<PacienteDTO>> listarMisPacientes();

    /**
     * Obtiene el historial clínico de un paciente vinculado al médico autenticado.
     * Solo permite el acceso si existe una relación médico-paciente con estado "ACTIVA".
     *
     * @param nif NIF del paciente cuyo historial se consulta
     * @return ResponseEntity con el HistorialClinicoDTO del paciente
     */
    ResponseEntity<HistorialClinicoDTO> obtenerHistorialPaciente(@PathVariable("nif") String nif);

    /**
     * Crea una nueva solicitud de asignación entre el médico autenticado y un paciente.
     * La solicitud queda pendiente hasta que sea aprobada por el sistema o el paciente.
     * 
     * @param nifPaciente NIF del paciente al que se solicita asignación
     * @return ResponseEntity con la SolicitudAsignacionDTO creada
     * @throws IllegalArgumentException si el NIF del paciente no es válido
     * @throws IllegalStateException si ya existe una solicitud activa para este paciente
     */
    ResponseEntity<SolicitudAsignacionDTO> crearSolicitudAsignacion(@RequestParam("nifPaciente") String nifPaciente);

    /**
     * Lista todas las solicitudes de asignación pendientes que requieren
     * aprobación para el médico autenticado.
     *
     * @return ResponseEntity con lista de SolicitudAsignacionDTO con estado pendiente
     */
    ResponseEntity<List<SolicitudAsignacionDTO>> listarSolicitudesPendientes();

    /**
     * Lista todas las solicitudes de asignación enviadas por el médico autenticado,
     * independientemente de su estado actual (pendiente, aprobada, rechazada).
     *
     * @return ResponseEntity con lista de SolicitudAsignacionDTO enviadas por el médico
     */
    ResponseEntity<List<SolicitudAsignacionDTO>> listarSolicitudesEnviadas();

    /**
     * Escribe una anotación médica sobre un paciente vinculado al médico autenticado.
     * Solo se permite si existe una relación médico-paciente en estado "ACTIVA" y el
     * paciente no ha limitado el tratamiento de sus datos.
     *
     * @param nifPaciente NIF del paciente sobre el que se escribe la anotación
     * @param request cuerpo con el mensaje de la anotación
     * @return ResponseEntity con la AnotacionMedicaDTO creada
     */
    ResponseEntity<AnotacionMedicaDTO> crearAnotacion(@PathVariable("nif") String nifPaciente,
                                                      @RequestBody AnotacionMedicaRequestDTO request);

    /**
     * Lista las anotaciones que el médico autenticado ha escrito sobre un paciente
     * vinculado a él, de la más reciente a la más antigua. Solo se permite si existe
     * una relación médico-paciente en estado "ACTIVA" y el paciente no ha limitado el
     * tratamiento de sus datos.
     *
     * @param nifPaciente NIF del paciente
     * @return ResponseEntity con la lista de AnotacionMedicaDTO escritas por el médico
     */
    ResponseEntity<List<AnotacionMedicaDTO>> listarAnotaciones(@PathVariable("nif") String nifPaciente);

    /**
     * Lista los documentos clínicos de un paciente vinculado al médico autenticado.
     *
     * @param nif NIF del paciente
     * @return ResponseEntity con la lista de ArchivoClinicoDTO del paciente
     */
    ResponseEntity<List<ArchivoClinicoDTO>> listarArchivosPaciente(@PathVariable("nif") String nif);

    /**
     * Sube un documento clínico al historial de un paciente vinculado al médico autenticado.
     *
     * @param nif NIF del paciente
     * @param file documento multipart a subir
     * @return ResponseEntity con el ArchivoClinicoDTO del documento creado
     * @throws IOException si hay error al cifrar el contenido
     */
    ResponseEntity<ArchivoClinicoDTO> subirArchivoPaciente(@PathVariable("nif") String nif,
                                                           @RequestParam("file") MultipartFile file) throws IOException;

    /**
     * Descarga un documento clínico de un paciente vinculado al médico autenticado.
     *
     * @param nif NIF del paciente
     * @param id ID del documento
     * @return ResponseEntity con el Resource del documento
     */
    ResponseEntity<Resource> descargarArchivoPaciente(@PathVariable("nif") String nif, @PathVariable("id") UUID id);

    /**
     * Registra una propuesta de cambio (alta, edición o borrado) sobre el historial de un
     * paciente vinculado al médico autenticado. El cambio queda pendiente de que el paciente lo
     * confirme; no se aplica en el momento.
     *
     * @param nif NIF del paciente destinatario
     * @param request dominio, operación, recurso objetivo, motivo y datos propuestos
     * @return ResponseEntity con la PropuestaCambioClinicoDTO creada
     */
    ResponseEntity<PropuestaCambioClinicoDTO> proponerCambioClinico(@PathVariable("nif") String nif,
                                                                    @RequestBody PropuestaCambioClinicoRequestDTO request);

    /**
     * Lista las propuestas de cambio que el médico autenticado ha enviado sobre el historial de
     * un paciente vinculado a él, con su estado actual.
     *
     * @param nif NIF del paciente
     * @return ResponseEntity con la lista de PropuestaCambioClinicoDTO enviadas a ese paciente
     */
    ResponseEntity<List<PropuestaCambioClinicoDTO>> listarPropuestasCambio(@PathVariable("nif") String nif);
}
