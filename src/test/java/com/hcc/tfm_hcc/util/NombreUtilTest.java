package com.hcc.tfm_hcc.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.model.Usuario;

class NombreUtilTest {

    private Usuario usuario(String nombre, String apellido1, String apellido2) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido1(apellido1);
        usuario.setApellido2(apellido2);
        return usuario;
    }

    @Test
    void nombreCompleto_conNombreYAmbosApellidos_losUneConEspacios() {
        assertEquals("Ana García López", NombreUtil.nombreCompleto(usuario("Ana", "García", "López")));
    }

    @Test
    void nombreCompleto_sinSegundoApellido_omiteLaParteAusente() {
        assertEquals("Ana García", NombreUtil.nombreCompleto(usuario("Ana", "García", null)));
    }

    @Test
    void nombreCompleto_conSegundoApellidoEnBlanco_loOmite() {
        assertEquals("Ana García", NombreUtil.nombreCompleto(usuario("Ana", "García", "  ")));
    }

    @Test
    void nombreCompleto_conUsuarioNulo_devuelveNull() {
        assertNull(NombreUtil.nombreCompleto(null));
    }

    @Test
    void constructor_lanzaUnsupportedOperationException() throws Exception {
        Constructor<NombreUtil> constructor = NombreUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertEquals(UnsupportedOperationException.class, ex.getCause().getClass());
    }
}
