package com.hcc.tfm_hcc.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Flujo de punta a punta de las propuestas de cambio clínico: un médico con relación
 * asistencial activa propone un alta de dato clínico sobre el historial de un paciente, el
 * paciente la confirma y el dato aparece en su historial. Se comprueba además el control de
 * acceso: un paciente no puede resolver la propuesta de otro y un médico sin relación activa
 * no puede proponer nada.
 *
 * <p>La auditoría del cambio ({@code AuditoriaCambio} en MongoDB) se escribe en el
 * {@code afterCommit} de la transacción y por tanto no es observable dentro de un test
 * {@code @Transactional}; su registro con el {@code idMedico} y el motivo se comprueba en
 * {@code PropuestaCambioClinicoServiceImplTest}.</p>
 */
class PropuestaCambioClinicoIT extends AbstractIntegrationIT {

    private static final String NIF_MEDICO = "11111111H";
    private static final String NIF_PACIENTE = "22222222J";
    private static final String NIF_OTRO_PACIENTE = "33333333P";
    private static final String MOTIVO = "Corrección del valor tras la analítica de consulta";

    private String tokenMedico;
    private String tokenPaciente;

    private void prepararRelacionActiva() throws Exception {
        registrarPaciente(NIF_MEDICO, "medico.prop@example.com");
        registrarPaciente(NIF_PACIENTE, "paciente.prop@example.com");
        asignarRol(NIF_MEDICO, ROL_MEDICO);
        tokenMedico = login(NIF_MEDICO);
        tokenPaciente = login(NIF_PACIENTE);

        String respuesta = mockMvc.perform(post("/medico/solicitudes-asignacion")
                        .header("Authorization", bearer(tokenMedico))
                        .param("nifPaciente", NIF_PACIENTE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String idSolicitud = objectMapper.readTree(respuesta).get("id").asText();

        mockMvc.perform(put("/usuario/solicitudes/" + idSolicitud)
                        .header("Authorization", bearer(tokenPaciente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"ACEPTADA\"}"))
                .andExpect(status().isOk());
    }

    private String cuerpoAltaGlucosa() {
        return "{\"dominio\":\"ANALISIS_SANGRE\",\"operacion\":\"CREATE\",\"motivo\":\"" + MOTIVO + "\","
                + "\"medicion\":{\"label\":\"Glucosa\",\"value\":\"95\",\"unit\":\"mg/dL\"}}";
    }

    @Test
    void medicoPropone_pacienteConfirma_yElDatoApareceEnElHistorial() throws Exception {
        prepararRelacionActiva();

        String propuestaResp = mockMvc.perform(post("/medico/pacientes/" + NIF_PACIENTE + "/propuestas-cambio")
                        .header("Authorization", bearer(tokenMedico))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoAltaGlucosa()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.motivo").value(MOTIVO))
                .andReturn().getResponse().getContentAsString();
        String idPropuesta = objectMapper.readTree(propuestaResp).get("id").asText();

        // El paciente ve la propuesta pendiente.
        String pendientes = mockMvc.perform(get("/historia/propuestas-cambio")
                        .header("Authorization", bearer(tokenPaciente)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode lista = objectMapper.readTree(pendientes);
        org.assertj.core.api.Assertions.assertThat(lista).hasSize(1);
        org.assertj.core.api.Assertions.assertThat(lista.get(0).get("id").asText()).isEqualTo(idPropuesta);
        org.assertj.core.api.Assertions.assertThat(lista.get(0).get("estado").asText()).isEqualTo("PENDIENTE");

        // El paciente la acepta.
        mockMvc.perform(post("/historia/propuestas-cambio/" + idPropuesta)
                        .header("Authorization", bearer(tokenPaciente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aceptar\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACEPTADA"));

        // El dato ya está en el historial del paciente.
        mockMvc.perform(get("/historia").header("Authorization", bearer(tokenPaciente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisisSangre[0].tipo").value("Glucosa"))
                .andExpect(jsonPath("$.analisisSangre[0].valor").value("95"));

        // La propuesta sigue en el listado, ahora como parte del histórico resuelto.
        mockMvc.perform(get("/historia/propuestas-cambio").header("Authorization", bearer(tokenPaciente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estado").value("ACEPTADA"))
                .andExpect(jsonPath("$[0].fechaResolucion").isNotEmpty());
    }

    @Test
    void pacienteRechaza_yElDatoNoSeAplica() throws Exception {
        prepararRelacionActiva();

        String propuestaResp = mockMvc.perform(post("/medico/pacientes/" + NIF_PACIENTE + "/propuestas-cambio")
                        .header("Authorization", bearer(tokenMedico))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoAltaGlucosa()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String idPropuesta = objectMapper.readTree(propuestaResp).get("id").asText();

        mockMvc.perform(post("/historia/propuestas-cambio/" + idPropuesta)
                        .header("Authorization", bearer(tokenPaciente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aceptar\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"));

        mockMvc.perform(get("/historia").header("Authorization", bearer(tokenPaciente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisisSangre.length()").value(0));
    }

    @Test
    void otroPacienteNoPuedeResolverLaPropuesta() throws Exception {
        prepararRelacionActiva();
        String tokenOtro = registrarYObtenerToken(NIF_OTRO_PACIENTE, "otro.prop@example.com");

        String propuestaResp = mockMvc.perform(post("/medico/pacientes/" + NIF_PACIENTE + "/propuestas-cambio")
                        .header("Authorization", bearer(tokenMedico))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoAltaGlucosa()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String idPropuesta = objectMapper.readTree(propuestaResp).get("id").asText();

        mockMvc.perform(post("/historia/propuestas-cambio/" + idPropuesta)
                        .header("Authorization", bearer(tokenOtro))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aceptar\":true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void medicoSinRelacionActiva_noPuedeProponer() throws Exception {
        registrarPaciente(NIF_MEDICO, "medico.norel@example.com");
        registrarPaciente(NIF_PACIENTE, "paciente.norel@example.com");
        asignarRol(NIF_MEDICO, ROL_MEDICO);
        tokenMedico = login(NIF_MEDICO);

        mockMvc.perform(post("/medico/pacientes/" + NIF_PACIENTE + "/propuestas-cambio")
                        .header("Authorization", bearer(tokenMedico))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoAltaGlucosa()))
                .andExpect(status().isForbidden());
    }

    @Test
    void proponerSinMotivo_devuelveBadRequest() throws Exception {
        prepararRelacionActiva();

        mockMvc.perform(post("/medico/pacientes/" + NIF_PACIENTE + "/propuestas-cambio")
                        .header("Authorization", bearer(tokenMedico))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dominio\":\"ANALISIS_SANGRE\",\"operacion\":\"CREATE\",\"motivo\":\"  \","
                                + "\"medicion\":{\"label\":\"Glucosa\",\"value\":\"95\"}}"))
                .andExpect(status().isBadRequest());
    }
}
