package com.hcc.tfm_hcc.facade.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.facade.NotificacionFacade;
import com.hcc.tfm_hcc.model.Notificacion;
import com.hcc.tfm_hcc.service.NotificacionService;
import com.hcc.tfm_hcc.exception.NotificacionValidationException;
import com.hcc.tfm_hcc.exception.NotificacionOperacionException;
import com.hcc.tfm_hcc.converter.NotificacionConverter;
import com.hcc.tfm_hcc.dto.NotificacionDTO;
import com.hcc.tfm_hcc.exception.NotificacionAccesoException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación de la fachada para operaciones de notificación.
 * 
 * <p>Esta clase actúa como una capa de fachada entre los controladores y los servicios
 * de notificación, proporcionando una interfaz simplificada para las operaciones relacionadas
 * con la gestión de notificaciones del sistema.</p>
 * 
 * <p>Funcionalidades principales:</p>
 * <ul>
 *   <li>Listado de notificaciones por usuario</li>
 *   <li>Marcado de notificaciones como leídas</li>
 *   <li>Eliminación de notificaciones</li>
 *   <li>Paginación de notificaciones</li>
 *   <li>Conteo de notificaciones no leídas</li>
 *   <li>Generación automática de enlaces según el tipo de notificación</li>
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
public class NotificacionFacadeImpl implements NotificacionFacade {

    // ===============================
    // DEPENDENCIAS INYECTADAS
    // ===============================
    
    /**
     * Servicio para operaciones de notificación.
     */
    private final NotificacionService notificacionService;
    private final NotificacionConverter notificacionConverter;

