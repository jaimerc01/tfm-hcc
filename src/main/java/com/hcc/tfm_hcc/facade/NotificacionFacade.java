package com.hcc.tfm_hcc.facade;

import java.util.List;
import java.util.Map;

public interface NotificacionFacade {
    List<Map<String, Object>> listarNotificacionesUsuarioActual();
    void marcarTodasComoLeidasUsuarioActual();
    void marcarNotificacionComoLeida(String id);
    void eliminarNotificacionUsuarioActual(String id);
    java.util.Map<String, Object> listarNotificacionesUsuarioActual(int page, int size);
    long contarNoLeidasUsuarioActual();
}
