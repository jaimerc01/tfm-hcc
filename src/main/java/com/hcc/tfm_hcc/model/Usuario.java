package com.hcc.tfm_hcc.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hcc.tfm_hcc.converter.AESEncryptionConverter;
import com.hcc.tfm_hcc.converter.AESEncryptionLocalDateTimeConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
@NoArgsConstructor
@Entity
@Getter
@Setter
@ToString
@Table(name = "usuario")  
public class Usuario extends BaseEntity implements UserDetails {

    private static final long serialVersionUID = 18L;

    /**
     * Nombre del usuario.
     * Campo encriptado para protección de datos personales.
     */
    @Column(name = "nombre", nullable = false)
    @Convert(converter = AESEncryptionConverter.class)
    @ToString.Exclude
    private String nombre;

    /**
     * Primer apellido del usuario.
     * Campo encriptado para protección de datos personales.
     */
    @Column(name = "apellido1", nullable = false)
    @Convert(converter = AESEncryptionConverter.class)
    @ToString.Exclude
    private String apellido1;

    /**
     * Segundo apellido del usuario (opcional).
     * Campo encriptado para protección de datos personales.
     */
    @Column(name = "apellido2")
    @Convert(converter = AESEncryptionConverter.class)
    @ToString.Exclude
    private String apellido2;

    /**
     * Dirección de correo electrónico del usuario.
     * Campo cifrado con AES/GCM (no determinista): la unicidad y la búsqueda por
     * igualdad ya no se aplican sobre esta columna, sino sobre {@link #emailHash}.
     */
    @Column(name = "email", nullable = false)
    @Convert(converter = AESEncryptionConverter.class)
    @ToString.Exclude
    private String email;

    /**
     * Índice de búsqueda determinista del email (HMAC-SHA256), calculado por
     * {@link com.hcc.tfm_hcc.service.HmacSearchIndexService}. Único en el sistema:
     * sustituye a la antigua restricción de unicidad sobre la columna cifrada.
     */
    @Column(name = "email_hash", nullable = false, unique = true)
    @JsonIgnore
    @ToString.Exclude
    private String emailHash;

    /**
     * Contraseña del usuario almacenada únicamente como hash BCrypt.
     * Nunca debe salir en respuestas JSON ({@link JsonIgnore}) ni en trazas de log
     * ({@link ToString.Exclude}).
     */
    @Column(name = "password", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private String password;

    /**
     * Fecha de nacimiento del usuario.
     * Utilizada para validaciones de identidad, lo que la hace especialmente sensible.
     * Campo cifrado con AES/GCM igual que el resto de datos personales de esta entidad
     * (ver {@link AESEncryptionLocalDateTimeConverter}); la columna es de tipo texto en
     * base de datos, no una fecha nativa.
     */
    @Column(name = "fecha_nacimiento")
    @Convert(converter = AESEncryptionLocalDateTimeConverter.class)
    @ToString.Exclude
    private LocalDateTime fechaNacimiento;

    /**
     * NIF (Número de Identificación Fiscal) del usuario, utilizado como username.
     * Campo cifrado con AES/GCM (no determinista): la unicidad y la búsqueda por
     * igualdad ya no se aplican sobre esta columna, sino sobre {@link #nifHash}.
     */
    @Column(name = "nif", nullable = false)
    @Convert(converter = AESEncryptionConverter.class)
    @ToString.Exclude
    private String nif;

    /**
     * Índice de búsqueda determinista del NIF (HMAC-SHA256), calculado por
     * {@link com.hcc.tfm_hcc.service.HmacSearchIndexService}. Único en el sistema:
     * sustituye a la antigua restricción de unicidad sobre la columna cifrada.
     */
    @Column(name = "nif_hash", nullable = false, unique = true)
    @JsonIgnore
    @ToString.Exclude
    private String nifHash;

    /**
     * Número de teléfono del usuario (opcional).
     * Campo encriptado para protección de datos de contacto.
     */
    @Column(name = "telefono")
    @Convert(converter = AESEncryptionConverter.class)
    @ToString.Exclude
    private String telefono;

    /**
     * Especialidad médica del usuario.
     * Solo aplicable para usuarios con perfil médico.
     */
    @Column(name = "especialidad")
    private String especialidad;

    /**
     * Estado actual de la cuenta del usuario.
     * Valores posibles: {@link #ESTADO_CUENTA_ACTIVO}, {@link #ESTADO_CUENTA_ELIMINADO},
     * {@link #ESTADO_CUENTA_SUSPENDIDO}.
     */
    @Column(name = "estado_cuenta")
    private String estadoCuenta;

    public static final String ESTADO_CUENTA_ACTIVO = "ACTIVO";

    public static final String ESTADO_CUENTA_ELIMINADO = "ELIMINADO";

    /**
     * Cuenta con el tratamiento de sus datos limitado a petición propia (derecho de
     * limitación del tratamiento, art. 18 RGPD): la cuenta sigue existiendo y su titular
     * puede seguir accediendo a ella (para poder revertir la limitación cuando quiera),
     * pero ningún médico puede consultar su historial clínico mientras dure
     * (ver {@code HistorialClinicoServiceImpl.obtenerHistorialPaciente}).
     */
    public static final String ESTADO_CUENTA_SUSPENDIDO = "SUSPENDIDO";

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
     * Secreto TOTP (RFC 6238) del segundo factor de autenticación, en Base32.
     * Campo cifrado con AES/GCM. Nulo si el usuario nunca ha configurado 2FA o lo
     * ha desactivado. Mientras {@link #totpEnabled} sea falso pero este campo no
     * sea nulo, el secreto está "pendiente de confirmar" (generado pero todavía
     * no verificado con un primer código correcto).
     */
    @Column(name = "totp_secret")
    @Convert(converter = AESEncryptionConverter.class)
    @JsonIgnore
    @ToString.Exclude
    private String totpSecret;

    /**
     * Indica si el segundo factor de autenticación (TOTP) está activo para este
     * usuario. Si es true, el login con NIF/contraseña no basta por sí solo: debe
     * completarse además con un código TOTP válido.
     */
    @Column(name = "totp_enabled")
    private boolean totpEnabled;

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
     * Indica si la cuenta del usuario está habilitada para autenticarse.
     *
     * <p>Una cuenta {@link #ESTADO_CUENTA_ELIMINADO} (borrado lógico) no puede iniciar
     * sesión ni obtener un JWT: Spring Security rechaza la autenticación con
     * {@code DisabledException}. El estado {@link #ESTADO_CUENTA_SUSPENDIDO} (limitación
     * del tratamiento, art. 18 RGPD) se considera habilitado a propósito, porque su
     * titular debe poder seguir accediendo para revertir la limitación; lo que se le
     * bloquea es que un médico consulte su historial, no su propio acceso.</p>
     *
     * @return {@code false} solo si la cuenta está eliminada
     */
    @Override
    public boolean isEnabled() {
        return !ESTADO_CUENTA_ELIMINADO.equals(estadoCuenta);
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