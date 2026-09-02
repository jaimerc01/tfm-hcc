package com.hcc.tfm_hcc.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.hcc.tfm_hcc.dto.AnotacionMedicaDTO;
import com.hcc.tfm_hcc.dto.ChangePasswordRequest;
import com.hcc.tfm_hcc.dto.TotpCodeRequestDTO;
import com.hcc.tfm_hcc.dto.SolicitudAsignacionDTO;
import com.hcc.tfm_hcc.dto.TotpSetupResponseDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;

/**
 * Controlador REST para la gestión integral de usuarios en el sistema HCC.
 * Proporciona endpoints para operaciones de usuario incluyendo gestión de perfiles,
 * cambio de contraseñas y gestión de solicitudes de asignación.
 *
 * <p>Funcionalidades principales:</p>
 * <ul>
 *   <li>Consulta y actualización de datos de usuario autenticado</li>
 *   <li>Gestión segura de contraseñas</li>
 *   <li>Gestión de solicitudes de asignación médico-paciente</li>
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
    String getNombreUsuario();

    /**
     * Obtiene los datos completos del usuario autenticado.
     * Incluye información personal, perfil y preferencias del usuario.
     *
     * @return ResponseEntity con el UsuarioDTO del usuario actual
     */
    ResponseEntity<UsuarioDTO> getUsuarioActual();

    /**
     * Cambia la contraseña del usuario autenticado.
     * Requiere verificación de la contraseña actual antes del cambio.
     *
     * @param body ChangePasswordRequest con contraseña actual y nueva contraseña
     * @return ResponseEntity con mensaje de confirmación o error
     */
    ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest body);

    /**
     * Lista todas las solicitudes de asignación relacionadas con el usuario autenticado.
     * Incluye solicitudes recibidas y enviadas según el rol del usuario.
     *
     * @return ResponseEntity con lista de SolicitudAsignacionDTO del usuario
     */
    ResponseEntity<List<SolicitudAsignacionDTO>> listarMisSolicitudes();

    /**
     * Actualiza el estado de una solicitud de asignación específica.
     * Solo el usuario receptor puede cambiar el estado de la solicitud.
     *
     * @param idSolicitud ID único de la solicitud a actualizar
     * @param body Map con el nuevo estado de la solicitud
     * @return ResponseEntity con la SolicitudAsignacionDTO actualizada
     */
    ResponseEntity<SolicitudAsignacionDTO> actualizarEstadoSolicitud(@PathVariable("idSolicitud") String idSolicitud,
                                                                     @RequestBody Map<String, String> body);

    /**
     * Inicia la configuración del segundo factor (TOTP) para el usuario autenticado.
     * Genera un secreto nuevo, pendiente de confirmar con {@link #confirmTotp}.
     *
     * @return ResponseEntity con el secreto (Base32) y la URI otpauth para el código QR
     */
    ResponseEntity<TotpSetupResponseDTO> setupTotp();

    /**
     * Confirma la activación del segundo factor con el primer código válido de la
     * aplicación autenticadora.
     *
     * @param body código de 6 dígitos
     * @return ResponseEntity vacío en caso de éxito
     */
    ResponseEntity<Void> confirmTotp(@RequestBody TotpCodeRequestDTO body);

    /**
     * Desactiva el segundo factor del usuario autenticado.
     *
     * @param body código de 6 dígitos, para probar la posesión del segundo factor
     * @return ResponseEntity vacío en caso de éxito
     */
    ResponseEntity<Void> disableTotp(@RequestBody TotpCodeRequestDTO body);

    /**
     * Indica si el usuario autenticado tiene activado el segundo factor.
     *
     * @return ResponseEntity con un booleano: true si está activo
     */
    ResponseEntity<Boolean> getTotpStatus();

    /**
     * Lista las anotaciones médicas recibidas por el usuario autenticado, de la más
     * reciente a la más antigua, con filtro opcional por médico y por rango de fechas.
     *
     * @param medicoNif NIF del médico por el que filtrar (opcional)
     * @param desde fecha de inicio del rango en formato ISO-8601 (opcional)
     * @param hasta fecha de fin del rango en formato ISO-8601 (opcional)
     * @return ResponseEntity con la lista de AnotacionMedicaDTO recibidas
     */
    ResponseEntity<List<AnotacionMedicaDTO>> listarMisAnotaciones(@RequestParam(value = "medicoNif", required = false) String medicoNif,
                                                                  @RequestParam(value = "desde", required = false) String desde,
                                                                  @RequestParam(value = "hasta", required = false) String hasta);
}
