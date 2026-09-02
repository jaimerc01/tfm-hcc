package com.hcc.tfm_hcc.controller.impl;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hcc.tfm_hcc.constants.RestUrls;
import com.hcc.tfm_hcc.controller.NotificacionController;
import com.hcc.tfm_hcc.dto.NotificacionDTO;
import com.hcc.tfm_hcc.facade.NotificacionFacade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del controlador REST de notificaciones del usuario autenticado.
 *
 * <p>El controlador se limita a validar la forma de la petición y delegar en
 * {@link NotificacionFacade}. La autorización (usuario autenticado) la garantiza
 * la cadena de filtros de seguridad; la traducción de los errores de dominio a
 * códigos HTTP la resuelven las excepciones anotadas con {@code @ResponseStatus}
 * que lanza el facade ({@code NotificacionValidationException} -&gt; 400,
 * {@code NotificacionAccesoException} -&gt; 403,
 * {@code NotificacionOperacionException} -&gt; 500).</p>
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping(RestUrls.NOTIFICACION_BASE)
@RequiredArgsConstructor
public class NotificacionControllerImpl implements NotificacionController {

    /** Clave del cuerpo de respuesta con el número de notificaciones no leídas. */
    private static final String CLAVE_NO_LEIDAS = "noLeidas";

    /** Facade para operaciones de notificaciones. */
    private final NotificacionFacade notificacionFacade;

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping
    public ResponseEntity<Map<String, NotificacionDTO>> listarMisNotificaciones(@RequestParam("page") int page,
                                                                                @RequestParam("size") int size) {
        log.info("Listando notificaciones del usuario autenticado - página: {}, tamaño: {}", page, size);
        return ResponseEntity.ok(notificacionFacade.listarNotificacionesUsuarioActual(page, size));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping(RestUrls.NOTIFICACION_MARCAR_LEIDAS)
    public ResponseEntity<String> marcarTodasNotificacionesLeidas() {
        log.info("Marcando todas las notificaciones del usuario autenticado como leídas");
        notificacionFacade.marcarTodasComoLeidasUsuarioActual();
        return ResponseEntity.ok("Notificaciones marcadas como leídas");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.NOTIFICACION_NO_LEIDAS)
    public ResponseEntity<Map<String, Object>> contarNotificacionesNoLeidas() {
        log.debug("Contando notificaciones no leídas del usuario autenticado");
        long noLeidas = notificacionFacade.contarNoLeidasUsuarioActual();
        return ResponseEntity.ok(Map.of(CLAVE_NO_LEIDAS, noLeidas));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PutMapping(RestUrls.NOTIFICACION_LEIDA)
    public ResponseEntity<Void> marcarNotificacionLeida(@PathVariable("id") String id) {
        log.info("Marcando notificación como leída: {}", id);
        notificacionFacade.marcarNotificacionComoLeida(id);
        return ResponseEntity.ok().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DeleteMapping(RestUrls.NOTIFICACION_ID)
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable("id") String id) {
        log.info("Eliminando notificación: {}", id);
        notificacionFacade.eliminarNotificacionUsuarioActual(id);
        return ResponseEntity.noContent().build();
    }
}
