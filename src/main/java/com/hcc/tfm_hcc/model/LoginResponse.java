package com.hcc.tfm_hcc.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase de respuesta para el proceso de autenticación en el sistema HCC.
 * Encapsula la información devuelta al cliente después de un login exitoso,
 * incluyendo el token de acceso y su tiempo de expiración.
 * 
 * <p>Esta clase contiene:</p>
 * <ul>
 *   <li>Token JWT para autenticación de subsequentes solicitudes</li>
 *   <li>Tiempo de expiración del token en milisegundos</li>
 *   <li>Información necesaria para la gestión de sesión del cliente</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
public class LoginResponse {

    /**
     * Token JWT generado para el usuario autenticado.
     * Debe ser incluido en el header Authorization de las subsequentes solicitudes.
     */
    private String token;
    
    /**
     * Tiempo de expiración del token en milisegundos desde la época Unix.
     * El cliente debe renovar el token antes de esta fecha.
     */
    private long expirationTime;

    /**
     * Indica que el NIF y la contraseña eran correctos pero el usuario tiene
     * activado el segundo factor (TOTP): el login todavía no ha terminado.
     * Cuando vale {@code true}, {@link #token} viene vacío y el cliente debe
     * completar el segundo paso ({@code /authentication/login/2fa}) usando
     * {@link #challengeId} y el código de su aplicación autenticadora.
     */
    private boolean requiresTwoFactor;

    /**
     * Identificador del reto de segundo factor pendiente, emitido solo cuando
     * {@link #requiresTwoFactor} es {@code true}.
     */
    private String challengeId;
}
