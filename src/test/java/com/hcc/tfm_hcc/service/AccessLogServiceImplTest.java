package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.hcc.tfm_hcc.model.AccessLog;
import com.hcc.tfm_hcc.repository.AccessLogRepository;
import com.hcc.tfm_hcc.service.impl.AccessLogServiceImpl;

class AccessLogServiceImplTest {

    private AccessLogRepository accessLogRepository;
    private AccessLogServiceImpl service;

    @BeforeEach
    void setUp() {
        accessLogRepository = mock(AccessLogRepository.class);
        service = new AccessLogServiceImpl(accessLogRepository);
    }

    private AccessLog logValido() {
        AccessLog log = new AccessLog();
        log.setUsuarioId("usuario-1");
        log.setRuta("/api/historial");
        log.setMetodo("get");
        return log;
    }

    @Test
    void log_conDatosValidos_persisteElLogConValoresPorDefecto() {
        AccessLog log = logValido();

        service.log(log);

        ArgumentCaptor<AccessLog> captor = ArgumentCaptor.forClass(AccessLog.class);
        verify(accessLogRepository, times(1)).save(captor.capture());
        AccessLog guardado = captor.getValue();
        assertNotNull(guardado.getId());
        assertNotNull(guardado.getTimestamp());
        assertEquals(200, guardado.getEstado());
        assertEquals("GET", guardado.getMetodo());
    }

    @Test
    void log_conTimestampYEstadoYaEstablecidos_noLosSobrescribe() {
        AccessLog log = logValido();
        LocalDateTime timestampOriginal = LocalDateTime.now().minusHours(1);
        log.setTimestamp(timestampOriginal);
        log.setEstado(404);

        service.log(log);

        ArgumentCaptor<AccessLog> captor = ArgumentCaptor.forClass(AccessLog.class);
        verify(accessLogRepository, times(1)).save(captor.capture());
        assertEquals(timestampOriginal, captor.getValue().getTimestamp());
        assertEquals(404, captor.getValue().getEstado());
    }

    @Test
    void log_conRutaYIpConEspacios_lasNormaliza() {
        AccessLog log = logValido();
        log.setRuta("  /api/historial  ");
        log.setIp("  127.0.0.1  ");

        service.log(log);

        ArgumentCaptor<AccessLog> captor = ArgumentCaptor.forClass(AccessLog.class);
        verify(accessLogRepository, times(1)).save(captor.capture());
        assertEquals("/api/historial", captor.getValue().getRuta());
        assertEquals("127.0.0.1", captor.getValue().getIp());
    }

    @Test
    void log_conAccessLogNulo_noPersisteYNoLanzaExcepcion() {
        service.log(null);

        verify(accessLogRepository, never()).save(any());
    }

    @Test
    void log_sinUsuarioId_noPersiste() {
        AccessLog log = logValido();
        log.setUsuarioId(null);

        service.log(log);

        verify(accessLogRepository, never()).save(any());
    }

    @Test
    void log_sinRuta_noPersiste() {
        AccessLog log = logValido();
        log.setRuta("  ");

        service.log(log);

        verify(accessLogRepository, never()).save(any());
    }

    @Test
    void log_sinMetodo_noPersiste() {
        AccessLog log = logValido();
        log.setMetodo(null);

        service.log(log);

        verify(accessLogRepository, never()).save(any());
    }

    @Test
    void log_conErrorAlGuardar_lanzaRuntimeException() {
        AccessLog log = logValido();
        when(accessLogRepository.save(any())).thenThrow(new RuntimeException("fallo de BD"));

        assertThrows(RuntimeException.class, () -> service.log(log));
    }
}
