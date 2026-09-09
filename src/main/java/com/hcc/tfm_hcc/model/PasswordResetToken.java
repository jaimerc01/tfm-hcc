package com.hcc.tfm_hcc.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token de un solo uso del flujo de restablecimiento de contraseña ("he olvidado
 * mi contraseña"). Se entrega al usuario dentro de un enlace enviado por correo y
 * le permite fijar una contraseña nueva sin conocer la anterior.
 *
 * <p>Se guarda en MongoDB, igual que {@link GoogleLoginCode} y
 * {@link TwoFactorChallenge}, para que sobreviva a un reinicio del servidor y
 * funcione en despliegues con varias instancias.</p>
 *
 * <p><b>Nunca se almacena el token en claro.</b> El documento guarda solo el
 * hash SHA-256 ({@link #tokenHash}) del valor aleatorio que viaja en el enlace;
 * así, un volcado de la base de datos no permite construir enlaces válidos. La
 * búsqueda al canjearlo se hace recalculando el hash del token recibido.</p>
 *
 * <p>El campo {@link #fechaExpiracion} lleva un índice TTL
 * ({@code expireAfterSeconds = 0}): MongoDB borra el documento automáticamente al
 * superarse esa fecha. Es una red de seguridad adicional -{@code PasswordResetServiceImpl}
 * también comprueba la expiración explícitamente al canjear el token-, útil porque
 * el proceso de limpieza TTL de MongoDB no es instantáneo.</p>
 */
@Document(collection = "password_reset_token")
@Data
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    private String id;

    /**
     * Hash SHA-256 (hexadecimal) del token aleatorio que viaja en el enlace de
     * correo. Único: dos solicitudes nunca comparten token.
     */
    @Indexed(name = "idx_password_reset_token_hash", unique = true)
    @Field("token_hash")
    private String tokenHash;

    /**
     * UUID (como texto) del usuario dueño de la cuenta a restablecer.
     */
    @Field("usuario_id")
    private String usuarioId;

    /**
     * Marca de un solo uso: se pone a {@code true} en cuanto el token se canjea
     * con éxito, de modo que un segundo intento con el mismo enlace se rechaza.
     */
    @Field("usado")
    private boolean usado;

    @Field("fecha_creacion")
    private Instant fechaCreacion;

    @Indexed(name = "idx_password_reset_token_expiracion", expireAfterSeconds = 0)
    @Field("fecha_expiracion")
    private Instant fechaExpiracion;
}
