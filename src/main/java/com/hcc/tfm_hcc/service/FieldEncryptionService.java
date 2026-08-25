package com.hcc.tfm_hcc.service;

/**
 * Cifrado/descifrado de campos de texto sensibles almacenados fuera de JPA
 * (por ejemplo, documentos de MongoDB), donde no se puede aplicar el mecanismo
 * declarativo {@code @Convert} usado en las entidades JPA.
 *
 * Usa AES/GCM (autenticado, con un vector de inicialización aleatorio en cada
 * llamada), por lo que el mismo valor en claro nunca produce el mismo resultado
 * cifrado dos veces. La clave se comparte con el resto del cifrado de la
 * aplicación (ver {@code EncryptionKeyProvider}).
 */
public interface FieldEncryptionService {

    /**
     * Cifra un valor en claro. Devuelve {@code null} si la entrada es {@code null}.
     *
     * @param texto valor en claro a cifrar
     * @return el valor cifrado, codificado en Base64
     */
    String cifrar(String texto);

    /**
     * Descifra un valor previamente cifrado con {@link #cifrar(String)}.
     *
     * <p>Si el valor no tiene el formato de un ciphertext válido (por ejemplo,
     * documentos ya existentes antes de activar este cifrado), se devuelve tal
     * cual, asumiendo que es texto plano heredado.</p>
     *
     * @param valorAlmacenado valor tal y como está guardado
     * @return el valor en claro
     */
    String descifrar(String valorAlmacenado);
}
