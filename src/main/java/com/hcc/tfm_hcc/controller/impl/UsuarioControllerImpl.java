package com.hcc.tfm_hcc.controller.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hcc.tfm_hcc.constants.RestUrls;
import com.hcc.tfm_hcc.controller.UsuarioController;
import com.hcc.tfm_hcc.dto.ChangePasswordRequest;
import com.hcc.tfm_hcc.dto.NotificacionDTO;
import com.hcc.tfm_hcc.dto.UpdateUsuarioRequest;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.facade.NotificacionFacade;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.exception.UsuarioNoAutenticadoException;
import com.hcc.tfm_hcc.exception.UsuarioOperacionException;
import com.hcc.tfm_hcc.exception.UsuarioSinPermisoException;
import com.hcc.tfm_hcc.exception.UsuarioValidationException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del controlador REST para la gestión integral de usuarios.
 * Proporciona endpoints REST para operaciones de usuario incluyendo gestión de perfiles,
 * cambio de contraseñas, gestión de solicitudes y notificaciones.
 * 
 * <p>Características de seguridad:</p>
 * <ul>
 *   <li>Todos los endpoints requieren autenticación</li>
 *   <li>Validación de autorización mediante @PreAuthorize</li>
 *   <li>Logging detallado de operaciones de usuario</li>
 *   <li>Gestión centralizada de errores</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping(RestUrls.USUARIO_BASE)
@RequiredArgsConstructor
public class UsuarioControllerImpl implements UsuarioController {

    /** Facade para operaciones de usuario */
    private final UsuarioFacade usuarioFacade;
    
