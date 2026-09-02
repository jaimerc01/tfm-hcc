package com.hcc.tfm_hcc.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.hcc.tfm_hcc.constants.ErrorMessages;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El {@code @RestControllerAdvice} global se aplica de verdad dentro de la cadena de la
 * petición. Los tests unitarios de controlador usan {@code standaloneSetup} sin registrar
 * el advice (lo documentan explícitamente), así que sin un test de integración no hay
 * ninguna garantía de que un JSON mal formado devuelva 400 en lugar del 403 engañoso que
 * describe el Javadoc de {@code GlobalExceptionHandler}.
 */
class ManejoErroresIT extends AbstractIntegrationIT {

    @Test
    void jsonMalFormadoEnUnaRutaPublica_devuelve400ConMensajeDeError() throws Exception {
        mockMvc.perform(post("/authentication/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ esto no es json valido "))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(ErrorMessages.ERROR_JSON_MAL_FORMADO));
    }

    @Test
    void jsonMalFormadoEnUnaRutaProtegida_devuelve400_noUn403Enganoso() throws Exception {
        String token = registrarYObtenerToken("12345678Z", "errores@example.com");

        mockMvc.perform(post("/historia/alergias")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ descripcion: "))
                .andExpect(status().isBadRequest());
    }
}
