package com.hcc.tfm_hcc.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.dto.NotificacionDTO;
import com.hcc.tfm_hcc.model.Notificacion;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.service.impl.HmacSearchIndexServiceImpl;

class NotificacionConverterTest {

    private static final String CLAVE_PRUEBAS_BASE64 =
            Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    private final NotificacionConverter converter = new NotificacionConverter(
            new UsuarioConverter(new HmacSearchIndexServiceImpl(new EncryptionKeyProvider(CLAVE_PRUEBAS_BASE64))));

    @Test
    void toDto_conUsuarioAsociado_incluyeElUsuarioDto() {
        Notificacion notificacion = new Notificacion();
        UUID id = UUID.randomUUID();
        notificacion.setId(id);
        notificacion.setMensaje("Tienes una nueva solicitud pendiente");
        notificacion.setLeida(false);
        notificacion.setFechaCreacion(LocalDateTime.now());
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNif("12345678A");
        notificacion.setUsuario(usuario);

        NotificacionDTO dto = converter.toDto(notificacion);

        assertEquals(id.toString(), dto.getId());
        assertEquals("12345678A", dto.getUsuarioDTO().getNif());
        assertEquals("/mis-solicitudes", dto.getEnlace());
    }

    @Test
    void toDto_sinUsuarioAsociado_dejaElUsuarioDtoNulo() {
        Notificacion notificacion = new Notificacion();
        notificacion.setMensaje("Mensaje genérico");

        NotificacionDTO dto = converter.toDto(notificacion);

        assertNull(dto.getUsuarioDTO());
        assertNull(dto.getEnlace());
    }

    @Test
    void toEntity_conIdValido_loParsea() {
        UUID id = UUID.randomUUID();
        NotificacionDTO dto = new NotificacionDTO();
        dto.setId(id.toString());
        dto.setMensaje("mensaje");
        dto.setLeida(true);

        Notificacion notificacion = converter.toEntity(dto);

        assertEquals(id, notificacion.getId());
        assertTrue(notificacion.isLeida());
    }

    @Test
    void toEntity_conIdConFormatoInvalido_dejaElIdNulo() {
        NotificacionDTO dto = new NotificacionDTO();
        dto.setId("no-es-un-uuid");

        assertNull(converter.toEntity(dto).getId());
    }

    @Test
    void toEntity_conUsuarioDtoAsociado_convierteElUsuario() {
        NotificacionDTO dto = new NotificacionDTO();
        com.hcc.tfm_hcc.dto.UsuarioDTO usuarioDto = new com.hcc.tfm_hcc.dto.UsuarioDTO();
        usuarioDto.setNif("12345678A");
        dto.setUsuarioDTO(usuarioDto);

        Notificacion notificacion = converter.toEntity(dto);

        assertEquals("12345678A", notificacion.getUsuario().getNif());
    }

    @Test
    void toDtoList_convierteTodosLosElementos() {
        Notificacion n1 = new Notificacion();
        Notificacion n2 = new Notificacion();

        List<NotificacionDTO> resultado = converter.toDtoList(List.of(n1, n2));

        assertEquals(2, resultado.size());
    }

    @Test
    void toEntityList_convierteTodosLosElementos() {
        NotificacionDTO d1 = new NotificacionDTO();
        NotificacionDTO d2 = new NotificacionDTO();

        List<Notificacion> resultado = converter.toEntityList(List.of(d1, d2));

        assertEquals(2, resultado.size());
    }
}
