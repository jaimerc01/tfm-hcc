package com.hcc.tfm_hcc.integration;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

import com.hcc.tfm_hcc.dto.AlergiaDTO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica el cifrado en reposo de los datos clínicos de punta a punta: una alergia
 * creada por el paciente se devuelve en claro a través de la API, pero la columna
 * subyacente en la base de datos relacional NO contiene el texto plano (lo cifra el
 * {@code AESEncryptionConverter} de forma transparente).
 *
 * <p>Los tests unitarios prueban el convertidor de forma aislada; este comprueba que
 * está realmente enganchado a la entidad {@code Alergia} y que la ida y vuelta
 * (escritura cifrada + lectura descifrada) funciona con Hibernate real.</p>
 */
class CifradoHistorialIT extends AbstractIntegrationIT {

    private static final String NIF = "12345678Z";
    private static final String EMAIL = "cifrado@example.com";
    private static final String DESCRIPCION_ALERGIA = "Alergia a la penicilina y derivados";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void alergia_seDevuelveEnClaroPorLaApiPeroSeGuardaCifradaEnBaseDeDatos() throws Exception {
        String token = registrarYObtenerToken(NIF, EMAIL);

        AlergiaDTO alergia = new AlergiaDTO();
        alergia.setDescripcion(DESCRIPCION_ALERGIA);

        // 1. El paciente registra la alergia.
        mockMvc.perform(post("/historia/alergias")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(alergia)))
                .andExpect(status().isOk());

        // 2. La API se la devuelve descifrada.
        mockMvc.perform(get("/historia").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alergias[0].descripcion").value(DESCRIPCION_ALERGIA));

        // 3. Pero en la columna de la BD no está el texto plano: está cifrado.
        List<String> valoresCrudos = jdbcTemplate.queryForList(
                "SELECT descripcion FROM alergia", String.class);
        assertThat(valoresCrudos).hasSize(1);
        assertThat(valoresCrudos.get(0))
                .as("La descripción de la alergia no debe almacenarse en claro")
                .isNotEqualTo(DESCRIPCION_ALERGIA)
                .doesNotContain("penicilina");
    }
}
