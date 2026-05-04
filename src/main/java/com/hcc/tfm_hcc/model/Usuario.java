package com.hcc.tfm_hcc.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
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
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un usuario del sistema HCC.
 * Implementa UserDetails de Spring Security para integración con el sistema de autenticación.
 * Incluye encriptación AES para datos sensibles y gestión completa del ciclo de vida del usuario.
 * 
 * <p>Esta entidad gestiona:</p>
 * <ul>
 *   <li>Información personal y de contacto del usuario</li>
 *   <li>Credenciales de acceso y seguridad</li>
 *   <li>Datos específicos para perfiles médicos (especialidad)</li>
 *   <li>Control de estado y ciclo de vida de la cuenta</li>
 *   <li>Integración con Spring Security</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Data
@Table(name = "usuario")  
public class Usuario extends BaseEntity implements UserDetails {

    private static final long serialVersionUID = 18L;

    /**
     * Nombre del usuario.
     * Campo encriptado para protección de datos personales.
     */
    @Column(name = "nombre", nullable = false)
    @Convert(converter = AESEncryptionConverter.class)
    private String nombre;

    /**
     * Primer apellido del usuario.
     * Campo encriptado para protección de datos personales.
     */
    @Column(name = "apellido1", nullable = false)
    @Convert(converter = AESEncryptionConverter.class)
    private String apellido1;

    /**
     * Segundo apellido del usuario (opcional).
     * Campo encriptado para protección de datos personales.
     */
    @Column(name = "apellido2")
    @Convert(converter = AESEncryptionConverter.class)
    private String apellido2;

    /**
     * Dirección de correo electrónico del usuario.
     * Debe ser única en el sistema. Campo encriptado.
     */
    @Column(name = "email", nullable = false, unique = true)
    @Convert(converter = AESEncryptionConverter.class)
    private String email;

    /**
     * Contraseña encriptada del usuario.
     * Se almacena con hash y encriptación adicional.
     */
    @Column(name = "password", nullable = false)
    @Convert(converter = AESEncryptionConverter.class)
    private String password;

    /**
     * Fecha de nacimiento del usuario.
     * Utilizada para validaciones de identidad.
     */
    @Column(name = "fecha_nacimiento")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaNacimiento;

    /**
     * NIF (Número de Identificación Fiscal) del usuario.
     * Identificador único encriptado utilizado como username.
     */
    @Column(name = "nif", nullable = false, unique = true)
    @Convert(converter = AESEncryptionConverter.class)
    private String nif;

    /**
     * Número de teléfono del usuario (opcional).
     * Campo encriptado para protección de datos de contacto.
     */
    @Column(name = "telefono")
    @Convert(converter = AESEncryptionConverter.class)
    private String telefono;

    /**
     * Especialidad médica del usuario.
     * Solo aplicable para usuarios con perfil médico.
     */
    @Column(name = "especialidad")
    private String especialidad;

    /**
     * Estado actual de la cuenta del usuario.
     * Valores posibles: "ACTIVO", "ELIMINADO", "SUSPENDIDO", etc.
     */
    @Column(name = "estado_cuenta")
    private String estadoCuenta;

    /**
     * Fecha y hora cuando se eliminó la cuenta.
     * Se utiliza para soft delete y auditoría.
     */
    @Column(name = "fecha_eliminacion")
    private LocalDateTime fechaEliminacion;

    /**
     * Fecha y hora del último cambio de contraseña.
     * Utilizada para invalidar tokens JWT anteriores.
     */
    @Column(name = "last_password_change")
    private LocalDateTime lastPasswordChange;

    /**
     * Autoridades de Spring Security asignadas al usuario.
     * Se carga dinámicamente y no se persiste.
     */
    @Transient
    private transient Collection<? extends GrantedAuthority> authorities = Collections.emptyList();

    /**
     * Retorna el NIF como nombre de usuario para Spring Security.
     * 
     * @return NIF del usuario
     */
    @Override
    public String getUsername() {
        return nif;
    }

    /**
     * Indica si la cuenta del usuario no ha expirado.
     * 
     * @return true si la cuenta no ha expirado
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indica si la cuenta del usuario no está bloqueada.
     * 
     * @return true si la cuenta no está bloqueada
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indica si las credenciales del usuario no han expirado.
     * 
     * @return true si las credenciales no han expirado
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }   

    /**
     * Indica si la cuenta del usuario está habilitada.
     * 
     * @return true si la cuenta está habilitada
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Retorna las autoridades concedidas al usuario.
     * 
     * @return Collection de GrantedAuthority del usuario
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return (authorities != null) ? authorities : Collections.emptyList();
    }

    /**
     * Establece las autoridades del usuario.
     * 
     * @param authorities Collection de GrantedAuthority a asignar
     */
    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = (authorities != null) ? authorities : Collections.emptyList();
    }
}  