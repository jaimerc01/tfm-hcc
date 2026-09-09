package com.hcc.tfm_hcc.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.hcc.tfm_hcc.dto.NotificacionPageDTO;

/**
 * Controlador REST para la gestión de notificaciones del usuario autenticado.
 *
 * <p>Expone las operaciones de consulta y mantenimiento de las notificaciones
 * propias: listado paginado, conteo de no leídas, marcado de lectura y
 * eliminación. La creación de notificaciones es una operación interna que
 * realizan otros dominios a través de {@code NotificacionFacade}, por lo que no
 * se expone aquí.</p>
 *
 * <p>Todos los endpoints requieren autenticación.</p>
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface NotificacionController {

    /**
     * Lista las notificaciones del usuario autenticado con paginación.
     *
     * @param page Número de página a consultar (comenzando desde 0)
     * @param size Tamaño de la página (número de notificaciones por página)
     * @return ResponseEntity con las notificaciones de la página solicitada y el total de notificaciones vigentes
     */
    ResponseEntity<NotificacionPageDTO> listarMisNotificaciones(@RequestParam("page") int page,
                                                                @RequestParam("size") int size);

    /**
     * Marca todas las notificaciones del usuario autenticado como leídas.
     *
     * @return ResponseEntity con mensaje de confirmación
     */
    ResponseEntity<String> marcarTodasNotificacionesLeidas();

    /**
     * Cuenta las notificaciones no leídas del usuario autenticado.
     *
     * @return ResponseEntity con el conteo de notificaciones no leídas
     */
    ResponseEntity<Map<String, Object>> contarNotificacionesNoLeidas();

    /**
     * Marca una notificación concreta del usuario autenticado como leída.
     *
     * @param id ID de la notificación a marcar como leída
     * @return ResponseEntity vacío confirmando la operación
     */
    ResponseEntity<Void> marcarNotificacionLeida(@PathVariable("id") String id);

    /**
     * Elimina una notificación concreta del usuario autenticado.
     *
     * @param id ID de la notificación a eliminar
     * @return ResponseEntity vacío confirmando la eliminación
     */
    ResponseEntity<Void> eliminarNotificacion(@PathVariable("id") String id);
}
