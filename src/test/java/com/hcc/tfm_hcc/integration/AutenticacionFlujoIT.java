package com.hcc.tfm_hcc.integration;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.RegistroUsuarioRequest;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Flujo de autenticación de punta a punta: registro → login → uso del token contra un
 * endpoint protegido. Verifica la integración real entre el endpoint de autenticación,
 * el {@code AuthenticationManager}, la emisión del JWT, el {@code JwtAuthenticationFilter}
 * y el {@code UserDetailsService} que carga los roles.
 */
class AutenticacionFlujoIT extends AbstractIntegrationIT {

    private static final String NIF = "12345678Z";
    private static final String EMAIL = "paciente.flujo@example.com";

    @Test
    void registroYLogin_devuelvenUnTokenQueAbreUnEndpointProtegido() throws Exception {
        registrarPaciente(NIF, EMAIL);

        String token = login(NIF);
        assertNotNull(token);

        mockMvc.perform(get("/usuario/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nif", notNullValue()));
    }

    @Test
    void endpointProtegido_sinToken_devuelve403() throws Exception {
        mockMvc.perform(get("/usuario/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpointProtegido_conTokenBasura_noExponeDatosDeUsuario() throws Exception {
        // Un token mal formado no debe autenticar: la petición no llega al controlador
        // y la respuesta no contiene datos del usuario.
        mockMvc.perform(get("/usuario/me").header("Authorization", "Bearer no-es-un-jwt"))
                .andExpect(jsonPath("$.nif").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void login_conContrasenaIncorrecta_devuelve401() throws Exception {
        registrarPaciente(NIF, EMAIL);

        LoginUsuarioDTO credenciales = new LoginUsuarioDTO();
        credenciales.setNif(NIF);
        credenciales.setPassword("contrasena-que-no-es");

        mockMvc.perform(post("/authentication/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credenciales)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_conNifInexistente_devuelve401() throws Exception {
        LoginUsuarioDTO credenciales = new LoginUsuarioDTO();
        credenciales.setNif("00000000T");
        credenciales.setPassword(PASSWORD);

        mockMvc.perform(post("/authentication/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credenciales)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signup_conNifYaRegistrado_noDevuelve2xx() throws Exception {
        registrarPaciente(NIF, EMAIL);

        RegistroUsuarioRequest repetido = new RegistroUsuarioRequest();
        repetido.setNombre("Otro");
        repetido.setApellido1("Distinto");
        repetido.setNif(NIF);
        repetido.setEmail("otro.email@example.com");
        repetido.setPassword(PASSWORD);
        repetido.setFechaNacimiento(LocalDateTime.of(1985, 5, 5, 0, 0));

        mockMvc.perform(post("/authentication/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repetido)))
                .andExpect(status().is5xxServerError());
    }
}
