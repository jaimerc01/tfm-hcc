package com.hcc.tfm_hcc.model;

import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.hcc.tfm_hcc.converter.AESEncryptionConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "usuario")  
public class Usuario extends BaseEntity implements UserDetails{

    private static final long serialVersionUID = 18L;

    @Getter
    @Setter
    @Column(name = "nombre", nullable = false)
    @Convert(converter = AESEncryptionConverter.class)
    private String nombre;

    @Getter
    @Setter
    @Column(name = "apellido1", nullable = false)
    @Convert(converter = AESEncryptionConverter.class)
    private String apellido1;

    @Getter
    @Setter
    @Column(name = "apellido2")
    @Convert(converter = AESEncryptionConverter.class)
    private String apellido2;

    @Getter
    @Setter
    @Column(name = "email", nullable = false, unique = true)
    @Convert(converter = AESEncryptionConverter.class)
    private String email;

    @Getter
    @Setter
    @Column(name = "password", nullable = false)
    @Convert(converter = AESEncryptionConverter.class)
    private String password;

    @Getter
    @Setter
    @Column(name = "fecha_nacimiento")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaNacimiento;

    @Getter
    @Setter
    @Column(name = "nif", nullable = false, unique = true)
    @Convert(converter = AESEncryptionConverter.class)
    private String nif;

    @Getter
    @Setter
    @Column(name = "telefono")
    @Convert(converter = AESEncryptionConverter.class)
    private String telefono;

    @Getter
    @Setter
    @Column(name = "especialidad")
    private String especialidad;

    @Override
    public String getUsername() {
        return nif;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Implementar lógica si es necesario
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Implementar lógica si es necesario
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Implementar lógica si es necesario
    }   

    @Override
    public boolean isEnabled() {
        return true; // Implementar lógica si es necesario
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Implementar lógica para obtener los roles del usuario
        return java.util.Collections.emptyList(); // Retornar una lista vacía por defecto
    }

}  