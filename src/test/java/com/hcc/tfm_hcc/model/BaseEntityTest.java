package com.hcc.tfm_hcc.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class BaseEntityTest {

    private static Perfil perfilConId(UUID id) {
        Perfil p = new Perfil();
        p.setId(id);
        return p;
    }

    @Test
    void equals_mismasEntidadesConElMismoId_sonIguales() {
        UUID id = UUID.randomUUID();
        assertEquals(perfilConId(id), perfilConId(id));
        assertEquals(perfilConId(id).hashCode(), perfilConId(id).hashCode());
    }

    @Test
    void equals_mismasEntidadesConDistintoId_noSonIguales() {
        assertNotEquals(perfilConId(UUID.randomUUID()), perfilConId(UUID.randomUUID()));
    }

    @Test
    void equals_dosEntidadesSinPersistir_soloSonIgualesSiSonLaMismaInstancia() {
        Perfil a = new Perfil();
        Perfil b = new Perfil();
        assertNotEquals(a, b);
        assertEquals(a, a);
    }

    @Test
    void equals_tiposDistintosConElMismoId_noSonIguales() {
        UUID id = UUID.randomUUID();
        Perfil perfil = perfilConId(id);
        Rango rango = new Rango();
        rango.setId(id);

        assertNotEquals(perfil, rango);
    }

    @Test
    void hashCode_esEstableAunqueMutenLosCampos() {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        int hashInicial = usuario.hashCode();

        usuario.setNombre("Ana");
        usuario.setEstadoCuenta(Usuario.ESTADO_CUENTA_ACTIVO);
        usuario.setAuthorities(java.util.List.of());

        assertEquals(hashInicial, usuario.hashCode());
    }

    @Test
    void entidad_funcionaComoClaveEnUnConjunto() {
        UUID id = UUID.randomUUID();
        Set<Perfil> set = new HashSet<>();
        set.add(perfilConId(id));

        assertTrue(set.contains(perfilConId(id)));
        assertFalse(set.contains(perfilConId(UUID.randomUUID())));
    }
}
