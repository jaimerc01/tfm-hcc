package com.hcc.tfm_hcc.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hcc.tfm_hcc.model.Usuario;

class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserNif_conUsuarioAutenticado_devuelveSuNif() {
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null));

        assertEquals("12345678A", SecurityUtils.getCurrentUserNif());
    }

    @Test
    void getCurrentUserNif_sinAutenticacion_devuelveNull() {
        assertNull(SecurityUtils.getCurrentUserNif());
    }

    @Test
    void getCurrentUserNif_conPrincipalQueNoEsUsuario_devuelveNull() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("no-es-un-usuario", null));

        assertNull(SecurityUtils.getCurrentUserNif());
    }
}
