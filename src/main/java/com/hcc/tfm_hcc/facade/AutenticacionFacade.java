package com.hcc.tfm_hcc.facade;

import org.springframework.http.ResponseEntity;

import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
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
     * Valida las credenciales y genera un token JWT para el acceso al sistema.
     *
     * @param loginUsuarioDTO Credenciales del usuario (NIF y contraseña)
     * @return ResponseEntity con LoginResponse que contiene el token JWT y tiempo de expiración
     * @throws IncorrectCredentials si las credenciales son incorrectas o el usuario no existe
     */
    ResponseEntity<LoginResponse> autenticar(LoginUsuarioDTO loginUsuarioDTO) throws IncorrectCredentials;

    /**
     * Registra un nuevo usuario en el sistema.
     * Crea una cuenta nueva con los datos proporcionados y asigna perfiles básicos.
     *
     * @param usuarioDTO Datos completos del usuario a registrar
     * @return ResponseEntity con el UsuarioDTO del usuario registrado exitosamente
     * @throws IllegalArgumentException si los datos son inválidos o el usuario ya existe
     */
    ResponseEntity<UsuarioDTO> registrar(UsuarioDTO usuarioDTO) throws IllegalArgumentException;
}
