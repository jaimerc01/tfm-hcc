package com.hcc.tfm_hcc.service;

import java.time.LocalDateTime;
import java.util.List;

import com.hcc.tfm_hcc.dto.UserExportDTO;
import com.hcc.tfm_hcc.dto.UserExportDTO.AccesoDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.model.Usuario;

/**
 * Servicio para la gestión integral de usuarios en el sistema HCC.
 * Proporciona operaciones para el registro, autenticación, gestión de perfil,
 * control de accesos y exportación de datos de usuarios.
 * 
 * <p>Este servicio maneja la lógica de negocio relacionada con:</p>
 * <ul>
 *   <li>Registro y alta de nuevos usuarios</li>
 *   <li>Gestión de perfiles y datos personales</li>
 *   <li>Cambio de contraseñas y seguridad</li>
 *   <li>Control de accesos y auditoría</li>
 *   <li>Exportación de datos personales (GDPR)</li>
 *   <li>Gestión de solicitudes médico-paciente</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface UsuarioService {
    
    /**
     * Registra un nuevo usuario en el sistema.
     * Crea una cuenta de usuario con los datos proporcionados y realiza
     * todas las validaciones necesarias antes del registro.
     *
     * @param usuarioDTO Datos del usuario a registrar incluyendo información personal y credenciales
     * @return Usuario creado con ID asignado y configuración inicial
     * @throws IllegalArgumentException si los datos del usuario son inválidos o incompletos
     * @throws RuntimeException si ya existe un usuario con el mismo NIF o email
     */
    Usuario altaUsuario(UsuarioDTO usuarioDTO);

    /**
     * Obtiene el nombre completo del usuario autenticado actualmente.
     *
     * @return Nombre completo del usuario autenticado, o null si no hay sesión activa
     * @throws RuntimeException si hay error en la autenticación
     */
    String getNombreUsuario();

    /**
     * Obtiene el DTO completo del usuario autenticado actualmente.
     * Incluye toda la información del perfil del usuario sin datos sensibles.
     * 
     * @return UsuarioDTO con los datos del usuario autenticado, o null si no hay sesión activa
     * @throws RuntimeException si hay error en la autenticación o acceso a datos
     */
    UsuarioDTO getUsuarioActual();

    /**
     * Cambia la contraseña del usuario autenticado.
     * Valida la contraseña actual antes de establecer la nueva.
     * 
     * @param currentPassword Contraseña actual del usuario para verificación
     * @param newPassword Nueva contraseña que debe cumplir las políticas de seguridad
     * @throws IllegalArgumentException si la contraseña actual es incorrecta o la nueva no cumple requisitos
     * @throws RuntimeException si el usuario no está autenticado
     */
    void changePassword(String currentPassword, String newPassword);

    /**
     * Actualiza los datos personales del usuario autenticado.
     * No permite modificar credenciales de acceso ni información crítica.
     * 
     * @param parcial UsuarioDTO con los campos a actualizar (campos nulos se ignoran)
     * @return UsuarioDTO con los datos actualizados del usuario
     * @throws IllegalArgumentException si los datos proporcionados son inválidos
     * @throws RuntimeException si el usuario no está autenticado
     */
    UsuarioDTO updateUsuarioActual(UsuarioDTO parcial);

    /**
     * Elimina permanentemente la cuenta del usuario autenticado.
     * Esta operación es irreversible y elimina todos los datos asociados.
     * 
     * @throws RuntimeException si el usuario no está autenticado o hay error en la eliminación
     */
    void deleteCuentaActual();

    /**
     * Obtiene los registros de acceso del usuario autenticado.
     * Permite filtrar por rango de fechas para auditorías específicas.
     * 
     * @param desde Fecha y hora de inicio del filtro (opcional, null para sin límite inferior)
     * @param hasta Fecha y hora de fin del filtro (opcional, null para sin límite superior)
     * @return Lista de AccesoDTO con los registros de acceso del usuario en el rango especificado
     * @throws RuntimeException si el usuario no está autenticado
     */
    List<AccesoDTO> getMisLogs(LocalDateTime desde, LocalDateTime hasta);

    /**
     * Genera una exportación completa de los datos del usuario autenticado.
     * Cumple con los requisitos de portabilidad de datos (GDPR Art. 20).
     * 
     * @return UserExportDTO con todos los datos del usuario en formato exportable
     * @throws RuntimeException si el usuario no está autenticado o hay error en la exportación
     */
    UserExportDTO exportUsuario();

    /**
     * Lista todas las solicitudes de asignación médico-paciente recibidas por el usuario.
     * Solo disponible para usuarios con perfil de paciente.
     * 
     * @return Lista de SolicitudAsignacion dirigidas al usuario autenticado
     * @throws RuntimeException si el usuario no está autenticado o no tiene perfil de paciente
     */
    List<SolicitudAsignacion> listarMisSolicitudes();
    
    /**
     * Actualiza el estado de una solicitud de asignación médico-paciente.
     * Permite al paciente aceptar o rechazar solicitudes de médicos.
     * 
     * @param solicitudId ID único de la solicitud a actualizar
     * @param nuevoEstado Nuevo estado de la solicitud (ACEPTADA, RECHAZADA)
     * @return SolicitudAsignacion actualizada con el nuevo estado
     * @throws IllegalArgumentException si el ID o estado son inválidos
     * @throws RuntimeException si el usuario no está autenticado o no tiene permisos sobre la solicitud
     */
    SolicitudAsignacion actualizarEstadoSolicitud(String solicitudId, String nuevoEstado);
}
