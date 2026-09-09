package com.hcc.tfm_hcc.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.model.Notificacion;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.NotificacionRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;
import com.hcc.tfm_hcc.service.NotificacionService;
import com.hcc.tfm_hcc.util.LogMaskUtil;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio de notificaciones.
 * Proporciona funcionalidades para gestionar notificaciones de usuarios.
 * 
 * @author Sistema HCC
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private static final String EUROPE_MADRID = "Europe/Madrid";
    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioFacade usuarioFacade;
    private final HmacSearchIndexService hmacSearchIndexService;

    @Override
    @Transactional
    public Notificacion crearNotificacionParaUsuario(String usuarioNif, String mensaje) {
        if (usuarioNif == null || usuarioNif.trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_DESTINATARIO_INVALIDO);
        }
        if (mensaje == null) {
            mensaje = "";
        }

        var usuarioOpt = usuarioRepository.findByNifHash(hmacSearchIndexService.indexar(usuarioNif));
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException(
                    ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO + " (NIF: " + LogMaskUtil.enmascarar(usuarioNif) + ")");
        }

        Usuario usuario = usuarioOpt.get();
        Notificacion n = new Notificacion();
        n.setMensaje(mensaje);
        n.setLeida(false);
        n.setUsuario(usuario);
        n.setFechaCreacion(LocalDateTime.now(ZoneId.of(EUROPE_MADRID)));

        return notificacionRepository.save(n);
    }

    /**
     * Obtiene el usuario actual autenticado
     */
    private Usuario obtenerUsuarioActual() {
        UsuarioDTO dto = usuarioFacade.getUsuarioActual();
        if (dto == null) {
            throw new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }

        return usuarioRepository.findById(parsearUuid(dto.getId()))
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO));
    }

    /**
     * Convierte un identificador de texto a {@link UUID}, traduciendo un formato inválido a
     * un error de argumento con mensaje estable (sin depender de comparar el texto de la
     * excepción de {@code UUID.fromString}).
     */
    private UUID parsearUuid(String valor) {
        try {
            return UUID.fromString(valor);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_FORMATO_INVALIDO, e);
        }
    }

    /**
     * Valida que una notificación pertenezca al usuario actual
     */
    private void validarPropietarioNotificacion(Notificacion notificacion, Usuario usuario) {
        if (notificacion.getUsuario() == null || 
            !notificacion.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalStateException(ErrorMessages.ERROR_ACCESO_DENEGADO);
        }
    }

    /**
     * Busca una notificación por ID y valida que pertenezca al usuario
     */
    private Notificacion buscarYValidarNotificacion(String notificacionId, Usuario usuario) {
        Notificacion notificacion = notificacionRepository.findById(parsearUuid(notificacionId))
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ERROR_NOTIFICACION_NO_ENCONTRADA));

        validarPropietarioNotificacion(notificacion, usuario);
        return notificacion;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> listarNotificacionesUsuarioActual() {
        Usuario usuario = obtenerUsuarioActual();
        return notificacionRepository.findByUsuarioAndEliminadaFalseOrderByFechaCreacionDesc(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notificacion> listarNotificacionesUsuarioActual(int page, int size) {
        Usuario usuario = obtenerUsuarioActual();
        
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by("fechaCreacion").descending());
            
        return notificacionRepository.findByUsuarioAndEliminadaFalse(usuario, pageable);
    }

    /**
     * Actualiza todas las notificaciones no leídas a leídas
     */
    private List<Notificacion> actualizarNotificacionesALeidas(List<Notificacion> notificaciones) {
        notificaciones.stream()
                .filter(n -> !n.isLeida())
                .forEach(n -> n.setLeida(true));
        
        if (!notificaciones.isEmpty()) {
            notificacionRepository.saveAll(notificaciones);
        }
        return notificaciones;
    }

    @Override
    @Transactional
    public List<Notificacion> marcarTodasComoLeidasUsuarioActual() {
        Usuario usuario = obtenerUsuarioActual();
        List<Notificacion> notificaciones = notificacionRepository
                .findByUsuarioAndEliminadaFalseOrderByFechaCreacionDesc(usuario);

        return actualizarNotificacionesALeidas(notificaciones);
    }

    @Override
    @Transactional
    public Notificacion marcarNotificacionComoLeida(String notificacionId) {
        Usuario usuario = obtenerUsuarioActual();
        Notificacion notificacion = buscarYValidarNotificacion(notificacionId, usuario);
        
        if (!notificacion.isLeida()) {
            notificacion.setLeida(true);
            notificacionRepository.save(notificacion);
        }
        return notificacion;
    }

    @Override
    @Transactional
    public Notificacion eliminarNotificacionUsuarioActual(String notificacionId) {
        Usuario usuario = obtenerUsuarioActual();
        Notificacion notificacion = buscarYValidarNotificacion(notificacionId, usuario);
        
        return softDeleteNotificacion(notificacion);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarNoLeidasUsuarioActual() {
        Usuario usuario = obtenerUsuarioActual();
        return notificacionRepository.countByUsuarioAndLeidaFalseAndEliminadaFalse(usuario);
    }

    private Notificacion softDeleteNotificacion(Notificacion notificacion) {
        notificacion.setEliminada(true);
        return notificacionRepository.save(notificacion);
    }

}
