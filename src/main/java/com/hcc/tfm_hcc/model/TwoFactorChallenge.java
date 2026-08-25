package com.hcc.tfm_hcc.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reto pendiente de segundo factor (TOTP) durante el login.
 *
 * <p>Cuando un usuario con 2FA activo introduce correctamente su NIF y contraseña,
 * el login no emite un JWT todavía: crea uno de estos retos (identificado por
 * {@code id}, un valor aleatorio que el cliente debe presentar junto al código de
 * su aplicación autenticadora) y lo guarda en MongoDB, igual que el código de un
 * solo uso del login con Google (mismo motivo: sobrevivir a un reinicio y funcionar
 * con varias instancias del backend).</p>
 *
 * <p>El campo {@code fechaExpiracion} tiene un índice TTL que hace que MongoDB
 * elimine el reto automáticamente pasados unos minutos, limitando la ventana en la
 * que un reto capturado (por ejemplo, en un log) podría explotarse. El campo
 * {@code intentos} limita además cuántos códigos incorrectos se pueden probar
 * contra un mismo reto antes de invalidarlo, para dificultar la fuerza bruta sobre
 * el código de 6 dígitos.</p>
 */
@Document(collection = "two_factor_challenge")
@Data
@NoArgsConstructor
public class TwoFactorChallenge {

    @Id
    private String id;

    @Field("usuario_id")
    private String usuarioId;

    @Field("intentos")
    private int intentos;

    @Indexed(name = "idx_two_factor_challenge_expiracion", expireAfterSeconds = 0)
    @Field("fecha_expiracion")
    private Instant fechaExpiracion;
}
