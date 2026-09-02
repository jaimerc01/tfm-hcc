package com.hcc.tfm_hcc.controller.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.constants.RestUrls;
import com.hcc.tfm_hcc.controller.UsuarioController;
import com.hcc.tfm_hcc.converter.AnotacionMedicaConverter;
import com.hcc.tfm_hcc.converter.SolicitudAsignacionConverter;
import com.hcc.tfm_hcc.dto.AnotacionMedicaDTO;
import com.hcc.tfm_hcc.dto.ChangePasswordRequest;
import com.hcc.tfm_hcc.dto.SolicitudAsignacionDTO;
import com.hcc.tfm_hcc.dto.TotpCodeRequestDTO;
import com.hcc.tfm_hcc.dto.TotpSetupResponseDTO;
import com.hcc.tfm_hcc.dto.UpdateUsuarioRequest;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.model.AnotacionMedica;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.exception.ReautenticacionRequeridaException;
import com.hcc.tfm_hcc.exception.UsuarioNoAutenticadoException;
import com.hcc.tfm_hcc.exception.UsuarioOperacionException;
import com.hcc.tfm_hcc.exception.UsuarioSinPermisoException;
import com.hcc.tfm_hcc.exception.UsuarioValidationException;
import com.hcc.tfm_hcc.util.LogMaskUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del controlador REST para la gestión integral de usuarios.
 * Proporciona endpoints REST para operaciones de usuario incluyendo gestión de perfiles,
 * cambio de contraseñas y gestión de solicitudes.
 * 
 * <p>Características de seguridad:</p>
 * <ul>
 *   <li>Todos los endpoints requieren autenticación</li>
 *   <li>Validación de autorización delegada a la capa facade</li>
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

    /** Convierte las entidades de solicitud a DTO antes de exponerlas en la API */
    private final SolicitudAsignacionConverter solicitudAsignacionConverter;

    /** Convierte las entidades de anotación médica a DTO antes de exponerlas en la API */
    private final AnotacionMedicaConverter anotacionMedicaConverter;

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.USUARIO_NOMBRE)
    public String getNombreUsuario() {
        log.debug("Consultando nombre de usuario autenticado");
        return usuarioFacade.getNombreUsuario();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.USUARIO_ME)
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
            
            log.info("Datos de usuario obtenidos exitosamente: {}", LogMaskUtil.enmascarar(dto.getNif()));
            return ResponseEntity.ok(dto);

        } catch (UsuarioNoAutenticadoException e) {
            log.warn("Usuario no autenticado al consultar datos del usuario actual");
            throw e;
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
    public ResponseEntity<List<SolicitudAsignacionDTO>> listarMisSolicitudes() {
        log.info("Listando solicitudes para usuario autenticado");

        try {
            UsuarioDTO dto = usuarioFacade.getUsuarioActual();
            if (dto == null) {
                log.warn("Usuario no autenticado al listar solicitudes");
                throw new UsuarioNoAutenticadoException("Usuario no autenticado");
            }

            List<SolicitudAsignacion> solicitudes = usuarioFacade.listarMisSolicitudes();
            log.info("Se encontraron {} solicitudes para el usuario", solicitudes.size());
            return ResponseEntity.ok(solicitudAsignacionConverter.toDtoList(solicitudes));

        } catch (UsuarioNoAutenticadoException e) {
            log.warn("Usuario no autenticado al listar solicitudes");
            throw e;
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
    // TODO: CORREGIR ESE REQUESTBODY Y HACER UN DTO
    public ResponseEntity<SolicitudAsignacionDTO> actualizarEstadoSolicitud(@PathVariable("idSolicitud") String idSolicitud,
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
            return ResponseEntity.ok(solicitudAsignacionConverter.toDto(updated));
            
        } catch (UsuarioValidationException e) {
            log.warn("Error de validación al actualizar solicitud: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Error de permisos al actualizar solicitud: {}", e.getMessage());
            return ResponseEntity.status(403).build();
        } catch (UsuarioSinPermisoException e) {
            log.warn("Error de permisos al actualizar solicitud: {}", e.getMessage());
            return ResponseEntity.status(403).build();
        } catch (UsuarioNoAutenticadoException e) {
            log.warn("Usuario no autenticado al actualizar solicitud");
            throw e;
        } catch (Exception e) {
            log.error("Error interno al actualizar solicitud: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error interno al actualizar solicitud", e);
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
            
            log.info("Datos de usuario actualizados exitosamente: {}", LogMaskUtil.enmascarar(actualizado.getNif()));
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
     * Exige confirmar la contraseña actual mediante la cabecera {@code X-Current-Password}.
     *
     * @param currentPassword contraseña actual del usuario, enviada en la cabecera {@code X-Current-Password}
     * @return ResponseEntity vacío confirmando la eliminación
     */
    @DeleteMapping(RestUrls.USUARIO_ME)
    public ResponseEntity<Void> deleteCuenta(
            @RequestHeader(value = "X-Current-Password", required = false) String currentPassword) {
        log.info("Eliminando cuenta del usuario autenticado");

        try {
            usuarioFacade.deleteCuentaActual(currentPassword);
            log.info("Cuenta eliminada exitosamente");
            return ResponseEntity.noContent().build();

        } catch (ReautenticacionRequeridaException _) {
            log.warn("Reautenticación requerida para eliminar cuenta");
            return ResponseEntity.status(401).header("X-Reauth-Required", "true").build();
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
     * Limita el tratamiento de los datos del usuario autenticado (derecho de limitación
     * del tratamiento, art. 18 RGPD): mientras dure, ningún médico puede consultar su
     * historial clínico.
     *
     * @return ResponseEntity vacío confirmando la limitación
     */
    @PostMapping(RestUrls.USUARIO_LIMITAR_TRATAMIENTO)
    public ResponseEntity<Void> limitarTratamiento() {
        log.info("Limitando el tratamiento de datos del usuario autenticado");

        try {
            usuarioFacade.limitarTratamiento();
            log.info("Tratamiento de datos limitado exitosamente");
            return ResponseEntity.noContent().build();

        } catch (IllegalStateException e) {
            return respuestaSegunEstadoDeCuenta(e);
        } catch (Exception e) {
            log.error("Error al limitar el tratamiento de datos: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error al limitar el tratamiento de datos", e);
        }
    }

    /**
     * Revierte la limitación del tratamiento aplicada por {@link #limitarTratamiento()}.
     *
     * @return ResponseEntity vacío confirmando la reanudación
     */
    @PostMapping(RestUrls.USUARIO_REANUDAR_TRATAMIENTO)
    public ResponseEntity<Void> reanudarTratamiento() {
        log.info("Reanudando el tratamiento de datos del usuario autenticado");

        try {
            usuarioFacade.reanudarTratamiento();
            log.info("Tratamiento de datos reanudado exitosamente");
            return ResponseEntity.noContent().build();

        } catch (IllegalStateException e) {
            return respuestaSegunEstadoDeCuenta(e);
        } catch (Exception e) {
            log.error("Error al reanudar el tratamiento de datos: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error al reanudar el tratamiento de datos", e);
        }
    }

    /**
     * Traduce las dos causas posibles de {@link IllegalStateException} que lanza el
     * servicio para {@code limitarTratamiento}/{@code reanudarTratamiento}: usuario no
     * autenticado (401) o cuenta ya eliminada, que no admite este cambio de estado (409).
     */
    private ResponseEntity<Void> respuestaSegunEstadoDeCuenta(IllegalStateException e) {
        if (ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO.equals(e.getMessage())) {
            log.warn("Usuario no autenticado al cambiar el estado del tratamiento de datos");
            return ResponseEntity.status(401).build();
        }
        log.warn("No se pudo cambiar el estado del tratamiento de datos: {}", e.getMessage());
        return ResponseEntity.status(409).build();
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

        } catch (UsuarioNoAutenticadoException e) {
            log.warn("Usuario no autenticado al consultar logs");
            throw e;
        } catch (Exception e) {
            log.error("Error al consultar logs: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error al consultar logs", e);
        }
    }

    /**
     * Exporta todos los datos del usuario autenticado.
     * Funcionalidad para cumplimiento RGPD. Exige confirmar la contraseña actual
     * mediante la cabecera {@code X-Current-Password}.
     *
     * @param currentPassword contraseña actual del usuario, enviada en la cabecera {@code X-Current-Password}
     * @return ResponseEntity con los datos exportados del usuario
     */
    @GetMapping(RestUrls.USUARIO_EXPORT)
    public ResponseEntity<Object> exportUsuario(
            @RequestHeader(value = "X-Current-Password", required = false) String currentPassword) {
        log.info("Exportando datos del usuario autenticado");

        try {
            UsuarioDTO dto = usuarioFacade.getUsuarioActual();
            if (dto == null) {
                throw new UsuarioNoAutenticadoException("Usuario no autenticado");
            }

            Object exportData = usuarioFacade.exportUsuario(currentPassword);
            log.info("Datos de usuario exportados exitosamente");
            return ResponseEntity.ok(exportData);

        } catch (ReautenticacionRequeridaException e) {
            log.warn("Reautenticación requerida para exportar datos del usuario");
            return ResponseEntity.status(401).header("X-Reauth-Required", "true").build();
        } catch (UsuarioNoAutenticadoException e) {
            log.warn("Usuario no autenticado al exportar datos del usuario");
            throw e;
        } catch (Exception e) {
            log.error("Error al exportar datos del usuario: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error al exportar datos del usuario", e);
        }
    }

    // ===============================
    // SEGUNDO FACTOR (TOTP)
    // ===============================

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping(RestUrls.USUARIO_2FA_SETUP)
    public ResponseEntity<TotpSetupResponseDTO> setupTotp() {
        log.info("Iniciando configuración de segundo factor");
        try {
            return ResponseEntity.ok(usuarioFacade.setupTotp());
        } catch (Exception e) {
            log.error("Error al iniciar configuración de segundo factor: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error al iniciar la configuración del segundo factor", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping(RestUrls.USUARIO_2FA_CONFIRM)
    public ResponseEntity<Void> confirmTotp(@RequestBody TotpCodeRequestDTO body) {
        log.info("Confirmando activación de segundo factor");
        try {
            usuarioFacade.confirmTotp(body != null ? body.getCode() : null);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            log.warn("Estado inválido al confirmar segundo factor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            log.warn("Código inválido al confirmar segundo factor: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error al confirmar segundo factor: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error al confirmar el segundo factor", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping(RestUrls.USUARIO_2FA_DISABLE)
    public ResponseEntity<Void> disableTotp(@RequestBody TotpCodeRequestDTO body) {
        log.info("Desactivando segundo factor");
        try {
            usuarioFacade.disableTotp(body != null ? body.getCode() : null);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            log.warn("Estado inválido al desactivar segundo factor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            log.warn("Código inválido al desactivar segundo factor: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error al desactivar segundo factor: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error al desactivar el segundo factor", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.USUARIO_2FA_STATUS)
    public ResponseEntity<Boolean> getTotpStatus() {
        log.debug("Consultando estado del segundo factor");
        return ResponseEntity.ok(usuarioFacade.isTotpEnabled());
    }

    // ===============================
    // ANOTACIONES MÉDICAS
    // ===============================

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.USUARIO_ANOTACIONES)
    public ResponseEntity<List<AnotacionMedicaDTO>> listarMisAnotaciones(@RequestParam(value = "medicoNif", required = false) String medicoNif,
                                                                         @RequestParam(value = "desde", required = false) String desde,
                                                                         @RequestParam(value = "hasta", required = false) String hasta) {
        log.info("Consultando anotaciones médicas para usuario autenticado");

        try {
            UsuarioDTO dto = usuarioFacade.getUsuarioActual();
            if (dto == null) {
                throw new UsuarioNoAutenticadoException("Usuario no autenticado");
            }

            LocalDateTime desdeFecha = desde != null ? LocalDateTime.parse(desde) : null;
            LocalDateTime hastaFecha = hasta != null ? LocalDateTime.parse(hasta) : null;

            List<AnotacionMedica> anotaciones = usuarioFacade.listarMisAnotaciones(medicoNif, desdeFecha, hastaFecha);
            log.info("Anotaciones médicas consultadas exitosamente: {} registros", anotaciones.size());
            return ResponseEntity.ok(anotacionMedicaConverter.toDtoList(anotaciones));

        } catch (UsuarioNoAutenticadoException e) {
            log.warn("Usuario no autenticado al consultar anotaciones médicas");
            throw e;
        } catch (DateTimeParseException e) {
            log.warn("Formato de fecha inválido al consultar anotaciones médicas: {}", e.getMessage());
            throw new UsuarioValidationException(ErrorMessages.ERROR_FORMATO_FECHA_INVALIDO);
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al consultar anotaciones médicas: {}", e.getMessage());
            throw new UsuarioValidationException(e.getMessage());
        } catch (Exception e) {
            log.error("Error al consultar anotaciones médicas: {}", e.getMessage(), e);
            throw new UsuarioOperacionException("Error al consultar anotaciones médicas", e);
        }
    }
}
