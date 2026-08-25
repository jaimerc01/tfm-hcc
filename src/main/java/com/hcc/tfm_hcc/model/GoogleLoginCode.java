package com.hcc.tfm_hcc.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Código de un solo uso del flujo de login con Google, de vida muy corta.
 *
 * <p>Se guarda en MongoDB (en vez de en un mapa en memoria del proceso) para que
 * sobreviva a un reinicio del servidor y funcione igual si la aplicación llega a
 * desplegarse en varias instancias a la vez: cualquier instancia puede generar el
 * código y cualquier otra puede canjearlo, ya que ambas comparten la misma base
 * de datos.</p>
 *
 * <p>El campo {@code fechaExpiracion} tiene un índice TTL ({@code expireAfterSeconds = 0}):
 * MongoDB elimina el documento automáticamente en cuanto se supera la fecha
 * almacenada en ese campo. Esto es una red de seguridad adicional -el código en
 * {@code AutenticacionServiceImpl} también comprueba la expiración explícitamente
 * al canjearlo-, útil porque el proceso de limpieza TTL de MongoDB no es instantáneo
 * (se ejecuta en un ciclo periódico, típicamente cada 60 segundos).</p>
 *
 * <p>Si el usuario autenticado con Google tiene activado el segundo factor (TOTP),
 * este código no envuelve un JWT ya emitido: envuelve el identificador de un reto
 * de segundo factor pendiente ({@code requiresTwoFactor=true} + {@code challengeId}),
 * que el frontend resuelve con el mismo endpoint que usa el login normal
 * ({@code /authentication/login/2fa}). En ese caso {@link #token} queda vacío.</p>
 */
@Document(collection = "google_login_code")
@Data
@NoArgsConstructor
public class GoogleLoginCode {

    @Id
    private String id;

    @Field("token")
    private String token;

    @Field("expiration_time")
    private long expirationTime;

    @Field("requires_two_factor")
    private boolean requiresTwoFactor;

    @Field("challenge_id")
    private String challengeId;

    @Indexed(name = "idx_google_login_code_expiracion", expireAfterSeconds = 0)
    @Field("fecha_expiracion")
    private Instant fechaExpiracion;
}
