package com.hcc.tfm_hcc.facade;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.dto.RestablecerPasswordDTO;
import com.hcc.tfm_hcc.dto.SolicitudRestablecerPasswordDTO;
import com.hcc.tfm_hcc.exception.PasswordResetTokenInvalidoException;
import com.hcc.tfm_hcc.facade.impl.PasswordResetFacadeImpl;
import com.hcc.tfm_hcc.service.PasswordResetService;

class PasswordResetFacadeImplTest {

    private PasswordResetService service;
    private PasswordResetFacadeImpl facade;

    @BeforeEach
    void setUp() {
        service = mock(PasswordResetService.class);
        facade = new PasswordResetFacadeImpl(service);
    }

    @Test
    void solicitarRestablecimiento_pasaElCorreoAlServicio() {
        SolicitudRestablecerPasswordDTO dto = new SolicitudRestablecerPasswordDTO();
        dto.setEmail("ana@example.com");

        facade.solicitarRestablecimiento(dto);

        verify(service).solicitarRestablecimiento("ana@example.com");
    }

    @Test
    void solicitarRestablecimiento_conCuerpoNulo_pasaNullSinFallar() {
        facade.solicitarRestablecimiento(null);

        verify(service).solicitarRestablecimiento(null);
    }

    @Test
    void restablecerPassword_pasaTokenYContrasenaAlServicio() {
        RestablecerPasswordDTO dto = new RestablecerPasswordDTO();
        dto.setToken("tok");
        dto.setNuevaPassword("nuevaClaveSegura");

        facade.restablecerPassword(dto);

        verify(service).restablecerPassword("tok", "nuevaClaveSegura");
    }

    @Test
    void restablecerPassword_conCuerpoNulo_lanzaTokenInvalido() {
        assertThrows(PasswordResetTokenInvalidoException.class, () -> facade.restablecerPassword(null));
    }

    @Test
    void restablecerPassword_propagaElErrorDelServicio() {
        RestablecerPasswordDTO dto = new RestablecerPasswordDTO();
        dto.setToken("tok");
        dto.setNuevaPassword("nuevaClaveSegura");
        org.mockito.Mockito.doThrow(new PasswordResetTokenInvalidoException("caducado"))
                .when(service).restablecerPassword("tok", "nuevaClaveSegura");

        assertThrows(PasswordResetTokenInvalidoException.class, () -> facade.restablecerPassword(dto));
    }

    @Test
    void solicitarRestablecimiento_noPropagaExcepcionesDeServicio_siLasHubiera() {
        SolicitudRestablecerPasswordDTO dto = new SolicitudRestablecerPasswordDTO();
        dto.setEmail("ana@example.com");
        // El facade no captura: el controlador es quien garantiza la respuesta neutra.
        org.mockito.Mockito.doThrow(new RuntimeException("bd caída"))
                .when(service).solicitarRestablecimiento("ana@example.com");

        assertThrows(RuntimeException.class, () -> facade.solicitarRestablecimiento(dto));
    }
}
