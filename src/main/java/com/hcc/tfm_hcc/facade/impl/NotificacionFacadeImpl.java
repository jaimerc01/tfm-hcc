package com.hcc.tfm_hcc.facade.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.facade.NotificacionFacade;
import com.hcc.tfm_hcc.model.Notificacion;
import com.hcc.tfm_hcc.service.NotificacionService;

@Component
public class NotificacionFacadeImpl implements NotificacionFacade {

    @Autowired
    private NotificacionService notificacionService;

    @Override
    public List<Map<String, Object>> listarNotificacionesUsuarioActual() {
        var list = notificacionService.listarNotificacionesUsuarioActual();
        return list.stream().map(n -> {
            Map<String, Object> m = Map.of(
                "id", n.getId(),
                "mensaje", n.getMensaje(),
                "leida", n.isLeida(),
                "fechaCreacion", n.getFechaCreacion(),
                "enlace", computeEnlace(n)
            );
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public void marcarTodasComoLeidasUsuarioActual() {
        notificacionService.marcarTodasComoLeidasUsuarioActual();
    }

    @Override
    public void marcarNotificacionComoLeida(String id) {
        notificacionService.marcarNotificacionComoLeida(id);
    }

    @Override
    public void eliminarNotificacionUsuarioActual(String id) {
        notificacionService.eliminarNotificacionUsuarioActual(id);
    }

    @Override
    public java.util.Map<String, Object> listarNotificacionesUsuarioActual(int page, int size) {
        var p = notificacionService.listarNotificacionesUsuarioActual(page, size);
        var items = p.stream().map(n -> java.util.Map.of(
            "id", n.getId(),
            "mensaje", n.getMensaje(),
            "leida", n.isLeida(),
            "fechaCreacion", n.getFechaCreacion(),
            "enlace", computeEnlace(n)
        )).toList();
        return java.util.Map.of("items", items, "total", p.getTotalElements());
    }

    @Override
    public long contarNoLeidasUsuarioActual() {
        return notificacionService.contarNoLeidasUsuarioActual();
    }

    private String computeEnlace(Notificacion n) {
        String msg = n.getMensaje() == null ? "" : n.getMensaje().toLowerCase();
        if (msg.contains("solicitud") || msg.contains("asignación") || msg.contains("asignacion")) return "/mis-solicitudes";
        if (msg.contains("historia clínica") || msg.contains("historia clinica")) return "/historia-clinica";
        return null;
    }
}
