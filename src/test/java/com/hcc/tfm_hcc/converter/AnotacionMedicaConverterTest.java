package com.hcc.tfm_hcc.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.dto.AnotacionMedicaDTO;
import com.hcc.tfm_hcc.model.AnotacionMedica;
import com.hcc.tfm_hcc.model.Usuario;

class AnotacionMedicaConverterTest {

    private final AnotacionMedicaConverter converter = new AnotacionMedicaConverter();

    private Usuario medico(String nif, String nombre, String apellido1, String apellido2) {
        Usuario usuario = new Usuario();
        usuario.setNif(nif);
        usuario.setNombre(nombre);
        usuario.setApellido1(apellido1);
        usuario.setApellido2(apellido2);
        return usuario;
    }

    @Test
    void toDto_mapeaTodosLosCampos() {
        AnotacionMedica anotacion = new AnotacionMedica();
        anotacion.setId(UUID.randomUUID());
        anotacion.setMensaje("Revisar la tensión en la próxima visita");
        anotacion.setFechaCreacion(LocalDateTime.now());
        anotacion.setMedico(medico("11111111A", "Ana", "García", "López"));

        AnotacionMedicaDTO dto = converter.toDto(anotacion);

        assertEquals(anotacion.getId().toString(), dto.getId());
        assertEquals("Revisar la tensión en la próxima visita", dto.getMensaje());
        assertEquals("11111111A", dto.getMedicoNif());
        assertEquals("Ana García López", dto.getMedicoNombre());
        assertEquals(anotacion.getFechaCreacion().toString(), dto.getCreatedAt());
    }

    @Test
    void toDto_conApellidosNulos_componeSoloElNombreDisponible() {
        AnotacionMedica anotacion = new AnotacionMedica();
        anotacion.setMedico(medico("11111111A", "Ana", null, null));

        assertEquals("Ana", converter.toDto(anotacion).getMedicoNombre());
    }

    @Test
    void toDto_conEntradaNula_devuelveNull() {
        assertNull(converter.toDto(null));
    }

    @Test
    void toDto_sinMedico_dejaLosCamposDelMedicoNulos() {
        AnotacionMedica anotacion = new AnotacionMedica();
        anotacion.setMensaje("texto");

        AnotacionMedicaDTO dto = converter.toDto(anotacion);

        assertNull(dto.getMedicoNif());
        assertNull(dto.getMedicoNombre());
    }

    @Test
    void toDtoList_conEntradaNula_devuelveListaVacia() {
        assertTrue(converter.toDtoList(null).isEmpty());
    }

    @Test
    void toDtoList_mapeaCadaElemento() {
        AnotacionMedica a1 = new AnotacionMedica();
        a1.setMensaje("uno");
        AnotacionMedica a2 = new AnotacionMedica();
        a2.setMensaje("dos");

        List<AnotacionMedicaDTO> dtos = converter.toDtoList(List.of(a1, a2));

        assertEquals(2, dtos.size());
        assertEquals("uno", dtos.get(0).getMensaje());
        assertEquals("dos", dtos.get(1).getMensaje());
    }
}
