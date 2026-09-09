package com.hcc.tfm_hcc.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class UsuarioTest {

    private Usuario usuarioCompleto() {
        Usuario u = new Usuario();
        u.setNombre("Ana");
        u.setApellido1("García");
        u.setApellido2("López");
        u.setNif("12345678Z");
        u.setNifHash("hash-nif");
        u.setEmail("ana@example.com");
        u.setEmailHash("hash-email");
        u.setTelefono("600123123");
        u.setPassword("$2a$10$hashbcryptsecreto");
        u.setTotpSecret("JBSWY3DPEHPK3PXP");
        return u;
    }

    @Test
    void isEnabled_conCuentaActiva_devuelveTrue() {
        Usuario u = new Usuario();
        u.setEstadoCuenta(Usuario.ESTADO_CUENTA_ACTIVO);
        assertTrue(u.isEnabled());
    }

    @Test
    void isEnabled_conEstadoNulo_devuelveTrue() {
        assertTrue(new Usuario().isEnabled());
    }

    @Test
    void isEnabled_conCuentaSuspendida_devuelveTrue() {
        // La limitación del tratamiento (art. 18 RGPD) no impide el acceso del titular.
        Usuario u = new Usuario();
        u.setEstadoCuenta(Usuario.ESTADO_CUENTA_SUSPENDIDO);
        assertTrue(u.isEnabled());
    }

    @Test
    void isEnabled_conCuentaEliminada_devuelveFalse() {
        Usuario u = new Usuario();
        u.setEstadoCuenta(Usuario.ESTADO_CUENTA_ELIMINADO);
        assertFalse(u.isEnabled());
    }

    @Test
    void toString_noExponeContrasenaNiDatosPersonales() {
        String salida = usuarioCompleto().toString();

        assertFalse(salida.contains("$2a$10$hashbcryptsecreto"), "no debe aparecer el hash de la contraseña");
        assertFalse(salida.contains("JBSWY3DPEHPK3PXP"), "no debe aparecer el secreto TOTP");
        assertFalse(salida.contains("12345678Z"), "no debe aparecer el NIF");
        assertFalse(salida.contains("ana@example.com"), "no debe aparecer el email");
        assertFalse(salida.contains("600123123"), "no debe aparecer el teléfono");
        assertFalse(salida.contains("Ana"), "no debe aparecer el nombre");
        assertFalse(salida.contains("hash-nif"), "no debe aparecer el índice de búsqueda del NIF");
    }

    @Test
    void serializacionJson_omiteContrasenaSecretoTotpYHashes() throws Exception {
        Usuario u = usuarioCompleto();
        u.setFechaNacimiento(null); // evita necesitar el módulo de java.time en este test

        String json = new ObjectMapper().writeValueAsString(u);

        assertFalse(json.contains("\"password\""), "password no debe serializarse");
        assertFalse(json.contains("\"totpSecret\""), "totpSecret no debe serializarse");
        assertFalse(json.contains("\"nifHash\""), "nifHash no debe serializarse");
        assertFalse(json.contains("\"emailHash\""), "emailHash no debe serializarse");
        assertFalse(json.contains("hashbcryptsecreto"));
        assertFalse(json.contains("JBSWY3DPEHPK3PXP"));
    }
}