    // ===============================
    // MÉTODOS DE LISTADO DE NOTIFICACIONES
    // ===============================

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public List<NotificacionDTO> listarNotificacionesUsuarioActual() {
        log.debug("Obteniendo todas las notificaciones del usuario autenticado");
        
        try {
            List<Notificacion> listaNotificaciones = notificacionService.listarNotificacionesUsuarioActual();
            List<NotificacionDTO> listaNotificacionesDTO = notificacionConverter.toDtoList(listaNotificaciones);

            log.info("Notificaciones obtenidas: {} registros", listaNotificacionesDTO.size());
            return listaNotificacionesDTO;
        } catch (NotificacionValidationException e) {
            log.warn("Validación al listar notificaciones: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al obtener notificaciones del usuario: {}", e.getMessage(), e);
            throw new NotificacionOperacionException("Error interno durante la consulta de notificaciones", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificacionDTO crearNotificacionParaUsuario(String usuarioNif, String mensaje) {
        log.debug("Creando notificación para usuario NIF: {} - mensaje: {}", usuarioNif, mensaje);
        try {
            Notificacion notificacion = notificacionService.crearNotificacionParaUsuario(usuarioNif, mensaje);
            NotificacionDTO notificacionDTO = notificacionConverter.toDto(notificacion);
            log.info("Notificación creada para NIF: {}", usuarioNif);
            return notificacionDTO;
        } catch (IllegalArgumentException e) {
            log.warn("Validación al crear notificación para {}: {}", usuarioNif, e.getMessage());
            throw new NotificacionValidationException(e.getMessage(), e);
        } catch (SecurityException e) {
            log.warn("Acceso denegado al crear notificación para {}: {}", usuarioNif, e.getMessage());
            throw new NotificacionAccesoException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al crear notificación para {}: {}", usuarioNif, e.getMessage(), e);
            throw new NotificacionOperacionException("Error interno al crear notificación", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public Map<String, NotificacionDTO> listarNotificacionesUsuarioActual(int page, int size) {
        log.debug("Obteniendo notificaciones paginadas del usuario autenticado - Página: {}, Tamaño: {}", 
                page, size);
        
        try {
            validarParametrosPaginacion(page, size);
            
            Page<Notificacion> pageNotificaciones = notificacionService.listarNotificacionesUsuarioActual(page, size);
            
            List<NotificacionDTO> items = notificacionConverter.toDtoList(pageNotificaciones.getContent());
            Map<String, NotificacionDTO> resultado = items.stream()
                    .collect(Collectors.toMap(NotificacionDTO::getId, n -> n));
            
            log.info("Notificaciones paginadas obtenidas: {} de {} registros", 
                    items.size(), pageNotificaciones.getTotalElements());
            return resultado;
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación en paginación de notificaciones: {}", e.getMessage());
            throw new NotificacionValidationException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al obtener notificaciones paginadas: {}", e.getMessage(), e);
            throw new NotificacionOperacionException("Error interno durante la consulta de notificaciones", e);
        }
    }

    // ===============================
    // MÉTODOS DE GESTIÓN DE ESTADO
    // ===============================

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public List<NotificacionDTO> marcarTodasComoLeidasUsuarioActual() {
        log.debug("Marcando todas las notificaciones como leídas para usuario autenticado");
        
        try {
            List<Notificacion> notificaciones = notificacionService.marcarTodasComoLeidasUsuarioActual();
            List<NotificacionDTO> notificacionesDTO = notificacionConverter.toDtoList(notificaciones);
            
            log.info("Todas las notificaciones marcadas como leídas exitosamente");
            return notificacionesDTO;
        } catch (Exception e) {
            log.error("Error inesperado al marcar notificaciones como leídas: {}", e.getMessage(), e);
            throw new NotificacionOperacionException("Error interno al marcar notificaciones como leídas", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public NotificacionDTO marcarNotificacionComoLeida(String id) {
        log.debug("Marcando notificación como leída: {}", id);
        
        try {
            validarIdNotificacion(id);
            
            Notificacion notificacion = notificacionService.marcarNotificacionComoLeida(id);
            NotificacionDTO notificacionDTO = notificacionConverter.toDto(notificacion);
            
            log.info("Notificación marcada como leída exitosamente: {}", id);
            return notificacionDTO;
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al marcar notificación como leída: {} - Error: {}", 
                    id, e.getMessage());
            throw new NotificacionValidationException(e.getMessage(), e);
        } catch (SecurityException e) {
            log.warn("Acceso denegado al marcar notificación como leída: {} - Error: {}", id, e.getMessage());
            throw new NotificacionAccesoException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al marcar notificación como leída: {} - Error: {}", 
                     id, e.getMessage(), e);
            throw new NotificacionOperacionException("Error interno al marcar notificación como leída", e);
        }
    }

    // ===============================
    // MÉTODOS DE ELIMINACIÓN
    // ===============================

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public NotificacionDTO eliminarNotificacionUsuarioActual(String id) {
        log.debug("Eliminando notificación del usuario autenticado: {}", id);
        
        try {
            validarIdNotificacion(id);
            
            Notificacion notificacion = notificacionService.eliminarNotificacionUsuarioActual(id);
            NotificacionDTO notificacionDTO = notificacionConverter.toDto(notificacion);
            
            log.info("Notificación eliminada exitosamente: {}", id);
            return notificacionDTO;
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al eliminar notificación: {} - Error: {}", 
                    id, e.getMessage());
            throw new NotificacionValidationException(e.getMessage(), e);
        } catch (SecurityException e) {
            log.warn("Acceso denegado al eliminar notificación: {} - Error: {}", id, e.getMessage());
            throw new NotificacionAccesoException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al eliminar notificación: {} - Error: {}", 
                     id, e.getMessage(), e);
            throw new NotificacionOperacionException("Error interno al eliminar notificación", e);
        }
    }

    // ===============================
    // MÉTODOS DE CONTEO
    // ===============================

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public long contarNoLeidasUsuarioActual() {
        log.debug("Contando notificaciones no leídas del usuario autenticado");
        
        try {
            long count = notificacionService.contarNoLeidasUsuarioActual();
            
            log.debug("Notificaciones no leídas encontradas: {}", count);
            return count;
        } catch (Exception e) {
            log.error("Error inesperado al contar notificaciones no leídas: {}", e.getMessage(), e);
            throw new NotificacionOperacionException("Error interno al contar notificaciones no leídas", e);
        }
    }

    // ===============================
    // MÉTODOS UTILITARIOS PRIVADOS
    // ===============================

    /**
     * Valida los parámetros de paginación.
     * 
     * @param page Número de página
     * @param size Tamaño de página
     * @throws IllegalArgumentException Si los parámetros son inválidos
     */
    private void validarParametrosPaginacion(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("El número de página no puede ser negativo");
        }
        
        if (size <= 0) {
            throw new IllegalArgumentException("El tamaño de página debe ser mayor que cero");
        }
        
        if (size > 100) {
            throw new IllegalArgumentException("El tamaño de página no puede ser mayor que 100");
        }
    }

    /**
     * Valida el ID de una notificación.
     * 
     * @param id ID de la notificación
     * @throws IllegalArgumentException Si el ID es inválido
     */
    private void validarIdNotificacion(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la notificación no puede ser nulo o vacío");
        }
    }

    
}
