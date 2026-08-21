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
     * Canjea un código de un solo uso emitido por {@link #generarCodigoLoginGoogle} por el
     * token JWT que envuelve. El código se invalida tras el primer canje o al caducar.
     *
     * @param code código recibido del frontend
     * @return respuesta de login con el token JWT y su expiración
     * @throws GoogleAuthenticationException si el código no existe, ya se usó o ha caducado
     */
    LoginResponse canjearCodigoLoginGoogle(String code) throws GoogleAuthenticationException;
}
