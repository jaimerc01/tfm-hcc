package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.exception.PerfilNotFoundException;
import com.hcc.tfm_hcc.model.Perfil;
import com.hcc.tfm_hcc.repository.PerfilRepository;
import com.hcc.tfm_hcc.service.impl.PerfilServiceImpl;

class PerfilServiceImplTest {

    private PerfilRepository perfilRepository;
    private PerfilServiceImpl service;

    @BeforeEach
    void setUp() {
        perfilRepository = mock(PerfilRepository.class);
        service = new PerfilServiceImpl(perfilRepository);
    }

    @Test
    void getPerfilByRol_conRolExistente_devuelveElPerfil() {
        Perfil perfil = new Perfil();
        perfil.setRol("MEDICO");
        when(perfilRepository.getPerfilByRol("MEDICO")).thenReturn(Optional.of(perfil));

        Perfil resultado = service.getPerfilByRol("MEDICO");

        assertEquals("MEDICO", resultado.getRol());
    }

    @Test
    void getPerfilByRol_conRolInexistente_lanzaPerfilNotFoundException() {
        when(perfilRepository.getPerfilByRol("INEXISTENTE")).thenReturn(Optional.empty());

        assertThrows(PerfilNotFoundException.class, () -> service.getPerfilByRol("INEXISTENTE"));
    }

    @Test
    void getPerfilByRol_conRolNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.getPerfilByRol(null));
    }

    @Test
    void getPerfilByRol_conRolVacio_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.getPerfilByRol("   "));
    }
}
