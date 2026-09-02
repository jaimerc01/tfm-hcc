package com.hcc.tfm_hcc.integration;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Autorización por rol de punta a punta: las reglas {@code hasRole(...)} de
 * {@code WebSecurityConfig} se aplican sobre el JWT real emitido por el servidor.
 *
 * <p>Los tests unitarios de los controladores de {@code /medico} y {@code /admin} usan
 * {@code standaloneSetup}, que no monta la cadena de seguridad; aquí se comprueba que un
 * paciente no puede tocar esas rutas y que, tras recibir el rol MEDICO, sí puede.</p>
 */
class ControlAccesoRolIT extends AbstractIntegrationIT {

    private static final String NIF = "12345678Z";
    private static final String EMAIL = "acceso@example.com";

    @Test
    void paciente_noPuedeAccederAZonaMedicaNiAdmin() throws Exception {
        String token = registrarYObtenerToken(NIF, EMAIL);

        mockMvc.perform(get("/medico/pacientes").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/admin/medicos").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void alRecibirElRolMedico_elAccesoSeConcedeSinNecesidadDeReloguearse() throws Exception {
        registrarPaciente(NIF, EMAIL);
        String token = login(NIF);

        // Todavía es solo paciente: no entra en la zona médica.
        mockMvc.perform(get("/medico/solicitudes-asignacion/pendientes")
                .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());

        // Un administrador le asigna el perfil MEDICO.
        asignarRol(NIF, ROL_MEDICO);

        // El mismo token de antes ya sirve: JwtAuthenticationFilter recarga las
        // authorities desde la base de datos en cada petición (el claim del JWT es
        // solo informativo para el frontend), así que la autorización siempre refleja
        // el estado actual de los perfiles del usuario.
        mockMvc.perform(get("/medico/solicitudes-asignacion/pendientes")
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void alRevocarElRolMedico_elAccesoSeCortaInmediatamente() throws Exception {
        registrarPaciente(NIF, EMAIL);
        asignarRol(NIF, ROL_MEDICO);
        String token = login(NIF);

        mockMvc.perform(get("/medico/solicitudes-asignacion/pendientes")
                .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        revocarRol(NIF, ROL_MEDICO);

        mockMvc.perform(get("/medico/solicitudes-asignacion/pendientes")
                .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinAutenticacion_lasRutasPorRolDevuelven403() throws Exception {
        mockMvc.perform(get("/medico/pacientes")).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/medicos")).andExpect(status().isForbidden());
    }
}