    /** Facade para operaciones de notificaciones */
    private final NotificacionFacade notificacionFacade;

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.USUARIO_NOMBRE)
    @PreAuthorize("isAuthenticated()")
    public String getNombreUsuario() {
        log.debug("Consultando nombre de usuario autenticado");
        return usuarioFacade.getNombreUsuario();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.USUARIO_ME)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioDTO> getUsuarioActual() {
        log.info("Consultando datos del usuario autenticado");
        
        try {
            UsuarioDTO dto = usuarioFacade.getUsuarioActual();
            if (dto == null) {
                log.warn("No se encontró usuario autenticado");
                throw new UsuarioNoAutenticadoException("Usuario no autenticado");
            }
            
            // Limpiar contraseña por seguridad
            if (dto.getPassword() != null) {
                dto.setPassword(null);
            }
            
            log.info("Datos de usuario obtenidos exitosamente: {}", dto.getNif());
            return ResponseEntity.ok(dto);
            
        } catch (Exception e) {
            log.error("Error al obtener datos del usuario autenticado: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error al obtener datos del usuario", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PutMapping(RestUrls.USUARIO_PASSWORD)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest changePassworRequest) {
        log.info("Solicitud de cambio de contraseña para usuario autenticado");
        
        try {
            if (changePassworRequest == null) {
                throw new UsuarioValidationException("El cuerpo de la solicitud es obligatorio");
            }

            // Validar entrada
            if (changePassworRequest.getNewPassword() == null || changePassworRequest.getNewPassword().length() < 6) {
                log.warn("Contraseña nueva demasiado corta");
                throw new UsuarioValidationException("Nueva contraseña demasiado corta");
            }
            
            usuarioFacade.changePassword(changePassworRequest.getCurrentPassword(), changePassworRequest.getNewPassword());
            log.info("Contraseña cambiada exitosamente");
            return ResponseEntity.ok("Contraseña actualizada exitosamente");
            
        } catch (UsuarioValidationException e) {
            log.warn("Error de validación al cambiar contraseña: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error interno al cambiar contraseña: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error al cambiar contraseña", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.USUARIO_SOLICITUDES)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SolicitudAsignacion>> listarMisSolicitudes() {
        log.info("Listando solicitudes para usuario autenticado");
        
        try {
            UsuarioDTO dto = usuarioFacade.getUsuarioActual();
            if (dto == null) {
                log.warn("Usuario no autenticado al listar solicitudes");
                throw new UsuarioNoAutenticadoException("Usuario no autenticado");
            }
            
            List<SolicitudAsignacion> solicitudes = usuarioFacade.listarMisSolicitudes();
            log.info("Se encontraron {} solicitudes para el usuario", solicitudes.size());
            return ResponseEntity.ok(solicitudes);
            
        } catch (Exception e) {
            log.error("Error al listar solicitudes del usuario: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error al listar solicitudes del usuario", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PutMapping(RestUrls.USUARIO_SOLICITUD_ID)
    @PreAuthorize("isAuthenticated()")
    // TODO: CORREGIR ESE REQUESTBODY Y HACER UN DTO
    public ResponseEntity<SolicitudAsignacion> actualizarEstadoSolicitud(@PathVariable("idSolicitud") String idSolicitud, 
                                                                          @RequestBody Map<String, String> body) {
        log.info("Actualizando estado de solicitud: {}", idSolicitud);
        
        try {
            UsuarioDTO dto = usuarioFacade.getUsuarioActual();
            if (dto == null) {
                log.warn("Usuario no autenticado al actualizar solicitud");
                throw new UsuarioNoAutenticadoException("Usuario no autenticado");
            }
            
            String nuevoEstado = body.get("estado");
            if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
                log.warn("Estado requerido no proporcionado");
                throw new UsuarioValidationException("Estado requerido no proporcionado");
            }
            
            SolicitudAsignacion updated = usuarioFacade.actualizarEstadoSolicitud(idSolicitud, nuevoEstado);
            log.info("Estado de solicitud actualizado exitosamente: {} -> {}", idSolicitud, nuevoEstado);
            return ResponseEntity.ok(updated);
            
        } catch (UsuarioValidationException e) {
            log.warn("Error de validación al actualizar solicitud: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Error de permisos al actualizar solicitud: {}", e.getMessage());
            return ResponseEntity.status(403).build();
        } catch (UsuarioSinPermisoException e) {
            log.warn("Error de permisos al actualizar solicitud: {}", e.getMessage());
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error("Error interno al actualizar solicitud: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error interno al actualizar solicitud", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.USUARIO_NOTIFICACIONES)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, NotificacionDTO>> listarMisNotificaciones(@RequestParam("page") int page,
                                                                        @RequestParam("size") int size) {
        log.info("Listando notificaciones para usuario autenticado - página: {}, tamaño: {}", page, size);
        
        try {
            UsuarioDTO dto = usuarioFacade.getUsuarioActual();
            if (dto == null) {
                log.warn("Usuario no autenticado al listar notificaciones");
                throw new UsuarioNoAutenticadoException("Usuario no autenticado");
            }
            
            Map<String, NotificacionDTO> resp = notificacionFacade.listarNotificacionesUsuarioActual(page, size);
            log.info("Notificaciones listadas exitosamente");
            return ResponseEntity.ok(resp);
            
        } catch (Exception e) {
            log.error("Error al listar notificaciones: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error al listar notificaciones", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping(RestUrls.USUARIO_NOTIFICACIONES_MARCAR_LEIDAS)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> marcarTodasNotificacionesLeidas() {
        log.info("Marcando todas las notificaciones como leídas");
        
        try {
            UsuarioDTO dto = usuarioFacade.getUsuarioActual();
            if (dto == null) {
                log.warn("Usuario no autenticado al marcar notificaciones");
                throw new UsuarioNoAutenticadoException("Usuario no autenticado");
            }
            
            notificacionFacade.marcarTodasComoLeidasUsuarioActual();
            log.info("Todas las notificaciones marcadas como leídas exitosamente");
            return ResponseEntity.ok("Notificaciones marcadas como leídas");
            
        } catch (Exception e) {
            log.error("Error al marcar notificaciones como leídas: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error marcando notificaciones", e);
        }
    }

    // Métodos adicionales útiles que no están en la interfaz principal pero son necesarios para el frontend

    /**
     * Actualiza los datos del usuario autenticado.
     * Endpoint adicional para operaciones de actualización de perfil.
     *
     * @param req UpdateUsuarioRequest con los datos a actualizar
     * @return ResponseEntity con el UsuarioDTO actualizado
     */
    @PutMapping(RestUrls.USUARIO_ME)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioDTO> updateUsuarioActual(@Valid @RequestBody UpdateUsuarioRequest req) {
        log.info("Actualizando datos del usuario autenticado");
        
        try {
            if (req == null) {
                throw new UsuarioValidationException("Los datos de usuario son obligatorios");
            }

            UsuarioDTO parcial = new UsuarioDTO();
            parcial.setNombre(req.getNombre());
            parcial.setApellido1(req.getApellido1());
            parcial.setApellido2(req.getApellido2());
            parcial.setNif(req.getNif());
            parcial.setEmail(req.getEmail());
            parcial.setTelefono(req.getTelefono());
            parcial.setFechaNacimiento(req.getFechaNacimiento());
            
            UsuarioDTO before = usuarioFacade.getUsuarioActual();
            String oldNif = before != null ? before.getNif() : null;
            
            UsuarioDTO actualizado = usuarioFacade.updateUsuarioActual(parcial);
            if (actualizado.getPassword() != null) actualizado.setPassword(null);
            
            boolean nifChanged = oldNif != null && actualizado.getNif() != null && !oldNif.equals(actualizado.getNif());
            ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
            if (nifChanged) {
                builder.header("X-Reauth-Required", "true");
            }
            
            log.info("Datos de usuario actualizados exitosamente: {}", actualizado.getNif());
            return builder.body(actualizado);
            
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al actualizar usuario: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error interno al actualizar usuario: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error interno al actualizar usuario", e);
        }
    }

    /**
     * Elimina la cuenta del usuario autenticado.
     * Operación irreversible que elimina todos los datos del usuario.
     *
     * @return ResponseEntity vacío confirmando la eliminación
     */
    @DeleteMapping(RestUrls.USUARIO_ME)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteCuenta() {
        log.info("Eliminando cuenta del usuario autenticado");
        
        try {
            usuarioFacade.deleteCuentaActual();
            log.info("Cuenta eliminada exitosamente");
            return ResponseEntity.noContent().build();
            
        } catch (IllegalStateException _) {
            log.warn("Usuario no autenticado al eliminar cuenta");
            return ResponseEntity.status(401).build();
        } catch (UsuarioNoAutenticadoException _) {
            log.warn("Usuario no autenticado al eliminar cuenta");
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("Error al eliminar cuenta: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error al eliminar cuenta", e);
        }
    }

    /**
     * Obtiene los logs de acceso del usuario autenticado.
     * Permite auditar la actividad del usuario en el sistema.
     *
     * @param desde Fecha de inicio del período (opcional)
     * @param hasta Fecha de fin del período (opcional)
     * @return ResponseEntity con la lista de logs de acceso
     */
    @GetMapping(RestUrls.USUARIO_LOGS)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> misLogs(@RequestParam(required = false) String desde, 
                                          @RequestParam(required = false) String hasta) {
        log.info("Consultando logs de acceso para usuario autenticado");
        
        try {
            UsuarioDTO dto = usuarioFacade.getUsuarioActual();
            if (dto == null) {
                throw new UsuarioNoAutenticadoException("Usuario no autenticado");
            }
            
            LocalDateTime d = null;
            LocalDateTime h = null;
            if (desde != null) {
                d = LocalDateTime.parse(desde); 
            }
            if (hasta != null) {
                h = LocalDateTime.parse(hasta); 
            }
            
            
            Object logs = usuarioFacade.getMisLogs(d, h);
            log.info("Logs consultados exitosamente");
            return ResponseEntity.ok(logs);
            
        } catch (Exception e) {
            log.error("Error al consultar logs: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error al consultar logs", e);
        }
    }

    /**
     * Cuenta las notificaciones no leídas del usuario autenticado.
     *
     * @return ResponseEntity con el conteo de notificaciones no leídas
     */
    @GetMapping(RestUrls.USUARIO_NOTIFICACIONES_NO_LEIDAS)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> contarNotificacionesNoLeidas() {
        log.debug("Contando notificaciones no leídas");
        
        try {
            UsuarioDTO dto = usuarioFacade.getUsuarioActual();
            if (dto == null) {
                throw new UsuarioNoAutenticadoException("Usuario no autenticado");
            }
            
            long count = notificacionFacade.contarNoLeidasUsuarioActual();
            return ResponseEntity.ok(Map.of("noLeidas", count));
            
        } catch (Exception e) {
            log.error("Error al contar notificaciones no leídas: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error al contar notificaciones no leídas", e);
        }
    }

    /**
     * Marca una notificación específica como leída.
     *
     * @param id ID de la notificación a marcar como leída
     * @return ResponseEntity confirmando la operación
     */
    @PutMapping(RestUrls.USUARIO_NOTIFICACION_LEIDA)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> marcarNotificacionLeida(@PathVariable("id") String id) {
        log.info("Marcando notificación como leída: {}", id);
        
        try {
            UsuarioDTO dto = usuarioFacade.getUsuarioActual();
            if (dto == null) {
                throw new UsuarioNoAutenticadoException("Usuario no autenticado");
            }
            
            notificacionFacade.marcarNotificacionComoLeida(id);
            log.info("Notificación marcada como leída: {}", id);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Error al marcar notificación como leída: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error al marcar notificación como leída", e);
        }
    }

    /**
     * Elimina una notificación específica del usuario.
     *
     * @param id ID de la notificación a eliminar
     * @return ResponseEntity confirmando la eliminación
     */
    @DeleteMapping(RestUrls.USUARIO_NOTIFICACION_ID)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable("id") String id) {
        log.info("Eliminando notificación: {}", id);
        
        try {
            UsuarioDTO dto = usuarioFacade.getUsuarioActual();
            if (dto == null) {
                throw new UsuarioNoAutenticadoException("Usuario no autenticado");
            }
            
            notificacionFacade.eliminarNotificacionUsuarioActual(id);
            log.info("Notificación eliminada: {}", id);
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            log.error("Error al eliminar notificación: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error al eliminar notificación", e);
        }
    }

    /**
     * Exporta todos los datos del usuario autenticado.
     * Funcionalidad para cumplimiento RGPD.
     *
     * @return ResponseEntity con los datos exportados del usuario
     */
    @GetMapping(RestUrls.USUARIO_EXPORT)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> exportUsuario() {
        log.info("Exportando datos del usuario autenticado");
        
        try {
            UsuarioDTO dto = usuarioFacade.getUsuarioActual();
            if (dto == null) {
                throw new UsuarioNoAutenticadoException("Usuario no autenticado");
            }
            
            Object exportData = usuarioFacade.exportUsuario();
            log.info("Datos de usuario exportados exitosamente");
            return ResponseEntity.ok(exportData);
            
        } catch (Exception e) {
            log.error("Error al exportar datos del usuario: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error al exportar datos del usuario", e);
        }
    }
}
