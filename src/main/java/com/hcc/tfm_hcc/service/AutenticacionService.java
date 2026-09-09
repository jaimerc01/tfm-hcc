package com.hcc.tfm_hcc.service;

import java.net.URI;

import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.GoogleAuthenticationException;
import com.hcc.tfm_hcc.exception.IncorrectCredentials;
import com.hcc.tfm_hcc.model.LoginResponse;
import com.hcc.tfm_hcc.model.Usuario;

public interface AutenticacionService {

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param usuarioDTO los datos del usuario a registrar
     * @return el usuario registrado
     */
    Usuario registrar(UsuarioDTO usuarioDTO);

    /**
     * Autentica a un usuario con las credenciales proporcionadas.
     *
     * @param loginUsuarioDTO objeto que contiene las credenciales del usuario
     * @return el usuario autenticado
     * @throws IncorrectCredentials si las credenciales son incorrectas o el usuario no existe
     */
    Usuario autenticar(LoginUsuarioDTO loginUsuarioDTO) throws IncorrectCredentials;

    /**
     * Obtiene la URI de inicio del flujo OAuth2 con Google.
     *
     * @return URI de autorización OAuth2 para Google
     */
    URI obtenerUriAutorizacionGoogle();

    /**
     * Autentica a un usuario ya registrado a partir de su email de Google.
     * Solo permite el login: no crea cuentas nuevas ni completa datos faltantes.
     *
     * @param email email devuelto por Google en el perfil OIDC
     * @param emailVerificado indica si Google ha verificado ese email
     * @return el usuario existente asociado a ese email, con sus authorities cargadas
     * @throws GoogleAuthenticationException si el email no está verificado o no existe una cuenta asociada
     */
    Usuario autenticarConGoogle(String email, boolean emailVerificado) throws GoogleAuthenticationException;

    /**
     * Genera un código de un solo uso y corta duración que envuelve un token JWT ya emitido,
     * para poder entregárselo al frontend a través de una redirección sin exponer el JWT en la URL.
     *
     * @param token token JWT ya generado para el usuario autenticado con Google
     * @param expirationTime tiempo de expiración del token, en milisegundos
     * @return código de un solo uso
     */
    String generarCodigoLoginGoogle(String token, long expirationTime);

    /**
     * Genera un código de un solo uso y corta duración que envuelve un reto de segundo
     * factor pendiente, para el caso en que el usuario autenticado con Google tenga
     * activado el TOTP. El login con Google todavía no se completa: el frontend debe
     * resolver el reto con el código de su aplicación autenticadora, igual que en el
     * login normal.
     *
     * @param challengeId identificador del reto emitido por {@link #crearChallengeDosFactores}
     * @return código de un solo uso
     */
    String generarCodigoLoginGoogleConDosFactores(String challengeId);

    /**
     * Canjea un código de un solo uso emitido por {@link #generarCodigoLoginGoogle} o por
     * {@link #generarCodigoLoginGoogleConDosFactores}. El código se invalida tras el primer
     * canje o al caducar.
     *
     * @param code código recibido del frontend
     * @return respuesta de login: con el token JWT si el login ya se completó, o con
     *         {@code requiresTwoFactor=true} y el {@code challengeId} si queda pendiente
     *         el segundo factor
     * @throws GoogleAuthenticationException si el código no existe, ya se usó o ha caducado
     */
    LoginResponse canjearCodigoLoginGoogle(String code) throws GoogleAuthenticationException;

    /**
     * Crea un reto de segundo factor (TOTP) pendiente para un usuario cuyo NIF y
     * contraseña ya se han validado. El login no se completa hasta que el reto se
     * resuelva con {@link #verificarCodigoDosFactores}.
     *
     * @param usuario usuario ya autenticado con NIF/contraseña, con 2FA activo
     * @return identificador del reto, a presentar junto al código TOTP
     */
    String crearChallengeDosFactores(Usuario usuario);

    /**
     * Resuelve un reto de segundo factor: comprueba que no haya caducado, que no se
     * hayan agotado los intentos, y que el código TOTP sea válido para el usuario
     * asociado al reto. El reto se consume (deja de poder reutilizarse) tanto si
     * la verificación tiene éxito como si se agotan los intentos.
     *
     * @param challengeId identificador del reto emitido por {@link #crearChallengeDosFactores}
     * @param code código de 6 dígitos de la aplicación autenticadora
     * @return el usuario autenticado, con sus authorities cargadas
     * @throws IncorrectCredentials si el reto no existe, ha caducado o el código no es válido
     */
    Usuario verificarCodigoDosFactores(String challengeId, String code) throws IncorrectCredentials;
}
