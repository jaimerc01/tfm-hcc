package com.hcc.tfm_hcc.integration;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.model.AccessLog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Trazabilidad de accesos de punta a punta: cada petición autenticada que atraviesa la
 * cadena de filtros deja un registro en la colección de logs de MongoDB
 * ({@code AccessLogFilter} → {@code AccessLogService} → repositorio Mongo real embebido).
 *
 * <p>Es un requisito de auditoría del historial clínico: quién accedió a qué y cuándo.</p>
 */
class TrazabilidadAccesoIT extends AbstractIntegrationIT {

    private static final String NIF = "12345678Z";
    private static final String EMAIL = "traza@example.com";

    @Test
    void unaPeticionAutenticada_dejaUnLogDeAccesoConRutaMetodoYUsuario() throws Exception {
        String token = registrarYObtenerToken(NIF, EMAIL);
        accessLogRepository.deleteAll();

        mockMvc.perform(get("/historia").header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        List<AccessLog> logs = accessLogRepository.findAll();
        assertThat(logs).hasSize(1);

        AccessLog log = logs.get(0);
        assertThat(log.getRuta()).isEqualTo("/historia");
        assertThat(log.getMetodo()).isEqualTo("GET");
        assertThat(log.getEstado()).isEqualTo(200);
        assertThat(log.getUsuarioId()).isNotBlank();
        assertThat(log.getTimestamp()).isNotNull();
        assertThat(log.getDuracionMs()).isNotNull();
    }

    @Test
    void cadaPeticionDejaSuPropioLog_yQuedanAsociadosAlMismoUsuario() throws Exception {
        String token = registrarYObtenerToken(NIF, EMAIL);
        accessLogRepository.deleteAll();

        mockMvc.perform(get("/historia").header("Authorization", bearer(token))).andExpect(status().isOk());
        mockMvc.perform(get("/usuario/me").header("Authorization", bearer(token))).andExpect(status().isOk());

        List<AccessLog> logs = accessLogRepository.findAll();
        assertThat(logs).hasSize(2);
        assertThat(logs).extracting(AccessLog::getRuta)
                .containsExactlyInAnyOrder("/historia", "/usuario/me");
        assertThat(logs).allSatisfy(l -> {
            assertThat(l.getMetodo()).isEqualTo("GET");
            assertThat(l.getUsuarioId()).isNotBlank();
        });
        assertThat(logs).extracting(AccessLog::getUsuarioId).containsOnly(logs.get(0).getUsuarioId());
    }
}
