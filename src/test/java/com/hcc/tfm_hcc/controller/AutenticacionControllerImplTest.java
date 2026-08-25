package com.hcc.tfm_hcc.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcc.tfm_hcc.controller.impl.AutenticacionControllerImpl;
import com.hcc.tfm_hcc.dto.GoogleCodeRequestDTO;
import com.hcc.tfm_hcc.dto.LoginUsuarioDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.exception.GoogleAuthenticationException;
import com.hcc.tfm_hcc.facade.AutenticacionFacade;
import com.hcc.tfm_hcc.model.LoginResponse;

class AutenticacionControllerImplTest {

    private MockMvc mvc;
    private AutenticacionFacade autenticacionFacade;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        autenticacionFacade = mock(AutenticacionFacade.class);
        AutenticacionControllerImpl controller = new AutenticacionControllerImpl(autenticacionFacade);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private LoginUsuarioDTO login(String nif, String password) {
        LoginUsuarioDTO dto = new LoginUsuarioDTO();
        dto.setNif(nif);
        dto.setPassword(password);
        return dto;
    }

    @Test
    void autenticar_conCredencialesValidas_devuelveElToken() throws Exception {
        LoginResponse response = new LoginResponse();
        response.setToken("jwt-token");
        when(autenticacionFacade.autenticar(org.mockito.ArgumentMatchers.any())).thenReturn(ResponseEntity.ok(response));

        mvc.perform(post("/authentication/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login("12345678A", "password123"))))
                .andExpect(status().isOk());
    }

    @Test
    void autenticar_conNifVacio_devuelveBadRequest() throws Exception {
        mvc.perform(post("/authentication/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login("  ", "password123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void autenticar_conErrorInesperado_devuelve500() throws Exception {
        when(autenticacionFacade.autenticar(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("fallo"));

        mvc.perform(post("/authentication/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login("12345678A", "password123"))))
                .andExpect(status().isInternalServerError());
    }

    private UsuarioDTO registroValido() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNif("12345678A");
        dto.setEmail("ana@example.com");
        dto.setPassword("password123");
        dto.setFechaNacimiento(LocalDateTime.of(1990, 5, 20, 0, 0));
        return dto;
    }

    @Test
    void registrar_conDatosValidos_devuelveElUsuarioRegistrado() throws Exception {
        when(autenticacionFacade.registrar(org.mockito.ArgumentMatchers.any())).thenReturn(ResponseEntity.ok(new UsuarioDTO()));

        mvc.perform(post("/authentication/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroValido())))
                .andExpect(status().isOk());
    }

    @Test
    void registrar_sinFechaNacimiento_devuelveBadRequest() throws Exception {
        UsuarioDTO dto = registroValido();
        dto.setFechaNacimiento(null);

        mvc.perform(post("/authentication/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrar_conIdYEstadoCuentaEnElJson_seIgnoranYNoLleganAlFacade() throws Exception {
        // Regresión: el cuerpo de /authentication/signup solo debe poder fijar los
        // campos de RegistroUsuarioRequest. Si un cliente no autenticado incluyera un
        // "id" en el JSON (con o sin intención de sobrescribir una cuenta ajena), no
        // debe llegar al UsuarioDTO que se pasa al resto de capas.
        org.mockito.ArgumentCaptor<UsuarioDTO> captor = org.mockito.ArgumentCaptor.forClass(UsuarioDTO.class);
        when(autenticacionFacade.registrar(captor.capture())).thenReturn(ResponseEntity.ok(new UsuarioDTO()));

        String jsonConCamposNoPermitidos = """
                {
                  "id": "11111111-1111-1111-1111-111111111111",
                  "estadoCuenta": "ADMINISTRADOR",
                  "especialidad": "Cardiología",
                  "nif": "12345678A",
                  "email": "ana@example.com",
                  "password": "password123",
                  "fechaNacimiento": "1990-05-20T00:00:00"
                }
                """;

        mvc.perform(post("/authentication/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonConCamposNoPermitidos))
                .andExpect(status().isOk());

        UsuarioDTO recibidoPorElFacade = captor.getValue();
        org.junit.jupiter.api.Assertions.assertNull(recibidoPorElFacade.getId());
        org.junit.jupiter.api.Assertions.assertNull(recibidoPorElFacade.getEstadoCuenta());
        org.junit.jupiter.api.Assertions.assertNull(recibidoPorElFacade.getEspecialidad());
        org.junit.jupiter.api.Assertions.assertEquals("12345678A", recibidoPorElFacade.getNif());
    }

    @Test
    void registrar_conErrorInesperado_devuelve500() throws Exception {
        when(autenticacionFacade.registrar(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("fallo"));

        mvc.perform(post("/authentication/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroValido())))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void iniciarLoginGoogle_devuelveLaRedireccionDelFacade() throws Exception {
        when(autenticacionFacade.iniciarLoginGoogle())
                .thenReturn(ResponseEntity.status(HttpStatus.FOUND).build());

        mvc.perform(get("/authentication/google/login")).andExpect(status().isFound());
    }

    @Test
    void intercambiarCodigoGoogle_conCodigoValido_devuelveElToken() throws Exception {
        GoogleCodeRequestDTO body = new GoogleCodeRequestDTO();
        body.setCode("codigo-un-uso");
        when(autenticacionFacade.canjearCodigoGoogle("codigo-un-uso"))
                .thenReturn(ResponseEntity.ok(new LoginResponse()));

        mvc.perform(post("/authentication/google/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void intercambiarCodigoGoogle_sinCodigo_devuelveBadRequest() throws Exception {
        GoogleCodeRequestDTO body = new GoogleCodeRequestDTO();

        mvc.perform(post("/authentication/google/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void intercambiarCodigoGoogle_conCodigoInvalido_devuelve401() throws Exception {
        GoogleCodeRequestDTO body = new GoogleCodeRequestDTO();
        body.setCode("codigo-invalido");
        when(autenticacionFacade.canjearCodigoGoogle("codigo-invalido"))
                .thenThrow(new GoogleAuthenticationException("codigo invalido", "google_code_invalid"));

        mvc.perform(post("/authentication/google/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }
}
