package com.hcc.tfm_hcc.service.impl;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.service.TotpService;

/**
 * Implementación de TOTP (RFC 6238, construido sobre HOTP de RFC 4226) usando
 * exclusivamente {@code javax.crypto} del JDK, en línea con el resto del cifrado
 * del proyecto (ver {@link com.hcc.tfm_hcc.converter.AESEncryptionConverter}), y
 * Apache Commons Codec solo para la codificación Base32 del secreto (formato que
 * exigen las aplicaciones autenticadoras, sin equivalente en el JDK estándar).
 */
@Service
public class TotpServiceImpl implements TotpService {

    private static final String ALGORITMO_HMAC = "HmacSHA1";
    private static final int DIGITOS = 6;
    private static final int PASO_SEGUNDOS = 30;
    /** Pasos de 30s tolerados antes/después del actual, para absorber el desfase de reloj del dispositivo. */
    private static final int VENTANA_TOLERANCIA_PASOS = 1;
    /** 160 bits: longitud de secreto recomendada por RFC 4226 para HMAC-SHA1. */
    private static final int LONGITUD_SECRETO_BYTES = 20;
    private static final String EMISOR = "HCC-TFM";
    private static final int MODULO_CODIGO = (int) Math.pow(10, DIGITOS);

    private final SecureRandom secureRandom = new SecureRandom();
    private final Base32 base32 = new Base32();

    @Override
    public String generarSecreto() {
        byte[] secreto = new byte[LONGITUD_SECRETO_BYTES];
        secureRandom.nextBytes(secreto);
        return base32.encodeToString(secreto).replace("=", "");
    }

    @Override
    public String generarOtpAuthUri(String secreto, String nombreCuenta) {
        String etiqueta = urlEncode(EMISOR + ":" + nombreCuenta);
        String emisorCodificado = urlEncode(EMISOR);
        return "otpauth://totp/" + etiqueta
                + "?secret=" + secreto
                + "&issuer=" + emisorCodificado
                + "&algorithm=SHA1&digits=" + DIGITOS + "&period=" + PASO_SEGUNDOS;
    }

    @Override
    public boolean validarCodigo(String secretoBase32, String codigo) {
        if (secretoBase32 == null || codigo == null || !codigo.matches("\\d{" + DIGITOS + "}")) {
            return false;
        }

        long pasoActual = Instant.now().getEpochSecond() / PASO_SEGUNDOS;
        boolean valido = false;
        for (int desfase = -VENTANA_TOLERANCIA_PASOS; desfase <= VENTANA_TOLERANCIA_PASOS; desfase++) {
            // Comparación en tiempo constante y sin cortocircuito: se recorren siempre todos
            // los pasos de la ventana para no dar pistas de temporización sobre el código.
            if (comparacionSegura(codigo, generarCodigoParaPaso(secretoBase32, pasoActual + desfase))) {
                valido = true;
            }
        }
        return valido;
    }

    /**
     * Compara dos cadenas en tiempo constante (via {@link MessageDigest#isEqual}) para no
     * filtrar por temporización cuántos dígitos iniciales del código eran correctos.
     */
    private boolean comparacionSegura(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * HMAC-SHA1 es el algoritmo por defecto de TOTP (RFC 6238, sobre HOTP de RFC 4226)
     * y el único que garantizan de forma fiable las aplicaciones autenticadoras. Sigue
     * siendo un MAC seguro: las colisiones conocidas de SHA-1 no comprometen su uso
     * dentro de HMAC. Por eso se suprime aquí la regla java:S4790 ("weak hash algorithm").
     */
    @SuppressWarnings("java:S4790")
    private String generarCodigoParaPaso(String secretoBase32, long paso) {
        try {
            byte[] clave = base32.decode(secretoBase32);
            byte[] contador = ByteBuffer.allocate(8).putLong(paso).array();

            Mac mac = Mac.getInstance(ALGORITMO_HMAC);
            mac.init(new SecretKeySpec(clave, ALGORITMO_HMAC));
            byte[] hash = mac.doFinal(contador);

            int offset = hash[hash.length - 1] & 0x0F;
            int binario = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            int codigo = binario % MODULO_CODIGO;
            return String.format("%0" + DIGITOS + "d", codigo);
        } catch (Exception e) {
            throw new IllegalStateException("Error al generar el código TOTP.", e);
        }
    }

    private String urlEncode(String valor) {
        return java.net.URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }
}
