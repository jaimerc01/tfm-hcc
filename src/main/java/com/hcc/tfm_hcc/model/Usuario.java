package com.hcc.tfm_hcc.model;

import java.util.Date;

import com.hcc.tfm_hcc.converter.AESEncryptionConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "usuario")  
public class Usuario extends BaseEntity {

    private static final long serialVersionUID = 18L;

    @Column(name = "nombre", nullable = false)
    @Convert(converter = AESEncryptionConverter.class)
    private String nombre;

    @Column(name = "apellido1", nullable = false)
    @Convert(converter = AESEncryptionConverter.class)
    private String apellido1;

    @Column(name = "apellido2")
    @Convert(converter = AESEncryptionConverter.class)
    private String apellido2;

    @Column(name = "email", nullable = false, unique = true)
    @Convert(converter = AESEncryptionConverter.class)
    private String email;

    @Column(name = "password", nullable = false)
    @Convert(converter = AESEncryptionConverter.class)
    private String password;

    @Column(name = "fecha_nacimiento")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaNacimiento;

    @Column(name = "nif", nullable = false, unique = true)
    @Convert(converter = AESEncryptionConverter.class)
    private String nif;

    @Column(name = "telefono")
    @Convert(converter = AESEncryptionConverter.class)
    private String telefono;

    @Column(name = "especialidad")
    private String especialidad;

}  