package com.hcc.tfm_hcc.service;

import java.util.List;

import com.hcc.tfm_hcc.model.Notificacion;

public interface NotificacionService {
    List<Notificacion> listarNotificacionesUsuarioActual();
    void marcarTodasComoLeidasUsuarioActual();
    void marcarNotificacionComoLeida(String id);
    void eliminarNotificacionUsuarioActual(String id);
    // Paginated listing: page is zero-based
    org.springframework.data.domain.Page<com.hcc.tfm_hcc.model.Notificacion> listarNotificacionesUsuarioActual(int page, int size);
    long contarNoLeidasUsuarioActual();
}
