package com.hcc.tfm_hcc.service.impl;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.constants.RestUrls;
import com.hcc.tfm_hcc.converter.UsuarioConverter;
import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.GoogleAuthenticationException;
import com.hcc.tfm_hcc.exception.IncorrectCredentials;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.model.LoginResponse;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.AutenticacionService;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio de autenticación.
 * Proporciona funcionalidades para registro y autenticación de usuarios.
 * 
 * @author Sistema HCC
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class AutenticacionServiceImpl implements AutenticacionService {

    private static final long GOOGLE_CODE_TTL_MILLIS = 60_000L;

    private final UsuarioFacade usuarioFacade;
    private final UsuarioRepository userRepository;
    private final UsuarioConverter usuarioConverter;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    /**
     * Almacén en memoria de códigos de un solo uso para el login con Google.
     * Proporcional al alcance del proyecto (una única instancia); no sobrevive a un reinicio.
     */
    private final Map<String, CodigoLoginGoogle> googleLoginCodes = new ConcurrentHashMap<>();

    private record CodigoLoginGoogle(String token, long expirationTime, long expiraEnEpochMillis) {
    }

    /**
     * Valida que el DTO de usuario no sea nulo
     */
    private void validarUsuarioDTO(UsuarioDTO usuarioDTO) {
        if (usuarioDTO == null) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("usuarioDTO"));
        }
    }

    /**
     * Valida que el DTO de login no sea nulo y contenga datos válidos
     */
    private void validarLoginDTO(LoginUsuarioDTO loginUsuarioDTO) {
        if (loginUsuarioDTO == null) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("loginUsuarioDTO"));
        }
        
        if (loginUsuarioDTO.getNif() == null || loginUsuarioDTO.getNif().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("NIF"));
        }
        
        if (loginUsuarioDTO.getPassword() == null || loginUsuarioDTO.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido("password"));
        }
    }

    /**
     * Crea el token de autenticación para el proceso de login
     */
    private UsernamePasswordAuthenticationToken crearTokenAutenticacion(LoginUsuarioDTO loginUsuarioDTO) {
        return new UsernamePasswordAuthenticationToken(
                loginUsuarioDTO.getNif(),
                loginUsuarioDTO.getPassword()
        );
    }

    /**
     * Extrae el usuario autenticado del principal, preservando las authorities
     * cargadas por el {@link UserDetailsService} (necesarias para el JWT).
     */
    private Usuario extraerUsuarioDelPrincipal(Object principal, String nif) {
        if (principal instanceof Usuario usuario) {
            return usuario;
        }

        // Fallback (no debería ocurrir normalmente): recargar vía UserDetailsService
        // para no perder las authorities, que findByNif no rellena.
        if (userDetailsService.loadUserByUsername(nif) instanceof Usuario usuario) {
            return usuario;
        }

        throw new IncorrectCredentials(ErrorMessages.ERROR_CREDENCIALES_INVALIDAS);
    }

    /**
     * Registra un nuevo usuario en el sistema
     * 
     * @param usuarioDTO Datos del usuario a registrar
     * @return Usuario registrado
     * @throws IllegalArgumentException si los datos son inválidos
     */
    @Override
    public Usuario registrar(UsuarioDTO usuarioDTO) {
        UsuarioDTO usuarioRegistrado = null;
        validarUsuarioDTO(usuarioDTO);
        usuarioRegistrado = usuarioFacade.altaUsuario(usuarioDTO);
        
        return usuarioConverter.toEntity(usuarioRegistrado);
    }

    /**
     * Autentica un usuario en el sistema
     * 
     * @param loginUsuarioDTO Credenciales de login
     * @return Usuario autenticado
     * @throws IncorrectCredentials si las credenciales son incorrectas
     * @throws IllegalArgumentException si los datos de entrada son inválidos
     */
    @Override
    public Usuario autenticar(LoginUsuarioDTO loginUsuarioDTO) {
        validarLoginDTO(loginUsuarioDTO);
        
        try {
            var token = crearTokenAutenticacion(loginUsuarioDTO);
            var authentication = authenticationManager.authenticate(token);

            return extraerUsuarioDelPrincipal(authentication.getPrincipal(), loginUsuarioDTO.getNif());

        } catch (Exception _) {
            throw new IncorrectCredentials(ErrorMessages.ERROR_CREDENCIALES_INVALIDAS);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI obtenerUriAutorizacionGoogle() {
        return URI.create(RestUrls.OAUTH2_AUTHORIZATION_GOOGLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Usuario autenticarConGoogle(String email, boolean emailVerificado) {
        if (email == null || email.isBlank()) {
            throw new GoogleAuthenticationException(
                    ErrorMessages.ERROR_GOOGLE_CUENTA_NO_ENCONTRADA, ErrorMessages.GOOGLE_ERROR_CODE_ACCOUNT_NOT_FOUND);
        }

        if (!emailVerificado) {
            throw new GoogleAuthenticationException(
                    ErrorMessages.ERROR_GOOGLE_EMAIL_NO_VERIFICADO, ErrorMessages.GOOGLE_ERROR_CODE_EMAIL_NOT_VERIFIED);
        }

        Usuario usuarioPorEmail = userRepository.findByEmail(email)
                .orElseThrow(() -> new GoogleAuthenticationException(
                        ErrorMessages.ERROR_GOOGLE_CUENTA_NO_ENCONTRADA, ErrorMessages.GOOGLE_ERROR_CODE_ACCOUNT_NOT_FOUND));

        // Se recarga a través del UserDetailsService para obtener las authorities (roles),
        // que findByEmail no rellena y que sí necesita el JWT para autorización.
        if (userDetailsService.loadUserByUsername(usuarioPorEmail.getNif()) instanceof Usuario usuarioConAuthorities) {
            return usuarioConAuthorities;
        }

        throw new GoogleAuthenticationException(
                ErrorMessages.ERROR_GOOGLE_AUTENTICACION_FALLIDA, ErrorMessages.GOOGLE_ERROR_CODE_AUTH_FAILED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generarCodigoLoginGoogle(String token, long expirationTime) {
        String code = UUID.randomUUID().toString();
        googleLoginCodes.put(code, new CodigoLoginGoogle(token, expirationTime, System.currentTimeMillis() + GOOGLE_CODE_TTL_MILLIS));
        return code;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LoginResponse canjearCodigoLoginGoogle(String code) {
        CodigoLoginGoogle entrada = code != null ? googleLoginCodes.remove(code) : null;

        if (entrada == null || entrada.expiraEnEpochMillis() < System.currentTimeMillis()) {
            throw new GoogleAuthenticationException(
                    ErrorMessages.ERROR_GOOGLE_CODIGO_INVALIDO, ErrorMessages.GOOGLE_ERROR_CODE_INVALID_CODE);
        }

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(entrada.token());
        loginResponse.setExpirationTime(entrada.expirationTime());
        return loginResponse;
    }
}
