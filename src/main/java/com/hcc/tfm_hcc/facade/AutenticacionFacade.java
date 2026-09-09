package com.hcc.tfm_hcc.facade;

import org.springframework.http.ResponseEntity;

import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.GoogleAuthenticationException;
import com.hcc.tfm_hcc.exception.IncorrectCredentials;
import com.hcc.tfm_hcc.model.LoginResponse;

/**
 * Facade de autenticación para el sistema HCC.
 * Proporciona una interfaz unificada para las operaciones de autenticación
 * y registro de usuarios en el sistema.
 * 
 * <p>Este facade encapsula:</p>
 * <ul>
 *   <li>Proceso de autenticación con credenciales</li>
 *   <li>Generación de tokens JWT para sesiones</li>
 *   <li>Registro de nuevos usuarios en el sistema</li>
 *   <li>Validación de credenciales y manejo de errores</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface AutenticacionFacade {

    /**
     * Realiza el proceso de autenticación de un usuario.
     * Valida las credenciales y, si el usuario no tiene activado el segundo factor,
     * genera directamente un token JWT para el acceso al sistema. Si lo tiene activo,
     * en su lugar devuelve un reto de segundo factor pendiente
     * (LoginResponse#isRequiresTwoFactor() a true, sin token) a completar con
     * {@link #autenticarSegundoFactor(String, String)}.
     *
     * @param loginUsuarioDTO Credenciales del usuario (NIF y contraseña)
     * @return ResponseEntity con LoginResponse: token JWT si el login se completa, o un
     *         reto de segundo factor pendiente si el usuario tiene TOTP activo
     * @throws IncorrectCredentials si las credenciales son incorrectas o el usuario no existe
     */
    ResponseEntity<LoginResponse> autenticar(LoginUsuarioDTO loginUsuarioDTO) throws IncorrectCredentials;

    /**
     * Completa el login de un usuario con segundo factor activo, verificando el
     * código TOTP contra el reto emitido por {@link #autenticar(LoginUsuarioDTO)}.
     *
     * @param challengeId identificador del reto de segundo factor pendiente
     * @param code código de 6 dígitos de la aplicación autenticadora
     * @return ResponseEntity con LoginResponse que contiene el token JWT y tiempo de expiración
     * @throws IncorrectCredentials si el reto no existe, ha caducado o el código no es válido
     */
    ResponseEntity<LoginResponse> autenticarSegundoFactor(String challengeId, String code) throws IncorrectCredentials;

    /**
     * Registra un nuevo usuario en el sistema.
     * Crea una cuenta nueva con los datos proporcionados y asigna perfiles básicos.
     *
     * @param usuarioDTO Datos completos del usuario a registrar
     * @return ResponseEntity con el UsuarioDTO del usuario registrado exitosamente
     * @throws IllegalArgumentException si los datos son inválidos o el usuario ya existe
     */
    ResponseEntity<UsuarioDTO> registrar(UsuarioDTO usuarioDTO) throws IllegalArgumentException;

    /**
     * Inicia el flujo OAuth2 para autenticación con Google.
     *
     * @return ResponseEntity de redirección al proveedor Google
     */
    ResponseEntity<Void> iniciarLoginGoogle();

    /**
     * Completa el login con Google una vez Spring Security ha validado la identidad del usuario
     * ante Google: busca la cuenta existente asociada al email y emite un código de un solo uso
     * que envuelve su token JWT.
     *
     * @param email email verificado por Google
     * @param emailVerificado indica si Google ha verificado ese email
     * @return código de un solo uso a entregar al frontend
     * @throws GoogleAuthenticationException si no existe una cuenta asociada o el email no está verificado
     */
    String procesarLoginGoogle(String email, boolean emailVerificado) throws GoogleAuthenticationException;

    /**
     * Canjea el código de un solo uso emitido tras un login con Google por el token JWT real.
     *
     * @param code código recibido del frontend
     * @return ResponseEntity con el LoginResponse (token JWT y expiración)
     * @throws GoogleAuthenticationException si el código no es válido, ya se usó o ha caducado
     */
    ResponseEntity<LoginResponse> canjearCodigoGoogle(String code) throws GoogleAuthenticationException;
}
