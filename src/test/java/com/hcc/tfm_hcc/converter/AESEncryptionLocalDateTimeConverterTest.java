package com.hcc.tfm_hcc.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AESEncryptionLocalDateTimeConverterTest {

    private static final String CLAVE_PRUEBAS_BASE64 =
            Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    private AESEncryptionLocalDateTimeConverter converter;

    @BeforeEach
    void setUp() {
        EncryptionKeyProvider keyProvider = new EncryptionKeyProvider(CLAVE_PRUEBAS_BASE64);
        converter = new AESEncryptionLocalDateTimeConverter(new AESEncryptionConverter(keyProvider));
    }

    @Test
    void convertToDatabaseColumn_conValorNulo_devuelveNulo() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_conValorNulo_devuelveNulo() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void roundTrip_cifrarYDescifrar_devuelveLaFechaOriginal() {
        LocalDateTime original = LocalDateTime.of(1990, 5, 14, 0, 0, 0);

        String cifrado = converter.convertToDatabaseColumn(original);
        LocalDateTime descifrado = converter.convertToEntityAttribute(cifrado);

        assertNotNull(cifrado);
        assertEquals(original, descifrado);
    }

    @Test
    void convertToDatabaseColumn_noEsDeterminista_mismaFechaProduceCifradosDistintos() {
        LocalDateTime fecha = LocalDateTime.of(1985, 1, 1, 0, 0, 0);

        String cifrado1 = converter.convertToDatabaseColumn(fecha);
        String cifrado2 = converter.convertToDatabaseColumn(fecha);

        assertTrue(!cifrado1.equals(cifrado2));
        assertEquals(fecha, converter.convertToEntityAttribute(cifrado1));
        assertEquals(fecha, converter.convertToEntityAttribute(cifrado2));
    }

    @Test
    void convertToEntityAttribute_conFormatoTextoDePostgresSinCifrar_seParseaComoFallback() {
        // Formato que produce Postgres al convertir una columna TIMESTAMP a texto
        // (ALTER TABLE ... USING fecha_nacimiento::text), antes de que la fila se
        // reescriba y quede cifrada.
        LocalDateTime descifrado = converter.convertToEntityAttribute("1990-05-14 00:00:00");

        assertEquals(LocalDateTime.of(1990, 5, 14, 0, 0, 0), descifrado);
    }

    @Test
    void convertToEntityAttribute_conFormatoTextoDePostgresConMicrosegundos_seParseaComoFallback() {
        LocalDateTime descifrado = converter.convertToEntityAttribute("1990-05-14 00:00:00.123456");

        assertEquals(LocalDateTime.of(1990, 5, 14, 0, 0, 0, 123_456_000), descifrado);
    }
}
