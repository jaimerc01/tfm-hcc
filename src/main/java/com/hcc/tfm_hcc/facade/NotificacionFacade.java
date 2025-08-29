package com.hcc.tfm_hcc.facade;

import java.util.List;
import java.util.Map;

/**
 * Facade para la gestión de notificaciones en el sistema HCC.
 * Proporciona operaciones completas para el manejo de notificaciones
 * del usuario autenticado, incluyendo listado, paginación, marcado de lectura
 * y eliminación de notificaciones.
 * 
 * <p>Funcionalidades principales:</p>
 * <ul>
 *   <li>Consulta de notificaciones con y sin paginación</li>
 *   <li>Gestión del estado de lectura de las notificaciones</li>
 *   <li>Conteo de notificaciones no leídas</li>
 *   <li>Eliminación individual de notificaciones</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface NotificacionFacade {
    
    /**
     * Lista todas las notificaciones del usuario autenticado sin paginación.
     * 
     * @return Lista de Map con los datos de las notificaciones del usuario actual
     */
    List<Map<String, Object>> listarNotificacionesUsuarioActual();
    
    /**
     * Marca todas las notificaciones del usuario autenticado como leídas.
     * Esta operación actualiza el estado de todas las notificaciones pendientes
     * de lectura del usuario actual.
     */
    void marcarTodasComoLeidasUsuarioActual();
    
    /**
     * Marca una notificación específica como leída.
     * 
     * @param id ID único de la notificación a marcar como leída
     * @throws IllegalArgumentException si el ID no es válido
     * @throws SecurityException si la notificación no pertenece al usuario actual
     */
    void marcarNotificacionComoLeida(String id) throws IllegalArgumentException, SecurityException;
    
    /**
     * Elimina una notificación específica del usuario autenticado.
     * 
     * @param id ID único de la notificación a eliminar
     * @throws IllegalArgumentException si el ID no es válido
     * @throws SecurityException si la notificación no pertenece al usuario actual
     */
    void eliminarNotificacionUsuarioActual(String id) throws IllegalArgumentException, SecurityException;
    
    /**
     * Lista las notificaciones del usuario autenticado con paginación.
     * Proporciona un mecanismo eficiente para consultar grandes volúmenes
     * de notificaciones mediante páginas de tamaño controlado.
     * 
     * @param page Número de página a consultar (comenzando desde 0)
     * @param size Tamaño de la página (número de notificaciones por página)
     * @return Map con los datos paginados de las notificaciones
     */
    Map<String, Object> listarNotificacionesUsuarioActual(int page, int size);
    
    /**
     * Cuenta el número de notificaciones no leídas del usuario autenticado.
     * Útil para mostrar indicadores de notificaciones pendientes en la interfaz.
     * 
     * @return Número de notificaciones no leídas del usuario actual
     */
    long contarNoLeidasUsuarioActual();
}
