package com.hcc.tfm_hcc.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcc.tfm_hcc.dto.PropuestaCambioClinicoDTO;
import com.hcc.tfm_hcc.model.PropuestaCambioClinico;
import com.hcc.tfm_hcc.model.PropuestaCambioClinico.Dominio;
import com.hcc.tfm_hcc.model.PropuestaCambioClinico.Operacion;
import com.hcc.tfm_hcc.model.Usuario;

/**
 * Pruebas de {@link PropuestaCambioClinicoConverter}: además de comprobar el mapeo campo a
 * campo, verifican que el {@code payloadJson} se parsea al sub-DTO correcto según el dominio y
 * que un JSON ilegible no aborta la conversión.
 */
class PropuestaCambioClinicoConverterTest {

    private PropuestaCambioClinicoConverter converter;

    @BeforeEach
    void setUp() {
        converter = new PropuestaCambioClinicoConverter(new ObjectMapper());
    }

    private Usuario usuario(String nombre, String nif) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setApellido1("Apellido1");
        u.setApellido2("Apellido2");
        u.setNif(nif);
        u.setEmail("secreto@example.com");
        u.setPassword("$2a$10$hashsecreto");
        u.setTotpSecret("SECRETOTOTP");
        return u;
    }

    private PropuestaCambioClinico base() {
        PropuestaCambioClinico p = new PropuestaCambioClinico();
        p.setId(UUID.randomUUID());
        p.setEstado(PropuestaCambioClinico.ESTADO_PENDIENTE);
        p.setOperacion(Operacion.CREATE);
        p.setMotivo("Corrección de un dato erróneo");
        p.setFechaCreacion(LocalDateTime.now().minusHours(2));
        p.setMedico(usuario("Dra. Médica", "11111111H"));
        p.setPaciente(usuario("Paco Paciente", "22222222J"));
        return p;
    }

    @Test
    void toDto_conEntidadNula_devuelveNull() {
        assertNull(converter.toDto(null));
    }

    @Test
    void toDto_copiaLosCamposEscalaresYLosResumenesDePersona() {
        PropuestaCambioClinico p = base();
        p.setDominio(Dominio.ANALISIS_SANGRE);
        p.setOperacion(Operacion.DELETE);
        p.setIdRecursoObjetivo("recurso-123");
        p.setDescripcionActual("Glucosa 95 mg/dL");
        p.setFechaResolucion(LocalDateTime.now());

        PropuestaCambioClinicoDTO dto = converter.toDto(p);

        assertNotNull(dto);
        assertEquals(p.getId().toString(), dto.getId());
        assertEquals("ANALISIS_SANGRE", dto.getDominio());
        assertEquals("DELETE", dto.getOperacion());
        assertEquals(PropuestaCambioClinico.ESTADO_PENDIENTE, dto.getEstado());
        assertEquals("recurso-123", dto.getIdRecursoObjetivo());
        assertEquals("Corrección de un dato erróneo", dto.getMotivo());
        assertEquals("Glucosa 95 mg/dL", dto.getDescripcionActual());
        assertEquals(p.getFechaCreacion(), dto.getFechaCreacion());
        assertEquals(p.getFechaResolucion(), dto.getFechaResolucion());
        assertEquals("Dra. Médica", dto.getMedico().getNombre());
        assertEquals("11111111H", dto.getMedico().getNif());
        assertEquals("Paco Paciente", dto.getPaciente().getNombre());
        assertEquals("Apellido2", dto.getPaciente().getApellido2());

        // Una operación de borrado no lleva payload: los sub-DTOs quedan a nulo.
        assertNull(dto.getMedicion());
        assertNull(dto.getAlergia());
        assertNull(dto.getAntecedente());
    }

    @Test
    void toDto_noExponeDatosSensiblesDelUsuarioOriginal() {
        PropuestaCambioClinicoDTO dto = converter.toDto(base());

        String serializado = dto.toString();
        assertTrue(serializado.contains("Paco Paciente"));
        assertFalse(serializado.contains("$2a$10$hashsecreto"));
        assertFalse(serializado.contains("SECRETOTOTP"));
        assertFalse(serializado.contains("secreto@example.com"));
    }

    @Test
    void toDto_conDominioYOperacionNulos_dejaEsosCamposANulo() {
        PropuestaCambioClinico p = base();
        p.setDominio(null);
        p.setOperacion(null);

        PropuestaCambioClinicoDTO dto = converter.toDto(p);

        assertNull(dto.getDominio());
        assertNull(dto.getOperacion());
    }

    @Test
    void toDto_conPersonasNulas_noFalla() {
        PropuestaCambioClinico p = base();
        p.setMedico(null);
        p.setPaciente(null);

        PropuestaCambioClinicoDTO dto = converter.toDto(p);

        assertNull(dto.getMedico());
        assertNull(dto.getPaciente());
    }

    @Test
    void toDto_conDominioAntecedente_parseaElPayloadAlSubDtoDeAntecedente() {
        PropuestaCambioClinico p = base();
        p.setDominio(Dominio.ANTECEDENTE);
        p.setPayloadJson("{\"categoria\":\"CRONICO\",\"descripcion\":\"Hipertensión\"}");

        PropuestaCambioClinicoDTO dto = converter.toDto(p);

        assertNotNull(dto.getAntecedente());
        assertEquals("CRONICO", dto.getAntecedente().getCategoria());
        assertEquals("Hipertensión", dto.getAntecedente().getDescripcion());
        assertNull(dto.getAlergia());
        assertNull(dto.getMedicion());
    }

    @Test
    void toDto_conDominioAlergia_parseaElPayloadAlSubDtoDeAlergia() {
        PropuestaCambioClinico p = base();
        p.setDominio(Dominio.ALERGIA);
        p.setPayloadJson("{\"descripcion\":\"Penicilina\"}");

        PropuestaCambioClinicoDTO dto = converter.toDto(p);

        assertNotNull(dto.getAlergia());
        assertEquals("Penicilina", dto.getAlergia().getDescripcion());
    }

    @Test
    void toDto_conDominioDeMedicion_parseaElPayloadAlSubDtoDeMedicion() {
        for (Dominio dominio : List.of(Dominio.ANALISIS_SANGRE, Dominio.SIGNOS_VITALES, Dominio.ANALISIS_ORINA)) {
            PropuestaCambioClinico p = base();
            p.setDominio(dominio);
            p.setPayloadJson("{\"label\":\"Glucosa\",\"value\":\"110\",\"unit\":\"mg/dL\"}");

            PropuestaCambioClinicoDTO dto = converter.toDto(p);

            assertNotNull(dto.getMedicion(), "medición para dominio " + dominio);
            assertEquals("Glucosa", dto.getMedicion().getLabel());
            assertEquals("110", dto.getMedicion().getValue());
            assertEquals("mg/dL", dto.getMedicion().getUnit());
        }
    }

    @Test
    void toDto_conPayloadEnBlanco_noRellenaNingunSubDto() {
        PropuestaCambioClinico p = base();
        p.setDominio(Dominio.ALERGIA);
        p.setPayloadJson("   ");

        PropuestaCambioClinicoDTO dto = converter.toDto(p);

        assertNull(dto.getAlergia());
    }

    @Test
    void toDto_conPayloadIlegible_registraElErrorYDejaElSubDtoANuloSinAbortar() {
        PropuestaCambioClinico p = base();
        p.setDominio(Dominio.ALERGIA);
        p.setPayloadJson("{ esto no es json valido ");

        PropuestaCambioClinicoDTO dto = converter.toDto(p);

        assertNotNull(dto);
        assertEquals("Corrección de un dato erróneo", dto.getMotivo());
        assertNull(dto.getAlergia());
    }

    @Test
    void toDtoList_conListaNula_devuelveListaVacia() {
        assertTrue(converter.toDtoList(null).isEmpty());
    }

    @Test
    void toDtoList_convierteCadaElemento() {
        PropuestaCambioClinico p1 = base();
        p1.setDominio(Dominio.ALERGIA);
        PropuestaCambioClinico p2 = base();
        p2.setEstado(PropuestaCambioClinico.ESTADO_ACEPTADA);

        List<PropuestaCambioClinicoDTO> dtos = converter.toDtoList(List.of(p1, p2));

        assertEquals(2, dtos.size());
        assertEquals("ALERGIA", dtos.get(0).getDominio());
        assertEquals(PropuestaCambioClinico.ESTADO_ACEPTADA, dtos.get(1).getEstado());
    }
}
