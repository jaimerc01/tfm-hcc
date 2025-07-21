package com.hcc.tfm_hcc.dto;

import java.time.LocalDateTime;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

public class UsuarioDTO {
    
    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private String nombre;

    @Getter
    @Setter
    private String apellido1;

    @Getter
    @Setter
    private String apellido2;

    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    private String password;

    @Getter
    @Setter
    private Date fechaNacimiento;

    @Getter
    @Setter
    private String nif;

    @Getter
    @Setter
    private String telefono;
    
    @Getter
    @Setter
    private String especialidad;

    @Getter
    @Setter
    private LocalDateTime fechaCreacion;

    @Getter
    @Setter
    private LocalDateTime fechaUltimaModificacion;

}
