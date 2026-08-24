package com.hcc.tfm_hcc.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.dto.UserExportDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.model.AccessLog;
import com.hcc.tfm_hcc.model.Usuario;

class UsuarioExportConverterTest {

    private final UsuarioExportConverter converter = new UsuarioExportConverter();

    @Test
    void toAccesoDto_mapeaTodosLosCampos() {
        AccessLog log = new AccessLog();
        log.setTimestamp(LocalDateTime.now());
        log.setMetodo("GET");
        log.setRuta("/api/historial");
        log.setEstado(200);
        log.setDuracionMs(15L);
        log.setIp("127.0.0.1");
        log.setUserAgent("JUnit");

        UserExportDTO.AccesoDTO dto = converter.toAccesoDto(log);

        assertEquals("GET", dto.getMetodo());
        assertEquals("/api/historial", dto.getRuta());
        assertEquals(200, dto.getEstado());
        assertEquals(15L, dto.getDuracionMs());
        assertEquals("127.0.0.1", dto.getIp());
        assertEquals("JUnit", dto.getUserAgent());
    }

    @Test
    void toExportDto_conUsuarioPresente_incluyeEstadoYFechaEliminacion() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId("id-1");
        dto.setNombre("Ana");
        dto.setEmail("ana@example.com");
        Usuario usuario = new Usuario();
        usuario.setEstadoCuenta("ACTIVO");
        LocalDateTime fechaEliminacion = LocalDateTime.now();
        usuario.setFechaEliminacion(fechaEliminacion);

        UserExportDTO export = converter.toExportDto(dto, usuario, List.of(new AccessLog()));

        assertEquals("Ana", export.getNombre());
        assertEquals("ACTIVO", export.getEstadoCuenta());
        assertEquals(fechaEliminacion, export.getFechaEliminacion());
        assertEquals(1, export.getAccesos().size());
    }

    @Test
    void toExportDto_conUsuarioNulo_dejaEstadoYFechaEliminacionNulos() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId("id-1");

        UserExportDTO export = converter.toExportDto(dto, null, List.of());

        assertNull(export.getEstadoCuenta());
        assertNull(export.getFechaEliminacion());
        assertEquals(0, export.getAccesos().size());
    }

    @Test
    void toExportDto_conMasDe500Logs_limitaLosAccesosA500() {
        List<AccessLog> logs = new java.util.ArrayList<>();
        for (int i = 0; i < 600; i++) {
            logs.add(new AccessLog());
        }
        UsuarioDTO dto = new UsuarioDTO();

        UserExportDTO export = converter.toExportDto(dto, null, logs);

        assertEquals(500, export.getAccesos().size());
    }
}
