package com.hcc.tfm_hcc.service;

/**
 * Genera un índice de búsqueda determinista (HMAC-SHA256) para atributos que
 * están cifrados de forma no determinista con {@link com.hcc.tfm_hcc.converter.AESEncryptionConverter}
 * pero que necesitan seguir siendo consultables por igualdad (NIF, email, tipo
 * de dato clínico...).
 *
 * <p>El valor cifrado en sí (AES/GCM, con un IV aleatorio por fila) nunca produce
 * el mismo resultado dos veces, por lo que no sirve como columna de búsqueda ni
 * de restricción de unicidad. Este servicio calcula, en su lugar, un HMAC-SHA256
 * del valor en claro con una clave derivada de la clave de cifrado principal
 * (no la misma clave, para no reutilizar material criptográfico entre dos usos
 * distintos). Ese HMAC sí es determinista -el mismo valor en claro produce
 * siempre el mismo índice- pero no permite recuperar el valor original, por lo
 * que puede almacenarse e indexarse en una columna adicional sin debilitar el
 * cifrado del valor real.</p>
 */
public interface HmacSearchIndexService {

    /**
     * Calcula el índice de búsqueda determinista de un valor en claro.
     *
     * @param valorEnClaro valor original (p. ej. un NIF o un email)
     * @return índice HMAC-SHA256 en Base64, o {@code null} si el valor es {@code null}
     */
    String indexar(String valorEnClaro);
}
