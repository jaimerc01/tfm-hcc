package com.hcc.tfm_hcc.facade.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.dto.UserExportDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.service.UsuarioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación de la fachada para operaciones de usuario.
 * 
 * <p>Esta clase actúa como una capa de fachada entre los controladores y los servicios
 * de usuario, proporcionando una interfaz simplificada para las operaciones relacionadas
 * con la gestión de usuarios del sistema.</p>
 * 
 * <p>Funcionalidades principales:</p>
 * <ul>
 *   <li>Gestión de datos de usuario (alta, actualización, eliminación)</li>
 *   <li>Cambio de contraseñas</li>
 *   <li>Exportación de datos de usuario</li>
 *   <li>Gestión de logs de acceso</li>
 *   <li>Gestión de solicitudes de asignación</li>
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
@Component
@RequiredArgsConstructor
public class UsuarioFacadeImpl implements UsuarioFacade {

    // ===============================
    // DEPENDENCIAS INYECTADAS
    // ===============================
    
    /**
     * Servicio para operaciones de usuario.
     */
    private final UsuarioService usuarioService;
    
    /**
     * Mapper para conversión entre entidades y DTOs de usuario.
     */
    private final UsuarioMapper usuarioMapper;

    // ===============================
    // MÉTODOS DE GESTIÓN DE USUARIOS
    // ===============================
    
