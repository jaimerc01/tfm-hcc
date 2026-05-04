package com.hcc.tfm_hcc.dto;

import java.time.LocalDateTime;
import java.util.Date;

import lombok.Data;

@Data
public class UsuarioDTO {
    
    private String id;
    private String nombre;
    private String apellido1;
    private String apellido2;
    private String email;
    private String password;
    private Date fechaNacimiento;
    private String nif;
    private String telefono;
    private String especialidad;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaModificacion;
    private LocalDateTime lastPasswordChange;
    private String estadoCuenta;
    private LocalDateTime fechaEliminacion;

}
