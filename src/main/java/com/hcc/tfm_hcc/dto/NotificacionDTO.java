package com.hcc.tfm_hcc.dto;

import java.time.LocalDateTime;

import com.hcc.tfm_hcc.model.Notificacion;

import lombok.Data;

@Data
public class NotificacionDTO {
    private String id;
    private String mensaje;
    private boolean leida;
    private LocalDateTime fechaCreacion;
    private String enlace;
    private UsuarioDTO usuarioDTO;

    /**
     * Asignar enlace basado en el contenido del mensaje de la notificación.
     * 
     * @param n La notificación para la cual generar el enlace
     * @return String El enlace apropiado o null si no se puede determinar
     */
    public void asignarEnlace(Notificacion n) {
        if (n == null || n.getMensaje() == null) {
            this.enlace = null;
            return;
        }
        
        String msg = n.getMensaje().toLowerCase();
        if (msg.contains("solicitud") || msg.contains("asignación") || msg.contains("asignacion")) {
            this.enlace = "/mis-solicitudes";
            return;
        }
        if (msg.contains("historia clínica") || msg.contains("historia clinica")) {
            this.enlace = "/historia-clinica";
            return;
        }
        this.enlace = null;
    }
    
}


