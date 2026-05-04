package com.hcc.tfm_hcc.service;

import java.util.List;
import java.util.UUID;

import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;

/**
 * Servicio para la gestión de médicos en el sistema HCC.
 * Proporciona operaciones CRUD para médicos, búsqueda de pacientes,
 * gestión de solicitudes de asignación y administración de perfiles médicos.
 * 
 * <p>Este servicio maneja la lógica de negocio relacionada con:</p>
 * <ul>
 *   <li>Administración de usuarios con perfil médico</li>
 *   <li>Búsqueda y gestión de pacientes</li>
 *   <li>Solicitudes de asignación médico-paciente</li>
 *   <li>Perfiles y permisos médicos</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface MedicoService {
    
    /**
     * Lista todos los usuarios con perfil de médico en el sistema.
     * 
     * @return Lista de UsuarioDTO con información de todos los médicos
     */
    List<UsuarioDTO> listarMedicos();
    
    /**
     * Crea un nuevo usuario con perfil de médico en el sistema.
     * 
     * @param medicoDTO Datos del médico a crear
     * @return UsuarioDTO con la información del médico creado
     * @throws IllegalArgumentException si los datos del médico son inválidos
     */
    UsuarioDTO crearMedico(UsuarioDTO medicoDTO);
    
    /**
     * Actualiza la información de un médico existente.
     * 
     * @param id ID único del médico a actualizar
     * @param medicoDTO Nuevos datos del médico
     * @return UsuarioDTO con la información actualizada del médico
     * @throws IllegalArgumentException si el ID es inválido o el médico no existe
     */
    UsuarioDTO actualizarMedico(UUID id, UsuarioDTO medicoDTO);
    
    /**
     * Elimina un médico del sistema.
     * 
     * @param id ID único del médico a eliminar
     * @throws IllegalArgumentException si el ID es inválido o el médico no existe
     */
    void eliminarMedico(UUID id);

    /**
     * Busca un paciente por su DNI y fecha de nacimiento.
     * 
     * @param dni Documento Nacional de Identidad del paciente
     * @param fechaNacimiento Fecha de nacimiento del paciente en formato ISO (yyyy-MM-dd)
     * @return PacienteDTO con la información del paciente encontrado
     * @throws IllegalArgumentException si el DNI o fecha de nacimiento son inválidos
     * @throws RuntimeException si el paciente no es encontrado
     */
    PacienteDTO buscarPacientePorDniYFechaNacimiento(String dni, String fechaNacimiento);
    
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
     * Lista todas las solicitudes de asignación pendientes de aprobación.
     * 
     * @return Lista de SolicitudAsignacion con estado pendiente
     */
    List<SolicitudAsignacion> listarSolicitudesPendientes();
    
    /**
     * Lista todas las solicitudes de asignación enviadas por el médico autenticado.
     * 
     * @return Lista de SolicitudAsignacion enviadas por el médico actual
     * @throws RuntimeException si el médico no está autenticado
     */
    List<SolicitudAsignacion> listarSolicitudesEnviadas();
    
    /**
     * Asigna o revoca el perfil de médico a un usuario existente.
     * 
     * @param id ID único del usuario al cual asignar o revocar el perfil médico
     * @param asignar true para asignar el perfil médico, false para revocarlo
     * @throws IllegalArgumentException si el ID es inválido o el usuario no existe
     */
    void setPerfilMedico(UUID id, boolean asignar);
}
