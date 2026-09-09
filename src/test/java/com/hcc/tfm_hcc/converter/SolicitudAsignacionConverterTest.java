package com.hcc.tfm_hcc.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.dto.SolicitudAsignacionDTO;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.model.Usuario;

class SolicitudAsignacionConverterTest {

    private final SolicitudAsignacionConverter converter = new SolicitudAsignacionConverter();

    private Usuario usuario(String nombre, String nif) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setApellido1("Apellido");
        u.setNif(nif);
        u.setEmail("secreto@example.com");
        u.setPassword("$2a$10$hashsecreto");
        u.setTotpSecret("SECRETOTOTP");
        return u;
    }

    @Test
    void toDto_copiaSoloLosCamposMinimosYNoLosDatosSensibles() {
        SolicitudAsignacion solicitud = new SolicitudAsignacion();
        solicitud.setId(UUID.randomUUID());
        solicitud.setEstado("PENDIENTE");
        solicitud.setFechaCreacion(LocalDateTime.now());
        solicitud.setMedico(usuario("Dra. Médica", "11111111H"));
        solicitud.setPaciente(usuario("Paco Paciente", "22222222J"));

        SolicitudAsignacionDTO dto = converter.toDto(solicitud);

        assertNotNull(dto);
        assertEquals(solicitud.getId().toString(), dto.getId());
        assertEquals("PENDIENTE", dto.getEstado());
        assertEquals(solicitud.getFechaCreacion(), dto.getFechaCreacion());
        assertEquals("Dra. Médica", dto.getMedico().getNombre());
        assertEquals("11111111H", dto.getMedico().getNif());
        assertEquals("Paco Paciente", dto.getPaciente().getNombre());
        assertEquals("22222222J", dto.getPaciente().getNif());

        // El DTO no debe exponer ningún dato sensible del Usuario original.
        String serializado = dto.toString();
        assertTrue(serializado.contains("Paco Paciente"));
        assertTrue(!serializado.contains("$2a$10$hashsecreto"));
        assertTrue(!serializado.contains("SECRETOTOTP"));
        assertTrue(!serializado.contains("secreto@example.com"));
    }

    @Test
    void toDto_conEntidadNula_devuelveNull() {
        assertNull(converter.toDto(null));
    }

    @Test
    void toDto_conPersonasNulas_noFalla() {
        SolicitudAsignacion solicitud = new SolicitudAsignacion();
        solicitud.setEstado("ACEPTADA");

        SolicitudAsignacionDTO dto = converter.toDto(solicitud);

        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getMedico());
        assertNull(dto.getPaciente());
    }

    @Test
    void toDtoList_conListaNula_devuelveListaVacia() {
        assertTrue(converter.toDtoList(null).isEmpty());
    }

    @Test
    void toDtoList_convierteCadaElemento() {
        SolicitudAsignacion s1 = new SolicitudAsignacion();
        s1.setEstado("PENDIENTE");
        SolicitudAsignacion s2 = new SolicitudAsignacion();
        s2.setEstado("RECHAZADA");

        List<SolicitudAsignacionDTO> dtos = converter.toDtoList(List.of(s1, s2));

        assertEquals(2, dtos.size());
        assertEquals("PENDIENTE", dtos.get(0).getEstado());
        assertEquals("RECHAZADA", dtos.get(1).getEstado());
    }
}
