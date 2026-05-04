package com.hcc.tfm_hcc.converter;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import jakarta.persistence.AttributeConverter;

public class AESEncryptionConverter  implements AttributeConverter<String, String> {

    private final String secretKey = "1234567890123456"; // Clave secreta de 16 bytes (128 bits)
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null; 
        }
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes()));
        } catch (Exception e) {
            throw new IllegalStateException("Error al cifrar el atributo: " + attribute, e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if( dbData == null) {
            return null; 
        }
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedData = Base64.getDecoder().decode(dbData);
            return new String(cipher.doFinal(decodedData));
        } catch (Exception e) {
            throw new IllegalStateException("Error al descifrar el dato de la base de datos: " + dbData, e);
        }
    }
    
}
