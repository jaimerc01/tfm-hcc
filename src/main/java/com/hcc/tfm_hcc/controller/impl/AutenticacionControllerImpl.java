package com.hcc.tfm_hcc.controller.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.constants.RestUrls;
import com.hcc.tfm_hcc.controller.AutenticacionController;
import com.hcc.tfm_hcc.dto.GoogleCodeRequestDTO;
import com.hcc.tfm_hcc.dto.LoginTwoFactorRequestDTO;
import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.RegistroUsuarioRequest;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.facade.AutenticacionFacade;
import com.hcc.tfm_hcc.model.LoginResponse;
import com.hcc.tfm_hcc.exception.GoogleAuthenticationException;
import com.hcc.tfm_hcc.exception.IncorrectCredentials;
import com.hcc.tfm_hcc.exception.InvalidLoginDataException;
import com.hcc.tfm_hcc.exception.InvalidRegistrationDataException;
import com.hcc.tfm_hcc.util.LogMaskUtil;

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
@RequestMapping(RestUrls.AUTH_BASE)
@RequiredArgsConstructor
public class AutenticacionControllerImpl implements AutenticacionController {

    /** Facade para operaciones de autenticación */
    private final AutenticacionFacade autenticacionFacade;

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping(RestUrls.AUTH_LOGIN)
    public ResponseEntity<LoginResponse> autenticar(@RequestBody LoginUsuarioDTO loginUsuarioDTO) 
            throws InvalidLoginDataException, SecurityException {
        String nif = LogMaskUtil.enmascarar(loginUsuarioDTO != null ? loginUsuarioDTO.getNif() : null);
        log.info("Intento de autenticación para NIF: {}", nif);
        
        try {
            validarDatosLogin(loginUsuarioDTO);
            ResponseEntity<LoginResponse> response = autenticacionFacade.autenticar(loginUsuarioDTO);
            log.info("Autenticación exitosa para NIF: {}", nif);
            return response;
            
        } catch (InvalidLoginDataException e) {
            log.warn("Error de validación en autenticación: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IncorrectCredentials e) {
            // Credenciales bien formadas pero que no autentican (NIF inexistente,
            // contraseña incorrecta, cuenta eliminada). Mismo tratamiento que en la
            // verificación del segundo factor: 401 sin detallar el motivo.
            log.warn("Autenticación rechazada para NIF: {} - credenciales incorrectas", nif);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error interno en autenticación para NIF: {}, error: {}", nif, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping(RestUrls.AUTH_SIGNUP)
    public ResponseEntity<UsuarioDTO> registrar(@RequestBody RegistroUsuarioRequest request)
            throws InvalidRegistrationDataException, IllegalStateException {
        String nif = LogMaskUtil.enmascarar(request != null ? request.getNif() : null);
        log.info("Intento de registro para NIF: {}", nif);

        try {
            validarDatosRegistro(request);
            UsuarioDTO usuarioDTO = mapearARegistroUsuarioDTO(request);
            ResponseEntity<UsuarioDTO> response = autenticacionFacade.registrar(usuarioDTO);
            log.info("Registro exitoso para NIF: {}", nif);
            return response;

        } catch (InvalidRegistrationDataException e) {
            log.warn("Error de validación en registro: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error interno en registro para NIF: {}, error: {}", nif, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping(RestUrls.AUTH_LOGIN_2FA)
    public ResponseEntity<LoginResponse> autenticarSegundoFactor(@RequestBody LoginTwoFactorRequestDTO request) {
        String challengeId = request != null ? request.getChallengeId() : null;
        log.info("Verificando segundo factor para reto: {}", challengeId);

        try {
            ResponseEntity<LoginResponse> response = autenticacionFacade.autenticarSegundoFactor(
                    challengeId, request != null ? request.getCode() : null);
            log.info("Segundo factor verificado correctamente para reto: {}", challengeId);
            return response;
        } catch (IncorrectCredentials e) {
            log.warn("Verificación de segundo factor rechazada para reto: {} - {}", challengeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error interno al verificar segundo factor para reto: {}, error: {}", challengeId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.AUTH_GOOGLE_LOGIN)
    public ResponseEntity<Void> iniciarLoginGoogle() {
        log.info("Iniciando flujo OAuth de Google para login");
        return autenticacionFacade.iniciarLoginGoogle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping(RestUrls.AUTH_GOOGLE_TOKEN)
    public ResponseEntity<LoginResponse> intercambiarCodigoGoogle(@RequestBody GoogleCodeRequestDTO request) {
        String code = request != null ? request.getCode() : null;

        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            return autenticacionFacade.canjearCodigoGoogle(code);
        } catch (GoogleAuthenticationException e) {
            log.warn("Canje de código de login con Google rechazado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Valida los datos de login proporcionados.
     * Verifica que los campos obligatorios estén presentes y no sean vacíos.
     * 
     * @param loginUsuarioDTO Datos de login a validar
     * @throws InvalidLoginDataException si algún campo requerido es inválido
     */
    private void validarDatosLogin(LoginUsuarioDTO loginUsuarioDTO) throws InvalidLoginDataException {
        if (loginUsuarioDTO == null) {
            throw new InvalidLoginDataException(ErrorMessages.ERROR_CAMPO_REQUERIDO);
        }
        
        if (loginUsuarioDTO.getNif() == null || loginUsuarioDTO.getNif().trim().isEmpty()) {
            throw new InvalidLoginDataException(ErrorMessages.campoRequerido("NIF"));
        }
        
        if (loginUsuarioDTO.getPassword() == null || loginUsuarioDTO.getPassword().trim().isEmpty()) {
            throw new InvalidLoginDataException(ErrorMessages.campoRequerido("password"));
        }
    }

    /**
     * Valida los datos de registro proporcionados.
     * Verifica que todos los campos obligatorios estén presentes y sean válidos.
     *
     * @param request Datos de registro a validar
     * @throws InvalidRegistrationDataException si algún campo requerido es inválido
     */
    private void validarDatosRegistro(RegistroUsuarioRequest request) throws InvalidRegistrationDataException {
        if (request == null) {
            throw new InvalidRegistrationDataException(ErrorMessages.ERROR_CAMPO_REQUERIDO);
        }

        if (request.getFechaNacimiento() == null) {
            throw new InvalidRegistrationDataException(ErrorMessages.campoRequerido("fechaNacimiento"));
        }

        if (request.getNif() == null || request.getNif().trim().isEmpty()) {
            throw new InvalidRegistrationDataException(ErrorMessages.campoRequerido("NIF"));
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new InvalidRegistrationDataException(ErrorMessages.campoRequerido("email"));
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new InvalidRegistrationDataException(ErrorMessages.campoRequerido("password"));
        }
    }

    /**
     * Traduce el cuerpo público de registro al {@link UsuarioDTO} interno que consume
     * el resto de capas, copiando explícitamente solo los campos que un registro puede
     * fijar. Así, aunque el cliente incluya en el JSON campos que no existen en
     * {@link RegistroUsuarioRequest} (id, estadoCuenta, especialidad...), es imposible
     * que lleguen a la entidad persistida -- ver el Javadoc de {@link RegistroUsuarioRequest}.
     */
    private UsuarioDTO mapearARegistroUsuarioDTO(RegistroUsuarioRequest request) {
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setNombre(request.getNombre());
        usuarioDTO.setApellido1(request.getApellido1());
        usuarioDTO.setApellido2(request.getApellido2());
        usuarioDTO.setNif(request.getNif());
        usuarioDTO.setEmail(request.getEmail());
        usuarioDTO.setPassword(request.getPassword());
        usuarioDTO.setTelefono(request.getTelefono());
        usuarioDTO.setFechaNacimiento(request.getFechaNacimiento());
        return usuarioDTO;
    }
}
