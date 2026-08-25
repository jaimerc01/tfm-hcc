package com.hcc.tfm_hcc.converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convertidor de atributos JPA para columnas {@link LocalDateTime} sensibles (p. ej. la
 * fecha de nacimiento, usada para verificar la identidad de un paciente), cifradas en
 * reposo con el mismo esquema AES/GCM que {@link AESEncryptionConverter} usa para atributos
 * {@code String}. Delega en él la criptografía para no duplicarla; solo añade la
 * serialización/parseo entre {@link LocalDateTime} y texto ISO-8601.
 *
 * <p>Compatibilidad con columnas migradas desde un tipo de fecha nativo: si el valor leído
 * no está cifrado (todavía en texto plano porque la fila no se ha vuelto a escribir desde
 * la migración de columna), se admite además el formato de texto que genera Postgres al
 * convertir una columna {@code TIMESTAMP} a texto ({@code "yyyy-MM-dd HH:mm:ss"}), para no
 * romper la lectura de filas ya existentes antes de que se cifren de forma perezosa en su
 * próxima escritura (mismo criterio ya aplicado a {@code DatoClinico.valor}).</p>
 */
@Component
@Converter(autoApply = false)
public class AESEncryptionLocalDateTimeConverter implements AttributeConverter<LocalDateTime, String> {

    private static final DateTimeFormatter POSTGRES_TIMESTAMP_TEXT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSS]");

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
        String texto = aesEncryptionConverter.decryptFromBase64OrPlain(dbData);
        if (texto == null) {
            return null;
        }
        return parseFecha(texto);
    }

    private LocalDateTime parseFecha(String texto) {
        try {
            return LocalDateTime.parse(texto, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException isoFallido) {
            return LocalDateTime.parse(texto, POSTGRES_TIMESTAMP_TEXT_FORMAT);
        }
    }
}
