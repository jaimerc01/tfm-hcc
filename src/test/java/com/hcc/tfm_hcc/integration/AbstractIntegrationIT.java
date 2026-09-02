package com.hcc.tfm_hcc.integration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.RegistroUsuarioRequest;
import com.hcc.tfm_hcc.model.LoginResponse;
import com.hcc.tfm_hcc.model.Perfil;
import com.hcc.tfm_hcc.repository.AccessLogRepository;
import com.hcc.tfm_hcc.repository.PerfilRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;
import com.hcc.tfm_hcc.service.PerfilUsuarioService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Clase base para los tests de integración (@SpringBootTest con contexto completo,
 * H2 en memoria + MongoDB embebido, y toda la cadena de filtros de Spring Security).
 *
 * <p>A diferencia de los tests unitarios de controlador (que usan {@code standaloneSetup}
 * y mockean el facade), aquí una petición atraviesa de verdad: el {@code JwtAuthenticationFilter},
 * las reglas por rol de {@code WebSecurityConfig}, el {@code AccessLogFilter}, el
 * {@code @RestControllerAdvice}, la serialización Jackson, los convertidores AES y los
 * repositorios reales.</p>
 *
 * <p>Todas las subclases comparten exactamente esta configuración para que Spring reutilice
 * un único contexto de aplicación entre clases de test.</p>
 *
 * <p>El test es {@code @Transactional}: cada método se ejecuta en una transacción que se
 * revierte al terminar, así que la base de datos relacional queda limpia entre tests. Los
 * documentos de MongoDB (logs de acceso) no participan en esa transacción y se limpian
 * explícitamente en {@link #prepararEntorno()}.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
abstract class AbstractIntegrationIT {

    protected static final String PASSWORD = "Secreto123";
    protected static final String ROL_PACIENTE = "PACIENTE";
    protected static final String ROL_MEDICO = "MEDICO";
    protected static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HmacSearchIndexService hmacSearchIndexService;

    @Autowired
    private PerfilUsuarioService perfilUsuarioService;

    @Autowired
    protected AccessLogRepository accessLogRepository;

    @BeforeEach
    void prepararEntorno() {
        for (String rol : List.of(ROL_PACIENTE, ROL_MEDICO, ROL_ADMINISTRADOR)) {
            if (perfilRepository.getPerfilByRol(rol).isEmpty()) {
                Perfil perfil = new Perfil();
                perfil.setRol(rol);
                perfil.setFechaCreacion(LocalDateTime.now());
                perfilRepository.save(perfil);
            }
        }
        accessLogRepository.deleteAll();
    }

    /**
     * Da de alta un usuario a través del endpoint público real de registro.
     * El usuario queda con el perfil PACIENTE que asigna el propio flujo de alta.
     */
    protected void registrarPaciente(String nif, String email) throws Exception {
        RegistroUsuarioRequest request = new RegistroUsuarioRequest();
        request.setNombre("Nombre");
        request.setApellido1("Apellido");
        request.setApellido2("Segundo");
        request.setNif(nif);
        request.setEmail(email);
        request.setPassword(PASSWORD);
        request.setTelefono("600123123");
        request.setFechaNacimiento(LocalDateTime.of(1990, 1, 1, 0, 0));

        mockMvc.perform(post("/authentication/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /**
     * Ejecuta el login real y devuelve el JWT emitido por el servidor.
     */
    protected String login(String nif) throws Exception {
        LoginUsuarioDTO credenciales = new LoginUsuarioDTO();
        credenciales.setNif(nif);
        credenciales.setPassword(PASSWORD);

        MvcResult resultado = mockMvc.perform(post("/authentication/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credenciales)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse respuesta = objectMapper.readValue(
                resultado.getResponse().getContentAsString(), LoginResponse.class);
        return respuesta.getToken();
    }

    /** Atajo: registra el paciente y devuelve directamente su token. */
    protected String registrarYObtenerToken(String nif, String email) throws Exception {
        registrarPaciente(nif, email);
        return login(nif);
    }

    /**
     * Eleva a un usuario ya registrado añadiéndole un rol adicional (MEDICO / ADMINISTRADOR),
     * simulando lo que en producción hace un administrador. Tras esto hay que volver a hacer
     * login para que el nuevo rol viaje en el JWT.
     */
    protected void asignarRol(String nif, String rol) {
        perfilUsuarioService.asignarPerfil(idUsuario(nif), rol);
    }

    protected void revocarRol(String nif, String rol) {
        perfilUsuarioService.revocarPerfil(idUsuario(nif), rol);
    }

    private UUID idUsuario(String nif) {
        return usuarioRepository.findByNifHash(hmacSearchIndexService.indexar(nif))
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado para el NIF de test"))
                .getId();
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
