package com.hcc.tfm_hcc.converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convertidor de atributos JPA para columnas {@link LocalDateTime} sensibles (p. ej. la
 * fecha de nacimiento, usada para verificar la identidad de un paciente), cifradas en
 * reposo con el mismo esquema AES/GCM que {@link AESEncryptionConverter} usa para atributos
 * {@code String}. Delega en él la criptografía para no duplicarla; solo añade la
 * serialización/parseo entre {@link LocalDateTime} y texto ISO-8601.
 */
@Component
@Converter(autoApply = false)
public class AESEncryptionLocalDateTimeConverter implements AttributeConverter<LocalDateTime, String> {

    private final AESEncryptionConverter aesEncryptionConverter;

    public AESEncryptionLocalDateTimeConverter(AESEncryptionConverter aesEncryptionConverter) {
        this.aesEncryptionConverter = aesEncryptionConverter;
    }

    @Override
    public String convertToDatabaseColumn(LocalDateTime attribute) {
        if (attribute == null) {
            return null;
        }
        return aesEncryptionConverter.encryptToBase64(attribute.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return LocalDateTime.parse(
                aesEncryptionConverter.decryptFromBase64(dbData),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
