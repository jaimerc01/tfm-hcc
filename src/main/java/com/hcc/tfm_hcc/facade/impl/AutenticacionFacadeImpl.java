package com.hcc.tfm_hcc.facade.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.IncorrectCredentials;
import com.hcc.tfm_hcc.facade.AutenticacionFacade;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.LoginResponse;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.service.AutenticacionService;
import com.hcc.tfm_hcc.service.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del facade de autenticación para el sistema HCC.
 * Proporciona operaciones de autenticación y registro de usuarios con
 * gestión de tokens JWT y validación de credenciales.
 * 
 * <p>Esta implementación gestiona:</p>
 * <ul>
 *   <li>Autenticación segura de usuarios con credenciales</li>
 *   <li>Generación y configuración de tokens JWT con authorities</li>
 *   <li>Registro de nuevos usuarios con validación exhaustiva</li>
 *   <li>Logging estructurado para auditoría de accesos</li>
 *   <li>Manejo robusto de errores de autenticación</li>
 * </ul>
 * 
 * <p>Características de seguridad:</p>
 * <ul>
 *   <li>Validación de credenciales mediante servicios especializados</li>
 *   <li>Inclusión de authorities en tokens JWT para autorización</li>
 *   <li>Logging detallado para auditoría y monitoreo</li>
 *   <li>Manejo específico de excepciones de credenciales incorrectas</li>
 * </ul>
 * 
 * <p>La clase implementa el patrón de inyección de dependencias mediante constructor
 * y utiliza logging estructurado para facilitar la auditoría de operaciones de autenticación.</p>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 * @see AutenticacionFacade
 * @see AutenticacionService
 * @see JwtService
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutenticacionFacadeImpl implements AutenticacionFacade {

    /**
     * Servicio de autenticación que maneja la validación de credenciales
     * y el proceso de autenticación de usuarios.
     */
    private final AutenticacionService autenticacionService;

    /**
     * Servicio de gestión de tokens JWT para generación, validación
     * y configuración de claims de autorización.
     */
    private final JwtService jwtService;

    /**
     * Mapper para transformación entre entidades Usuario y UsuarioDTO.
     * Facilita la conversión de datos entre capas del sistema.
     */
    private final UsuarioMapper usuarioMapper;
    
    /**
     * {@inheritDoc}
     * 
     * <p>Implementación que realiza el proceso completo de autenticación:</p>
     * <ul>
     *   <li>Validación de credenciales mediante el servicio de autenticación</li>
     *   <li>Generación de token JWT con authorities incluidas</li>
     *   <li>Configuración de respuesta con token y tiempo de expiración</li>
     * </ul>
     * 
     * @param loginUsuarioDTO Credenciales del usuario (NIF y contraseña)
     * @return ResponseEntity con LoginResponse conteniendo token JWT
     * @throws IncorrectCredentials si las credenciales son incorrectas
     */
    @Override
    public ResponseEntity<LoginResponse> autenticar(LoginUsuarioDTO loginUsuarioDTO) throws IncorrectCredentials {
        log.debug("Iniciando proceso de autenticación para usuario: {}", 
                 loginUsuarioDTO != null ? loginUsuarioDTO.getNif() : "null");
        
        try {
            validarDatosAutenticacion(loginUsuarioDTO);
            
            Usuario usuarioAutenticado = autenticacionService.autenticar(loginUsuarioDTO);
            String jwtToken = generarTokenConAuthorities(usuarioAutenticado);
            
            LoginResponse loginResponse = construirLoginResponse(jwtToken);
            
            log.info("Autenticación exitosa para usuario: {}", 
                    loginUsuarioDTO != null ? loginUsuarioDTO.getNif() : "unknown");
            return ResponseEntity.ok(loginResponse);
        } catch (IncorrectCredentials e) {
            log.warn("Intento de autenticación fallido para usuario: {} - Credenciales incorrectas", 
                    loginUsuarioDTO != null ? loginUsuarioDTO.getNif() : "null");
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado durante autenticación para usuario: {} - Error: {}", 
                     loginUsuarioDTO != null ? loginUsuarioDTO.getNif() : "null", e.getMessage(), e);
            throw new RuntimeException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Implementación que gestiona el registro completo de nuevos usuarios:</p>
     * <ul>
     *   <li>Validación exhaustiva de datos de entrada</li>
     *   <li>Registro del usuario mediante el servicio de autenticación</li>
     *   <li>Conversión de entidad a DTO para respuesta</li>
     * </ul>
     * 
     * @param usuarioDTO Datos completos del usuario a registrar
     * @return ResponseEntity con el UsuarioDTO del usuario registrado
     * @throws IllegalArgumentException si los datos son inválidos
     */
    @Override
    public ResponseEntity<UsuarioDTO> registrar(UsuarioDTO usuarioDTO) throws IllegalArgumentException {
        log.debug("Iniciando proceso de registro para usuario: {}", 
                 usuarioDTO != null ? usuarioDTO.getNif() : "null");
        
        try {
            validarDatosRegistro(usuarioDTO);
            
            Usuario usuarioRegistrado = autenticacionService.registrar(usuarioDTO);
            UsuarioDTO usuarioResponse = usuarioMapper.toDto(usuarioRegistrado);
            
            log.info("Usuario registrado exitosamente: {}", 
                    usuarioDTO != null ? usuarioDTO.getNif() : "unknown");
            return ResponseEntity.ok(usuarioResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación en registro de usuario: {} - Error: {}", 
                    usuarioDTO != null ? usuarioDTO.getNif() : "null", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado durante registro de usuario: {} - Error: {}", 
                     usuarioDTO != null ? usuarioDTO.getNif() : "null", e.getMessage(), e);
            throw new RuntimeException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
        }
    }

    // ===============================
    // MÉTODOS UTILITARIOS PRIVADOS
    // ===============================

    /**
     * Valida los datos de autenticación proporcionados.
     * 
     * @param loginUsuarioDTO Datos de login a validar
     * @throws IllegalArgumentException si los datos son inválidos
     */
    private void validarDatosAutenticacion(LoginUsuarioDTO loginUsuarioDTO) throws IllegalArgumentException {
        if (loginUsuarioDTO == null) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_CAMPO_REQUERIDO);
        }
        
        if (loginUsuarioDTO.getNif() == null || loginUsuarioDTO.getNif().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("NIF"));
        }
        
        if (loginUsuarioDTO.getPassword() == null || loginUsuarioDTO.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("contraseña"));
        }
    }

    /**
     * Genera un token JWT incluyendo las authorities del usuario.
     * 
     * @param usuario Usuario autenticado
     * @return Token JWT con authorities incluidas
     */
    private String generarTokenConAuthorities(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        
        // Incluir authorities en el token para que el frontend pueda hacer guards sin llamadas extra
        var authorities = usuario.getAuthorities();
        if (authorities != null && !authorities.isEmpty()) {
            claims.put("authorities", authorities.stream()
                                                .map(a -> a.getAuthority())
                                                .toList());
        }
        
        return jwtService.generateToken(claims, usuario);
    }

    /**
     * Construye la respuesta de login con token y tiempo de expiración.
     * 
     * @param jwtToken Token JWT generado
     * @return LoginResponse con token y expiración
     */
    private LoginResponse construirLoginResponse(String jwtToken) {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpirationTime(jwtService.getExpirationTime());
        return loginResponse;
    }

    /**
     * Valida los datos de registro de usuario.
     * 
     * @param usuarioDTO Datos del usuario a validar
     * @throws IllegalArgumentException si los datos son inválidos
     */
    private void validarDatosRegistro(UsuarioDTO usuarioDTO) throws IllegalArgumentException {
        if (usuarioDTO == null) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_CAMPO_REQUERIDO);
        }
        
        if (usuarioDTO.getNif() == null || usuarioDTO.getNif().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("NIF"));
        }
        
        if (usuarioDTO.getNombre() == null || usuarioDTO.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("nombre"));
        }
        
        if (usuarioDTO.getApellido1() == null || usuarioDTO.getApellido1().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("primer apellido"));
        }
        
        if (usuarioDTO.getEmail() == null || usuarioDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("email"));
        }
        
        if (usuarioDTO.getPassword() == null || usuarioDTO.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("contraseña"));
        }
    }
}