    /**
     * Realiza el alta de un nuevo usuario en el sistema.
     * 
     * @param usuarioDTO Los datos del usuario a crear
     * @return UsuarioDTO El usuario creado con su información actualizada
     * @throws IllegalArgumentException Si los datos del usuario son inválidos
     * @throws RuntimeException Si ocurre un error durante la creación
     */
    @Override
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public UsuarioDTO altaUsuario(UsuarioDTO usuarioDTO) {
        log.debug("Iniciando alta de usuario: {}", 
                usuarioDTO != null ? usuarioDTO.getNif() : "null");
        
        try {
            validarDatosUsuario(usuarioDTO);
            
            UsuarioDTO resultado = usuarioMapper.toDto(usuarioService.altaUsuario(usuarioDTO));
            
            log.info("Usuario dado de alta exitosamente: {}", 
                    resultado != null ? resultado.getNif() : "unknown");
            return resultado;
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación en alta de usuario: {} - Error: {}", 
                    usuarioDTO != null ? usuarioDTO.getNif() : "null", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado durante alta de usuario: {} - Error: {}", 
                     usuarioDTO != null ? usuarioDTO.getNif() : "null", e.getMessage(), e);
            throw new RuntimeException("Error interno durante el alta de usuario", e);
        }
    }

    /**
     * Obtiene el nombre del usuario autenticado actualmente.
     * 
     * @return String El nombre del usuario actual
     * @throws RuntimeException Si no se puede obtener el usuario actual
     */
    @Override
    public String getNombreUsuario() {
        log.debug("Obteniendo nombre del usuario autenticado");
        
        try {
            String nombre = usuarioService.getNombreUsuario();
            log.debug("Nombre de usuario obtenido: {}", nombre != null ? nombre : "null");
            return nombre;
        } catch (Exception e) {
            log.error("Error al obtener nombre del usuario: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener información del usuario", e);
        }
    }

    /**
     * Obtiene los datos completos del usuario autenticado actualmente.
     * 
     * @return UsuarioDTO Los datos del usuario actual
     * @throws RuntimeException Si no se puede obtener el usuario actual
     */
    @Override
    public UsuarioDTO getUsuarioActual() {
        log.debug("Obteniendo datos del usuario autenticado");
        
        try {
            UsuarioDTO usuario = usuarioService.getUsuarioActual();
            log.debug("Datos de usuario obtenidos: {}", 
                    usuario != null ? usuario.getNif() : "null");
            return usuario;
        } catch (Exception e) {
            log.error("Error al obtener datos del usuario actual: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener información del usuario", e);
        }
    }

    /**
     * Cambia la contraseña del usuario autenticado actualmente.
     * 
     * @param currentPassword La contraseña actual
     * @param newPassword La nueva contraseña
     * @throws IllegalArgumentException Si las contraseñas son inválidas
     * @throws RuntimeException Si ocurre un error durante el cambio
     */
    @Override
    public void changePassword(String currentPassword, String newPassword) {
        log.debug("Iniciando cambio de contraseña para usuario autenticado");
        
        try {
            validarDatosPassword(currentPassword, newPassword);
            
            usuarioService.changePassword(currentPassword, newPassword);
            
            log.info("Contraseña cambiada exitosamente para usuario autenticado");
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación en cambio de contraseña: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado durante cambio de contraseña: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno durante el cambio de contraseña", e);
        }
    }

    /**
     * Actualiza los datos del usuario autenticado actualmente.
     * 
     * @param parcial Los datos parciales a actualizar
     * @return UsuarioDTO Los datos actualizados del usuario
     * @throws IllegalArgumentException Si los datos son inválidos
     * @throws RuntimeException Si ocurre un error durante la actualización
     */
    @Override
    public UsuarioDTO updateUsuarioActual(UsuarioDTO parcial) {
        log.debug("Iniciando actualización de datos del usuario autenticado");
        
        try {
            if (parcial == null) {
                throw new IllegalArgumentException("Los datos de actualización no pueden ser nulos");
            }
            
            UsuarioDTO resultado = usuarioService.updateUsuarioActual(parcial);
            
            log.info("Datos de usuario actualizados exitosamente: {}", 
                    resultado != null ? resultado.getNif() : "unknown");
            return resultado;
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación en actualización de usuario: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado durante actualización de usuario: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno durante la actualización de usuario", e);
        }
    }

    /**
     * Elimina la cuenta del usuario autenticado actualmente.
     * 
     * @throws RuntimeException Si ocurre un error durante la eliminación
     */
    @Override
    public void deleteCuentaActual() {
        log.debug("Iniciando eliminación de cuenta del usuario autenticado");
        
        try {
            usuarioService.deleteCuentaActual();
            
            log.info("Cuenta de usuario eliminada exitosamente");
        } catch (Exception e) {
            log.error("Error inesperado durante eliminación de cuenta: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno durante la eliminación de cuenta", e);
        }
    }

    // ===============================
    // MÉTODOS DE LOGS Y EXPORTACIÓN
    // ===============================

    /**
     * Obtiene los logs de acceso del usuario autenticado en un rango de fechas.
     * 
     * @param desde Fecha de inicio del rango
     * @param hasta Fecha de fin del rango
     * @return List<UserExportDTO.AccesoDTO> Lista de logs de acceso
     * @throws IllegalArgumentException Si las fechas son inválidas
     * @throws RuntimeException Si ocurre un error durante la consulta
     */
    @Override
    public List<UserExportDTO.AccesoDTO> getMisLogs(LocalDateTime desde, LocalDateTime hasta) {
        log.debug("Obteniendo logs de acceso para usuario autenticado - Desde: {}, Hasta: {}", 
                desde, hasta);
        
        try {
            validarRangoFechas(desde, hasta);
            
            List<UserExportDTO.AccesoDTO> logs = usuarioService.getMisLogs(desde, hasta);
            
            log.info("Logs de acceso obtenidos: {} registros", 
                    logs != null ? logs.size() : 0);
            return logs;
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación en consulta de logs: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado durante consulta de logs: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno durante la consulta de logs", e);
        }
    }

    /**
     * Exporta todos los datos del usuario autenticado.
     * 
     * @return UserExportDTO Los datos completos del usuario para exportación
     * @throws RuntimeException Si ocurre un error durante la exportación
     */
    @Override
    public UserExportDTO exportUsuario() {
        log.debug("Iniciando exportación de datos del usuario autenticado");
        
        try {
            UserExportDTO exportData = usuarioService.exportUsuario();
            
            log.info("Datos de usuario exportados exitosamente");
            return exportData;
        } catch (Exception e) {
            log.error("Error inesperado durante exportación de usuario: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno durante la exportación de datos", e);
        }
    }

    // ===============================
    // MÉTODOS DE SOLICITUDES
    // ===============================

    /**
     * Lista todas las solicitudes de asignación del usuario autenticado.
     * 
     * @return List<SolicitudAsignacion> Lista de solicitudes del usuario
     * @throws RuntimeException Si ocurre un error durante la consulta
     */
    @Override
    public List<SolicitudAsignacion> listarMisSolicitudes() {
        log.debug("Obteniendo solicitudes de asignación del usuario autenticado");
        
        try {
            List<SolicitudAsignacion> solicitudes = usuarioService.listarMisSolicitudes();
            
            log.info("Solicitudes obtenidas: {} registros", 
                    solicitudes != null ? solicitudes.size() : 0);
            return solicitudes;
        } catch (Exception e) {
            log.error("Error inesperado durante consulta de solicitudes: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno durante la consulta de solicitudes", e);
        }
    }

    /**
     * Actualiza el estado de una solicitud de asignación.
     * 
     * @param solicitudId ID de la solicitud a actualizar
     * @param nuevoEstado Nuevo estado para la solicitud
     * @return SolicitudAsignacion La solicitud actualizada
     * @throws IllegalArgumentException Si los parámetros son inválidos
     * @throws RuntimeException Si ocurre un error durante la actualización
     */
    @Override
    public SolicitudAsignacion actualizarEstadoSolicitud(String solicitudId, String nuevoEstado) {
        log.debug("Actualizando estado de solicitud: {} -> {}", solicitudId, nuevoEstado);
        
        try {
            validarDatosSolicitud(solicitudId, nuevoEstado);
            
            SolicitudAsignacion resultado = usuarioService.actualizarEstadoSolicitud(solicitudId, nuevoEstado);
            
            log.info("Estado de solicitud actualizado exitosamente: {} -> {}", 
                    solicitudId, nuevoEstado);
            return resultado;
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación en actualización de solicitud: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado durante actualización de solicitud: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno durante la actualización de solicitud", e);
        }
    }

    // ===============================
    // MÉTODOS UTILITARIOS PRIVADOS
    // ===============================

    /**
     * Valida los datos básicos de un usuario.
     * 
     * @param usuarioDTO Los datos del usuario a validar
     * @throws IllegalArgumentException Si los datos son inválidos
     */
    private void validarDatosUsuario(UsuarioDTO usuarioDTO) {
        if (usuarioDTO == null) {
            throw new IllegalArgumentException("Los datos del usuario no pueden ser nulos");
        }
        
        if (usuarioDTO.getNif() == null || usuarioDTO.getNif().trim().isEmpty()) {
            throw new IllegalArgumentException("El NIF del usuario es obligatorio");
        }
    }

    /**
     * Valida los datos para cambio de contraseña.
     * 
     * @param currentPassword Contraseña actual
     * @param newPassword Nueva contraseña
     * @throws IllegalArgumentException Si las contraseñas son inválidas
     */
    private void validarDatosPassword(String currentPassword, String newPassword) {
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña actual es obligatoria");
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La nueva contraseña es obligatoria");
        }
        
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres");
        }
    }

    /**
     * Valida un rango de fechas.
     * 
     * @param desde Fecha de inicio
     * @param hasta Fecha de fin
     * @throws IllegalArgumentException Si las fechas son inválidas
     */
    private void validarRangoFechas(LocalDateTime desde, LocalDateTime hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }

    /**
     * Valida los datos para actualización de solicitud.
     * 
     * @param solicitudId ID de la solicitud
     * @param nuevoEstado Nuevo estado
     * @throws IllegalArgumentException Si los datos son inválidos
     */
    private void validarDatosSolicitud(String solicitudId, String nuevoEstado) {
        if (solicitudId == null || solicitudId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la solicitud es obligatorio");
        }
        
        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            throw new IllegalArgumentException("El nuevo estado es obligatorio");
        }
    }
}
