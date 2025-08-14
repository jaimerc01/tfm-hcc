package com.hcc.tfm_hcc.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserExportDTO {
    private String id;
    private String nombre;
    private String apellido1;
    private String apellido2;
    private String email;
    private String nif;
    private String telefono;
    private Date   fechaNacimiento;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaModificacion;
    private String estadoCuenta;
    private LocalDateTime fechaEliminacion;
    private List<AccesoDTO> accesos;

    @Getter
    @Builder
    public static class AccesoDTO {
        private LocalDateTime timestamp;
        private String metodo;
        private String ruta;
        private Integer estado;
        private Long duracionMs;
        private String ip;
        private String userAgent;
    }
}
