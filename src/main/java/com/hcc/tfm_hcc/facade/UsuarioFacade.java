package com.hcc.tfm_hcc.facade;

import java.time.LocalDateTime;
import java.util.List;

import com.hcc.tfm_hcc.dto.UserExportDTO;
import com.hcc.tfm_hcc.dto.UserExportDTO.AccesoDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;

/**
 * Facade para la gestión integral de usuarios en el sistema HCC.
 * Proporciona operaciones completas para el manejo de cuentas de usuario,
 * incluyendo registro, autenticación, gestión de perfiles, control de accesos
 * y exportación de datos personales conforme a normativas de protección de datos.
 * 
 * <p>Funcionalidades principales:</p>
 * <ul>
 *   <li>Alta y gestión de cuentas de usuario</li>
 *   <li>Operaciones de perfil y datos personales</li>
 *   <li>Gestión de contraseñas y seguridad</li>
 *   <li>Auditoría de accesos y logs de usuario</li>
 *   <li>Exportación de datos conforme RGPD</li>
 *   <li>Gestión de solicitudes de asignación médico-paciente</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface UsuarioFacade {
    
    /**
     * Da de alta un nuevo usuario en el sistema con validación completa.
     * Incluye verificación de datos, validación de NIF y asignación de perfil.
     *
     * @param usuarioDTO Datos del usuario a registrar en el sistema
     * @return UsuarioDTO del usuario registrado con ID asignado
     * @throws IllegalArgumentException si los datos del usuario no son válidos
     * @throws IllegalStateException si el usuario ya existe en el sistema
     */
    UsuarioDTO altaUsuario(UsuarioDTO usuarioDTO) throws IllegalArgumentException, IllegalStateException;

    /**
     * Obtiene el nombre completo del usuario autenticado actualmente.
     *
     * @return Nombre del usuario autenticado
     * @throws SecurityException si no hay usuario autenticado
     */
    String getNombreUsuario() throws SecurityException;

    /**
     * Obtiene los datos completos del usuario autenticado.
     * Incluye información personal, perfil y preferencias del usuario.
     * 
     * @return UsuarioDTO con todos los datos del usuario actual
     * @throws SecurityException si no hay usuario autenticado
     */
    UsuarioDTO getUsuarioActual() throws SecurityException;

    /**
     * Cambia la contraseña del usuario autenticado.
     * Requiere verificación de la contraseña actual antes del cambio.
     * 
     * @param currentPassword Contraseña actual del usuario para verificación
     * @param newPassword Nueva contraseña que cumple con políticas de seguridad
     * @throws IllegalArgumentException si la contraseña actual es incorrecta
     * @throws IllegalStateException si la nueva contraseña no cumple los requisitos
     */
    void changePassword(String currentPassword, String newPassword) throws IllegalArgumentException, IllegalStateException;

    /**
     * Actualiza los campos editables del perfil del usuario autenticado.
     * Solo actualiza los campos que el usuario tiene permiso para modificar.
     * 
     * @param parcial UsuarioDTO con los campos a actualizar
     * @return UsuarioDTO actualizado con los nuevos datos
     * @throws IllegalArgumentException si los datos proporcionados no son válidos
     */
    UsuarioDTO updateUsuarioActual(UsuarioDTO parcial) throws IllegalArgumentException;

    /**
     * Elimina permanentemente la cuenta del usuario autenticado.
     * Esta operación es irreversible y elimina todos los datos asociados.
     * 
     * @throws SecurityException si el usuario no tiene permisos para eliminar su cuenta
     */
    void deleteCuentaActual() throws SecurityException;

    /**
     * Obtiene los logs de acceso del usuario autenticado en un rango de fechas.
     * Permite auditar la actividad del usuario en el sistema.
     * 
     * @param desde Fecha y hora de inicio del período de consulta (puede ser null)
     * @param hasta Fecha y hora de fin del período de consulta (puede ser null)
     * @return Lista de AccesoDTO con los logs de acceso del usuario
     */
    List<AccesoDTO> getMisLogs(LocalDateTime desde, LocalDateTime hasta);

    /**
     * Genera una exportación completa de todos los datos del usuario autenticado.
     * Incluye datos personales, logs de acceso y actividad, conforme a RGPD.
     * 
     * @return UserExportDTO con toda la información exportable del usuario
     */
    UserExportDTO exportUsuario();

    /**
     * Lista todas las solicitudes de asignación médico-paciente del usuario autenticado.
     * Incluye solicitudes recibidas y enviadas, independientemente de su estado.
     * 
     * @return Lista de SolicitudAsignacion relacionadas con el usuario actual
     */
    List<SolicitudAsignacion> listarMisSolicitudes();
    
    /**
     * Actualiza el estado de una solicitud de asignación específica.
     * Solo el usuario receptor de la solicitud puede cambiar su estado.
     * 
     * @param solicitudId ID único de la solicitud a actualizar
     * @param nuevoEstado Nuevo estado para la solicitud (PENDIENTE, APROBADA, RECHAZADA)
     * @return SolicitudAsignacion actualizada con el nuevo estado
     * @throws IllegalArgumentException si el estado especificado no es válido
     * @throws SecurityException si el usuario no tiene permisos para actualizar la solicitud
     */
    SolicitudAsignacion actualizarEstadoSolicitud(String solicitudId, String nuevoEstado) throws IllegalArgumentException, SecurityException;
}
