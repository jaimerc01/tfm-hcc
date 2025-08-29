package com.hcc.tfm_hcc.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.hcc.tfm_hcc.model.Notificacion;

/**
 * Servicio para la gestión de notificaciones en el sistema HCC.
 * Proporciona operaciones para consultar, marcar como leídas y eliminar 
 * notificaciones del usuario autenticado actualmente en el sistema.
 * 
 * <p>Este servicio maneja la lógica de negocio relacionada con:</p>
 * <ul>
 *   <li>Consulta de notificaciones del usuario actual</li>
 *   <li>Gestión del estado de lectura de notificaciones</li>
 *   <li>Eliminación de notificaciones</li>
 *   <li>Paginación de notificaciones</li>
 *   <li>Conteo de notificaciones no leídas</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface NotificacionService {
    
    /**
     * Lista todas las notificaciones del usuario autenticado actualmente.
     * 
     * @return Lista de Notificacion del usuario actual, ordenadas por fecha de creación descendente
     * @throws RuntimeException si el usuario no está autenticado
     */
    List<Notificacion> listarNotificacionesUsuarioActual();
    
    /**
     * Marca todas las notificaciones del usuario autenticado como leídas.
     * 
     * @throws RuntimeException si el usuario no está autenticado
     */
    void marcarTodasComoLeidasUsuarioActual();
    
    /**
     * Marca una notificación específica como leída.
     * Solo puede marcar notificaciones que pertenezcan al usuario autenticado.
     * 
     * @param id ID único de la notificación a marcar como leída
     * @throws IllegalArgumentException si el ID es inválido o la notificación no existe
     * @throws RuntimeException si el usuario no está autenticado o no tiene permisos sobre la notificación
     */
    void marcarNotificacionComoLeida(String id);
    
    /**
     * Elimina una notificación específica del usuario autenticado.
     * Solo puede eliminar notificaciones que pertenezcan al usuario autenticado.
     * 
     * @param id ID único de la notificación a eliminar
     * @throws IllegalArgumentException si el ID es inválido o la notificación no existe
     * @throws RuntimeException si el usuario no está autenticado o no tiene permisos sobre la notificación
     */
    void eliminarNotificacionUsuarioActual(String id);
    
    /**
     * Lista las notificaciones del usuario autenticado con paginación.
     * 
     * @param page Número de página (basado en cero)
     * @param size Tamaño de página (número de elementos por página)
     * @return Page con las notificaciones del usuario actual, ordenadas por fecha de creación descendente
     * @throws IllegalArgumentException si los parámetros de paginación son inválidos
     * @throws RuntimeException si el usuario no está autenticado
     */
    Page<Notificacion> listarNotificacionesUsuarioActual(int page, int size);
    
    /**
     * Cuenta el número de notificaciones no leídas del usuario autenticado.
     * 
     * @return Número de notificaciones no leídas del usuario actual
     * @throws RuntimeException si el usuario no está autenticado
     */
    long contarNoLeidasUsuarioActual();
}
