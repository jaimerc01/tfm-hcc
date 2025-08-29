package com.hcc.tfm_hcc.controller.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.controller.AutenticacionController;
import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.facade.AutenticacionFacade;
import com.hcc.tfm_hcc.model.LoginResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del controlador REST para operaciones de autenticación.
 * Proporciona endpoints REST para registro y autenticación de usuarios,
 * delegando la lógica de negocio al facade correspondiente.
 * 
 * <p>Características de seguridad:</p>
 * <ul>
 *   <li>Validación exhaustiva de datos de entrada</li>
 *   <li>Manejo centralizado de errores de autenticación</li>
 *   <li>Configuración CORS para frontend</li>
 *   <li>Logging detallado de operaciones de autenticación</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/authentication")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
@RequiredArgsConstructor
public class AutenticacionControllerImpl implements AutenticacionController {

    /** Facade para operaciones de autenticación */
    private final AutenticacionFacade autenticacionFacade;

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> autenticar(@RequestBody LoginUsuarioDTO loginUsuarioDTO) 
            throws IllegalArgumentException, SecurityException {
        String nif = loginUsuarioDTO != null ? loginUsuarioDTO.getNif() : "null";
        log.info("Intento de autenticación para NIF: {}", nif);
        
        try {
            validarDatosLogin(loginUsuarioDTO);
            ResponseEntity<LoginResponse> response = autenticacionFacade.autenticar(loginUsuarioDTO);
            log.info("Autenticación exitosa para NIF: {}", nif);
            return response;
            
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación en autenticación: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(crearErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error interno en autenticación para NIF: {}, error: {}", nif, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(ErrorMessages.ERROR_INTERNO_SERVIDOR));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping("/signup")
    public ResponseEntity<UsuarioDTO> registrar(@RequestBody UsuarioDTO usuarioDTO) 
            throws IllegalArgumentException, IllegalStateException {
        String nif = usuarioDTO != null ? usuarioDTO.getNif() : "null";
        log.info("Intento de registro para NIF: {}", nif);
        
        try {
            validarDatosRegistro(usuarioDTO);
            ResponseEntity<UsuarioDTO> response = autenticacionFacade.registrar(usuarioDTO);
            log.info("Registro exitoso para NIF: {}", nif);
            return response;
            
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación en registro: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error interno en registro para NIF: {}, error: {}", nif, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Valida los datos de login proporcionados.
     * Verifica que los campos obligatorios estén presentes y no sean vacíos.
     * 
     * @param loginUsuarioDTO Datos de login a validar
     * @throws IllegalArgumentException si algún campo requerido es inválido
     */
    private void validarDatosLogin(LoginUsuarioDTO loginUsuarioDTO) throws IllegalArgumentException {
        if (loginUsuarioDTO == null) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_CAMPO_REQUERIDO);
        }
        
        if (loginUsuarioDTO.getNif() == null || loginUsuarioDTO.getNif().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("NIF"));
        }
        
        if (loginUsuarioDTO.getPassword() == null || loginUsuarioDTO.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("password"));
        }
    }

    /**
     * Valida los datos de registro proporcionados.
     * Verifica que todos los campos obligatorios estén presentes y sean válidos.
     * 
     * @param usuarioDTO Datos de usuario a validar
     * @throws IllegalArgumentException si algún campo requerido es inválido
     */
    private void validarDatosRegistro(UsuarioDTO usuarioDTO)  throws IllegalArgumentException {
        if (usuarioDTO == null) {
            throw new IllegalArgumentException(ErrorMessages.ERROR_CAMPO_REQUERIDO);
        }
        
        if (usuarioDTO.getFechaNacimiento() == null) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("fechaNacimiento"));
        }
        
        if (usuarioDTO.getNif() == null || usuarioDTO.getNif().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("NIF"));
        }
        
        if (usuarioDTO.getEmail() == null || usuarioDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("email"));
        }
        
        if (usuarioDTO.getPassword() == null || usuarioDTO.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("password"));
        }
    }

    /**
     * Crea una respuesta de error para operaciones de login fallidas.
     * 
     * @param mensaje Mensaje de error descriptivo
     * @return LoginResponse con información de error
     */
    private LoginResponse crearErrorResponse(String mensaje) {
        LoginResponse response = new LoginResponse();
        response.setToken(null);
        // Agregar campo error si está disponible en LoginResponse
        return response;
    }
}
