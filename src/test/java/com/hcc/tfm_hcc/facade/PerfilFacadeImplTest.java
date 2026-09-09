package com.hcc.tfm_hcc.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.exception.PerfilOperacionException;
import com.hcc.tfm_hcc.exception.PerfilValidationException;
import com.hcc.tfm_hcc.facade.impl.PerfilFacadeImpl;
import com.hcc.tfm_hcc.model.Perfil;
import com.hcc.tfm_hcc.service.PerfilService;

class PerfilFacadeImplTest {

    private PerfilService perfilService;
    private PerfilFacadeImpl facade;

    @BeforeEach
    void setUp() {
        perfilService = mock(PerfilService.class);
        facade = new PerfilFacadeImpl(perfilService);
    }

    @Test
    void getPerfilByRol_conRolValido_devuelveElPerfil() {
        Perfil perfil = new Perfil();
        perfil.setRol("MEDICO");
        when(perfilService.getPerfilByRol("MEDICO")).thenReturn(perfil);

        assertEquals(perfil, facade.getPerfilByRol("MEDICO"));
    }

    @Test
    void getPerfilByRol_conRolVacio_lanzaPerfilValidationException() {
        assertThrows(PerfilValidationException.class, () -> facade.getPerfilByRol("  "));
    }

    @Test
    void getPerfilByRol_conErrorInesperadoDelServicio_lanzaPerfilOperacionException() {
        when(perfilService.getPerfilByRol("MEDICO")).thenThrow(new RuntimeException("fallo de BD"));

        assertThrows(PerfilOperacionException.class, () -> facade.getPerfilByRol("MEDICO"));
    }
}
