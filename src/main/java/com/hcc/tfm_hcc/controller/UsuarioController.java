package com.hcc.tfm_hcc.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.hcc.tfm_hcc.dto.ChangePasswordRequest;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;

/**
 * Controlador REST para la gestión integral de usuarios en el sistema HCC.
 * Proporciona endpoints para operaciones de usuario incluyendo gestión de perfiles,
 * cambio de contraseñas, gestión de solicitudes de asignación y notificaciones.
 * 
 * <p>Funcionalidades principales:</p>
 * <ul>
 *   <li>Consulta y actualización de datos de usuario autenticado</li>
 *   <li>Gestión segura de contraseñas</li>
 *   <li>Gestión de solicitudes de asignación médico-paciente</li>
 *   <li>Administración de notificaciones del usuario</li>
 * </ul>
 * 
 * <p>Todos los endpoints requieren autenticación de usuario.</p>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface UsuarioController {

    /**
     * Obtiene el nombre completo del usuario autenticado actualmente.
     *
     * @return Nombre del usuario autenticado
     */
    @GetMapping("/nombre")
    String getNombreUsuario();

    /**
     * Obtiene los datos completos del usuario autenticado.
     * Incluye información personal, perfil y preferencias del usuario.
     *
     * @return ResponseEntity con el UsuarioDTO del usuario actual
     */
    @GetMapping("/me")
    ResponseEntity<UsuarioDTO> getUsuarioActual();

    /**
     * Cambia la contraseña del usuario autenticado.
     * Requiere verificación de la contraseña actual antes del cambio.
     *
     * @param body ChangePasswordRequest con contraseña actual y nueva contraseña
     * @return ResponseEntity con mensaje de confirmación o error
     */
    @PutMapping("/password")
    ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest body);

    /**
     * Lista todas las solicitudes de asignación relacionadas con el usuario autenticado.
     * Incluye solicitudes recibidas y enviadas según el rol del usuario.
     *
     * @return ResponseEntity con lista de SolicitudAsignacion del usuario
     */
    @GetMapping("/solicitudes")
    ResponseEntity<List<SolicitudAsignacion>> listarMisSolicitudes();

    /**
     * Actualiza el estado de una solicitud de asignación específica.
     * Solo el usuario receptor puede cambiar el estado de la solicitud.
     *
     * @param solicitudId ID único de la solicitud a actualizar
     * @param body Map con el nuevo estado de la solicitud
     * @return ResponseEntity con la SolicitudAsignacion actualizada
     */
    @PutMapping("/solicitudes/{solicitudId}")
    ResponseEntity<SolicitudAsignacion> actualizarEstadoSolicitud(@PathVariable("solicitudId") String solicitudId, 
                                                                  @RequestBody Map<String, String> body);

    /**
     * Lista las notificaciones del usuario autenticado con paginación.
     * Permite consultar notificaciones de forma eficiente mediante páginas.
     *
     * @param page Número de página a consultar (comenzando desde 0)
     * @param size Tamaño de la página (número de notificaciones por página)
     * @return ResponseEntity con Map de notificaciones paginadas
     */
    @GetMapping("/notificaciones")
    ResponseEntity<Map<String, Object>> listarMisNotificaciones(@RequestParam("page") int page, 
                                                                 @RequestParam("size") int size);

    /**
     * Marca todas las notificaciones del usuario autenticado como leídas.
     * Esta operación actualiza el estado de todas las notificaciones pendientes.
     *
     * @return ResponseEntity con mensaje de confirmación
     */
    @PostMapping("/notificaciones/marcar-leidas")
    ResponseEntity<String> marcarTodasNotificacionesLeidas();
}
