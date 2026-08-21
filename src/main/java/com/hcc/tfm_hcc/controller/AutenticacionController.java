package com.hcc.tfm_hcc.controller;

import org.springframework.http.ResponseEntity;

import com.hcc.tfm_hcc.dto.GoogleCodeRequestDTO;
import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.GoogleAuthenticationException;
import com.hcc.tfm_hcc.exception.InvalidLoginDataException;
import com.hcc.tfm_hcc.exception.InvalidRegistrationDataException;
import com.hcc.tfm_hcc.model.LoginResponse;

/**
 * Controlador REST para operaciones de autenticación en el sistema HCC.
 * Proporciona endpoints para el registro de nuevos usuarios y autenticación
 * de usuarios existentes mediante credenciales (NIF/email y contraseña).
 * 
 * <p>Funcionalidades de autenticación:</p>
 * <ul>
 *   <li>Registro de nuevos usuarios con validación completa</li>
 *   <li>Autenticación mediante JWT tokens</li>
 *   <li>Validación de credenciales y datos de usuario</li>
 *   <li>Gestión segura de sesiones</li>
 * </ul>
 * 
 * <p>Los endpoints de este controlador son públicos y no requieren autenticación previa.</p>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface AutenticacionController {

    /**
     * Registra un nuevo usuario en el sistema HCC.
     * Valida los datos proporcionados, verifica que el usuario no exista
     * y crea una nueva cuenta con el perfil correspondiente.
     *
     * @param usuarioDTO Datos completos del usuario a registrar
     * @return ResponseEntity con el UsuarioDTO del usuario registrado exitosamente
     * @throws InvalidRegistrationDataException si los datos del usuario no son válidos
     * @throws IllegalStateException si el usuario ya existe en el sistema
     */
    ResponseEntity<UsuarioDTO> registrar(UsuarioDTO usuarioDTO) throws InvalidRegistrationDataException, IllegalStateException;

    /**
     * Autentica un usuario existente mediante sus credenciales.
     * Valida las credenciales proporcionadas y genera un token JWT
     * para acceso a recursos protegidos del sistema.
     *
     * @param loginUsuarioDTO Credenciales del usuario (NIF/email y contraseña)
     * @return ResponseEntity con LoginResponse que incluye el token JWT y datos del usuario
     * @throws InvalidLoginDataException si las credenciales no son válidas
     * @throws SecurityException si la autenticación falla
     */
    ResponseEntity<LoginResponse> autenticar(LoginUsuarioDTO loginUsuarioDTO) throws InvalidLoginDataException, SecurityException;

    /**
     * Inicia el flujo de autenticación con Google para usuarios existentes.
     * Pensado para navegación completa del navegador (no XHR/fetch): redirige al proveedor
     * OAuth configurado para completar la autenticación. Google solo permite iniciar sesión
     * en cuentas ya registradas por el formulario tradicional; no crea cuentas nuevas.
     *
     * @return ResponseEntity vacío con redirección al flujo OAuth de Google
     */
    ResponseEntity<Void> iniciarLoginGoogle();

    /**
     * Canjea el código de un solo uso recibido tras completar el login con Google
     * por el token JWT real de la aplicación.
     *
     * @param request cuerpo con el código de un solo uso
     * @return ResponseEntity con el LoginResponse (token JWT y expiración)
     * @throws GoogleAuthenticationException si el código no es válido, ya se usó o ha caducado
     */
    ResponseEntity<LoginResponse> intercambiarCodigoGoogle(GoogleCodeRequestDTO request) throws GoogleAuthenticationException;
}
