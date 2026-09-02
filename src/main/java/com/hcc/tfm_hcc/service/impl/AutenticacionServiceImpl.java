package com.hcc.tfm_hcc.service.impl;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.constants.RestUrls;
import com.hcc.tfm_hcc.converter.UsuarioConverter;
import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.AutenticacionOperacionException;
import com.hcc.tfm_hcc.exception.GoogleAuthenticationException;
import com.hcc.tfm_hcc.exception.IncorrectCredentials;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.util.LogMaskUtil;
import com.hcc.tfm_hcc.model.GoogleLoginCode;
import com.hcc.tfm_hcc.model.LoginResponse;
import com.hcc.tfm_hcc.model.TwoFactorChallenge;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.GoogleLoginCodeRepository;
import com.hcc.tfm_hcc.repository.TwoFactorChallengeRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.AutenticacionService;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;
import com.hcc.tfm_hcc.service.TotpService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de autenticación.
 * Proporciona funcionalidades para registro y autenticación de usuarios.
 *
 * @author Sistema HCC
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutenticacionServiceImpl implements AutenticacionService {

    private static final long GOOGLE_CODE_TTL_MILLIS = 60_000L;
    private static final long TWO_FACTOR_CHALLENGE_TTL_MILLIS = 5 * 60_000L;
    private static final int TWO_FACTOR_MAX_INTENTOS = 5;

    private final UsuarioFacade usuarioFacade;
    private final UsuarioRepository userRepository;
    private final UsuarioConverter usuarioConverter;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final HmacSearchIndexService hmacSearchIndexService;
    private final GoogleLoginCodeRepository googleLoginCodeRepository;
    private final TwoFactorChallengeRepository twoFactorChallengeRepository;
    private final TotpService totpService;
    private final MongoTemplate mongoTemplate;

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

        String nifLog = LogMaskUtil.enmascarar(loginUsuarioDTO.getNif());
        try {
            var token = crearTokenAutenticacion(loginUsuarioDTO);
            var authentication = authenticationManager.authenticate(token);

            return extraerUsuarioDelPrincipal(authentication.getPrincipal(), loginUsuarioDTO.getNif());

        } catch (AuthenticationException e) {
            // Credenciales bien formadas pero que no autentican (usuario inexistente,
            // contraseña incorrecta, cuenta deshabilitada...). No se detalla el motivo
            // al cliente para no filtrar el estado de la cuenta.
            log.warn("Autenticación fallida para el NIF {}: {}", nifLog, e.getClass().getSimpleName());
            throw new IncorrectCredentials(ErrorMessages.ERROR_CREDENCIALES_INVALIDAS);
        } catch (IncorrectCredentials e) {
            throw e;
        } catch (RuntimeException e) {
            // Fallo de infraestructura (BD caída, error de configuración...): no es un
            // problema de credenciales, así que no debe enmascararse como un 401.
            log.error("Error inesperado durante la autenticación del NIF {}: {}", nifLog, e.getMessage(), e);
            throw new AutenticacionOperacionException(ErrorMessages.ERROR_INTERNO_SERVIDOR, e);
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

        Usuario usuarioPorEmail = userRepository.findByEmailHash(hmacSearchIndexService.indexar(email))
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
        GoogleLoginCode codigo = new GoogleLoginCode();
        codigo.setId(UUID.randomUUID().toString());
        codigo.setToken(token);
        codigo.setExpirationTime(expirationTime);
        codigo.setFechaExpiracion(Instant.now().plusMillis(GOOGLE_CODE_TTL_MILLIS));
        googleLoginCodeRepository.save(codigo);
        return codigo.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generarCodigoLoginGoogleConDosFactores(String challengeId) {
        GoogleLoginCode codigo = new GoogleLoginCode();
        codigo.setId(UUID.randomUUID().toString());
        codigo.setRequiresTwoFactor(true);
        codigo.setChallengeId(challengeId);
        codigo.setFechaExpiracion(Instant.now().plusMillis(GOOGLE_CODE_TTL_MILLIS));
        googleLoginCodeRepository.save(codigo);
        return codigo.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LoginResponse canjearCodigoLoginGoogle(String code) {
        GoogleLoginCode entrada = code != null
                ? mongoTemplate.findAndRemove(Query.query(Criteria.where("_id").is(code)), GoogleLoginCode.class)
                : null;

        if (entrada == null || entrada.getFechaExpiracion().isBefore(Instant.now())) {
            throw new GoogleAuthenticationException(
                    ErrorMessages.ERROR_GOOGLE_CODIGO_INVALIDO, ErrorMessages.GOOGLE_ERROR_CODE_INVALID_CODE);
        }

        LoginResponse loginResponse = new LoginResponse();
        if (entrada.isRequiresTwoFactor()) {
            loginResponse.setRequiresTwoFactor(true);
            loginResponse.setChallengeId(entrada.getChallengeId());
            return loginResponse;
        }

        loginResponse.setToken(entrada.getToken());
        loginResponse.setExpirationTime(entrada.getExpirationTime());
        return loginResponse;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String crearChallengeDosFactores(Usuario usuario) {
        TwoFactorChallenge challenge = new TwoFactorChallenge();
        challenge.setId(UUID.randomUUID().toString());
        challenge.setUsuarioId(usuario.getId().toString());
        challenge.setIntentos(0);
        challenge.setFechaExpiracion(Instant.now().plusMillis(TWO_FACTOR_CHALLENGE_TTL_MILLIS));
        twoFactorChallengeRepository.save(challenge);
        return challenge.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Usuario verificarCodigoDosFactores(String challengeId, String code) {
        if (challengeId == null || challengeId.isBlank()) {
            throw new IncorrectCredentials(ErrorMessages.ERROR_TOTP_CHALLENGE_INVALIDO);
        }

        TwoFactorChallenge challenge = twoFactorChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new IncorrectCredentials(ErrorMessages.ERROR_TOTP_CHALLENGE_INVALIDO));

        if (challenge.getFechaExpiracion().isBefore(Instant.now())) {
            twoFactorChallengeRepository.deleteById(challengeId);
            throw new IncorrectCredentials(ErrorMessages.ERROR_TOTP_CHALLENGE_INVALIDO);
        }

        Usuario usuario = userRepository.findById(UUID.fromString(challenge.getUsuarioId()))
                .orElseThrow(() -> new IncorrectCredentials(ErrorMessages.ERROR_TOTP_CHALLENGE_INVALIDO));

        if (!totpService.validarCodigo(usuario.getTotpSecret(), code)) {
            registrarIntentoFallido(challenge);
            throw new IncorrectCredentials(ErrorMessages.ERROR_TOTP_CODIGO_INVALIDO);
        }

        twoFactorChallengeRepository.deleteById(challengeId);

        // Se recarga a través del UserDetailsService para obtener las authorities (roles),
        // igual que en el resto de flujos de login de esta clase.
        if (userDetailsService.loadUserByUsername(usuario.getNif()) instanceof Usuario usuarioConAuthorities) {
            return usuarioConAuthorities;
        }

        throw new IncorrectCredentials(ErrorMessages.ERROR_CREDENCIALES_INVALIDAS);
    }

    /**
     * Incrementa el contador de intentos fallidos de un reto de segundo factor y lo
     * invalida si se ha alcanzado el máximo permitido, para dificultar la fuerza
     * bruta sobre el código de 6 dígitos.
     */
    private void registrarIntentoFallido(TwoFactorChallenge challenge) {
        challenge.setIntentos(challenge.getIntentos() + 1);
        if (challenge.getIntentos() >= TWO_FACTOR_MAX_INTENTOS) {
            twoFactorChallengeRepository.deleteById(challenge.getId());
        } else {
            twoFactorChallengeRepository.save(challenge);
        }
    }
}
