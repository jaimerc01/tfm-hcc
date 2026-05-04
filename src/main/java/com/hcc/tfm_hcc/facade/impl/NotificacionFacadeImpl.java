package com.hcc.tfm_hcc.facade.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.facade.NotificacionFacade;
import com.hcc.tfm_hcc.model.Notificacion;
import com.hcc.tfm_hcc.service.NotificacionService;

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

    // ===============================
    // MÉTODOS DE LISTADO DE NOTIFICACIONES
    // ===============================

    /**
     * Lista todas las notificaciones del usuario autenticado actual.
     * 
     * @return List<Map<String, Object>> Lista de notificaciones en formato Map
     * @throws RuntimeException Si ocurre un error durante la consulta
     */
    @Override
    public List<Map<String, Object>> listarNotificacionesUsuarioActual() {
        log.debug("Obteniendo todas las notificaciones del usuario autenticado");
        
        try {
            List<Notificacion> list = notificacionService.listarNotificacionesUsuarioActual();
            
            List<Map<String, Object>> resultado = list.stream().map(n -> {
                Map<String, Object> m = Map.of(
                    "id", n.getId(),
                    "mensaje", n.getMensaje(),
                    "leida", n.isLeida(),
                    "fechaCreacion", n.getFechaCreacion(),
                    "enlace", computeEnlace(n)
                );
                return m;
            }).collect(Collectors.toList());
            
            log.info("Notificaciones obtenidas: {} registros", resultado.size());
            return resultado;
        } catch (Exception e) {
            log.error("Error inesperado al obtener notificaciones del usuario: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno durante la consulta de notificaciones", e);
        }
    }

    /**
     * Lista las notificaciones del usuario autenticado con paginación.
     * 
     * @param page Número de página (0-based)
     * @param size Tamaño de página
     * @return Map<String, Object> Mapa con elementos y total de notificaciones
     * @throws IllegalArgumentException Si los parámetros de paginación son inválidos
     * @throws RuntimeException Si ocurre un error durante la consulta
     */
    @Override
    public Map<String, Object> listarNotificacionesUsuarioActual(int page, int size) {
        log.debug("Obteniendo notificaciones paginadas del usuario autenticado - Página: {}, Tamaño: {}", 
                page, size);
        
        try {
            validarParametrosPaginacion(page, size);
            
            var p = notificacionService.listarNotificacionesUsuarioActual(page, size);
            var items = p.stream().map(n -> Map.of(
                "id", n.getId(),
                "mensaje", n.getMensaje(),
                "leida", n.isLeida(),
                "fechaCreacion", n.getFechaCreacion(),
                "enlace", computeEnlace(n)
            )).toList();
            
            Map<String, Object> resultado = Map.of("items", items, "total", p.getTotalElements());
            
            log.info("Notificaciones paginadas obtenidas: {} de {} registros", 
                    items.size(), p.getTotalElements());
            return resultado;
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación en paginación de notificaciones: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al obtener notificaciones paginadas: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno durante la consulta de notificaciones", e);
        }
    }

    // ===============================
    // MÉTODOS DE GESTIÓN DE ESTADO
    // ===============================

    /**
     * Marca todas las notificaciones del usuario autenticado como leídas.
     * 
     * @throws RuntimeException Si ocurre un error durante la actualización
     */
    @Override
    public void marcarTodasComoLeidasUsuarioActual() {
        log.debug("Marcando todas las notificaciones como leídas para usuario autenticado");
        
        try {
            notificacionService.marcarTodasComoLeidasUsuarioActual();
            
            log.info("Todas las notificaciones marcadas como leídas exitosamente");
        } catch (Exception e) {
            log.error("Error inesperado al marcar notificaciones como leídas: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno al marcar notificaciones como leídas", e);
        }
    }

    /**
     * Marca una notificación específica como leída.
     * 
     * @param id ID de la notificación a marcar como leída
     * @throws IllegalArgumentException Si el ID es nulo o vacío
     * @throws RuntimeException Si ocurre un error durante la actualización
     */
    @Override
    public void marcarNotificacionComoLeida(String id) {
        log.debug("Marcando notificación como leída: {}", id);
        
        try {
            validarIdNotificacion(id);
            
            notificacionService.marcarNotificacionComoLeida(id);
            
            log.info("Notificación marcada como leída exitosamente: {}", id);
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al marcar notificación como leída: {} - Error: {}", 
                    id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al marcar notificación como leída: {} - Error: {}", 
                     id, e.getMessage(), e);
            throw new RuntimeException("Error interno al marcar notificación como leída", e);
        }
    }

    // ===============================
    // MÉTODOS DE ELIMINACIÓN
    // ===============================

    /**
     * Elimina una notificación específica del usuario autenticado.
     * 
     * @param id ID de la notificación a eliminar
     * @throws IllegalArgumentException Si el ID es nulo o vacío
     * @throws RuntimeException Si ocurre un error durante la eliminación
     */
    @Override
    public void eliminarNotificacionUsuarioActual(String id) {
        log.debug("Eliminando notificación del usuario autenticado: {}", id);
        
        try {
            validarIdNotificacion(id);
            
            notificacionService.eliminarNotificacionUsuarioActual(id);
            
            log.info("Notificación eliminada exitosamente: {}", id);
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al eliminar notificación: {} - Error: {}", 
                    id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al eliminar notificación: {} - Error: {}", 
                     id, e.getMessage(), e);
            throw new RuntimeException("Error interno al eliminar notificación", e);
        }
    }

    // ===============================
    // MÉTODOS DE CONTEO
    // ===============================

    /**
     * Cuenta las notificaciones no leídas del usuario autenticado.
     * 
     * @return long Número de notificaciones no leídas
     * @throws RuntimeException Si ocurre un error durante la consulta
     */
    @Override
    public long contarNoLeidasUsuarioActual() {
        log.debug("Contando notificaciones no leídas del usuario autenticado");
        
        try {
            long count = notificacionService.contarNoLeidasUsuarioActual();
            
            log.debug("Notificaciones no leídas encontradas: {}", count);
            return count;
        } catch (Exception e) {
            log.error("Error inesperado al contar notificaciones no leídas: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno al contar notificaciones no leídas", e);
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

    /**
     * Computa el enlace apropiado para una notificación basado en su contenido.
     * 
     * @param n La notificación para la cual generar el enlace
     * @return String El enlace apropiado o null si no se puede determinar
     */
    private String computeEnlace(Notificacion n) {
        if (n == null || n.getMensaje() == null) {
            return null;
        }
        
        String msg = n.getMensaje().toLowerCase();
        if (msg.contains("solicitud") || msg.contains("asignación") || msg.contains("asignacion")) {
            return "/mis-solicitudes";
        }
        if (msg.contains("historia clínica") || msg.contains("historia clinica")) {
            return "/historia-clinica";
        }
        return null;
    }
}
