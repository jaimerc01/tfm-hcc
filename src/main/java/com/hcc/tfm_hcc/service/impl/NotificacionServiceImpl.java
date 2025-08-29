package com.hcc.tfm_hcc.service.impl;

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
import com.hcc.tfm_hcc.service.NotificacionService;

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

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioFacade usuarioFacade;

    /**
     * Obtiene el usuario actual autenticado
     */
    private Usuario obtenerUsuarioActual() {
        UsuarioDTO dto = usuarioFacade.getUsuarioActual();
        if (dto == null) {
            throw new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }
        
        try {
            UUID uid = UUID.fromString(dto.getId());
            return usuarioRepository.findById(uid)
                    .orElseThrow(() -> new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO));
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
        try {
            UUID nid = UUID.fromString(notificacionId);
            Notificacion notificacion = notificacionRepository.findById(nid)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ERROR_NOTIFICACION_NO_ENCONTRADA));
            
            validarPropietarioNotificacion(notificacion, usuario);
            return notificacion;
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals(ErrorMessages.ERROR_NOTIFICACION_NO_ENCONTRADA)) {
                throw e;
            }
            throw new IllegalArgumentException(ErrorMessages.ERROR_FORMATO_INVALIDO, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> listarNotificacionesUsuarioActual() {
        Usuario usuario = obtenerUsuarioActual();
        return notificacionRepository.findByUsuarioOrderByFechaCreacionDesc(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notificacion> listarNotificacionesUsuarioActual(int page, int size) {
        Usuario usuario = obtenerUsuarioActual();
        
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by("fechaCreacion").descending());
            
        return notificacionRepository.findByUsuario(usuario, pageable);
    }

    /**
     * Actualiza todas las notificaciones no leídas a leídas
     */
    private void actualizarNotificacionesALeidas(List<Notificacion> notificaciones) {
        notificaciones.stream()
                .filter(n -> !n.isLeida())
                .forEach(n -> n.setLeida(true));
        
        if (!notificaciones.isEmpty()) {
            notificacionRepository.saveAll(notificaciones);
        }
    }

    @Override
    @Transactional
    public void marcarTodasComoLeidasUsuarioActual() {
        Usuario usuario = obtenerUsuarioActual();
        List<Notificacion> notificaciones = notificacionRepository
                .findByUsuarioOrderByFechaCreacionDesc(usuario);
        
        actualizarNotificacionesALeidas(notificaciones);
    }

    @Override
    @Transactional
    public void marcarNotificacionComoLeida(String notificacionId) {
        Usuario usuario = obtenerUsuarioActual();
        Notificacion notificacion = buscarYValidarNotificacion(notificacionId, usuario);
        
        if (!notificacion.isLeida()) {
            notificacion.setLeida(true);
            notificacionRepository.save(notificacion);
        }
    }

    @Override
    @Transactional
    public void eliminarNotificacionUsuarioActual(String notificacionId) {
        Usuario usuario = obtenerUsuarioActual();
        Notificacion notificacion = buscarYValidarNotificacion(notificacionId, usuario);
        
        notificacionRepository.delete(notificacion);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarNoLeidasUsuarioActual() {
        Usuario usuario = obtenerUsuarioActual();
        return notificacionRepository.countByUsuarioAndLeidaFalse(usuario);
    }

}
