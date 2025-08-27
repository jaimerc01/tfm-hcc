package com.hcc.tfm_hcc.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hcc.tfm_hcc.model.Notificacion;
import com.hcc.tfm_hcc.repository.NotificacionRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.NotificacionService;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.facade.UsuarioFacade;

@Service
public class NotificacionServiceImpl implements NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioFacade usuarioFacade;

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> listarNotificacionesUsuarioActual() {
        UsuarioDTO dto = usuarioFacade.getUsuarioActual();
        if (dto == null) return List.of();
        try {
            UUID uid = UUID.fromString(dto.getId());
            return usuarioRepository.findById(uid)
                .map(u -> notificacionRepository.findByUsuarioOrderByFechaCreacionDesc(u))
                .orElse(List.of());
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Notificacion> listarNotificacionesUsuarioActual(int page, int size) {
        UsuarioDTO dto = usuarioFacade.getUsuarioActual();
        if (dto == null) return org.springframework.data.domain.Page.empty();
        try {
            UUID uid = UUID.fromString(dto.getId());
            var pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("fechaCreacion").descending());
            return usuarioRepository.findById(uid)
                .map(u -> notificacionRepository.findByUsuario(u, pageable))
                .orElse(org.springframework.data.domain.Page.empty());
        } catch (Exception e) {
            return org.springframework.data.domain.Page.empty();
        }
    }

    @Override
    @Transactional
    public void marcarTodasComoLeidasUsuarioActual() {
        UsuarioDTO dto = usuarioFacade.getUsuarioActual();
        if (dto == null) return;
        try {
            UUID uid = UUID.fromString(dto.getId());
            usuarioRepository.findById(uid).ifPresent(u -> {
                var notis = notificacionRepository.findByUsuarioOrderByFechaCreacionDesc(u);
            notis.forEach(n -> n.setLeida(true));
            notificacionRepository.saveAll(notis);
            });
        } catch (Exception ignored) {}
    }

    @Override
    @Transactional
    public void marcarNotificacionComoLeida(String id) {
        UsuarioDTO dto = usuarioFacade.getUsuarioActual();
        if (dto == null) return;
        try {
            UUID uid = UUID.fromString(dto.getId());
            UUID nid = UUID.fromString(id);
            usuarioRepository.findById(uid).ifPresent(u -> {
                notificacionRepository.findById(nid).ifPresent(n -> {
                    if (n.getUsuario() != null && n.getUsuario().getId().equals(u.getId())) {
                        n.setLeida(true);
                        notificacionRepository.save(n);
                    }
                });
            });
        } catch (Exception ignored) {}
    }

    @Override
    @Transactional
    public void eliminarNotificacionUsuarioActual(String id) {
        UsuarioDTO dto = usuarioFacade.getUsuarioActual();
        if (dto == null) return;
        try {
            UUID uid = UUID.fromString(dto.getId());
            UUID nid = UUID.fromString(id);
            usuarioRepository.findById(uid).ifPresent(u -> {
                notificacionRepository.findById(nid).ifPresent(n -> {
                    if (n.getUsuario() != null && n.getUsuario().getId().equals(u.getId())) {
                        notificacionRepository.delete(n);
                    }
                });
            });
        } catch (Exception ignored) {}
    }

    @Override
    @Transactional(readOnly = true)
    public long contarNoLeidasUsuarioActual() {
        UsuarioDTO dto = usuarioFacade.getUsuarioActual();
        if (dto == null) return 0L;
        try {
            UUID uid = UUID.fromString(dto.getId());
            return usuarioRepository.findById(uid).map(u -> notificacionRepository.countByUsuarioAndLeidaFalse(u)).orElse(0L);
        } catch (Exception e) {
            return 0L;
        }
    }
}
