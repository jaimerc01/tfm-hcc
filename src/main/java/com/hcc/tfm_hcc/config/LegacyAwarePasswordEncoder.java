package com.hcc.tfm_hcc.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hcc.tfm_hcc.converter.AESEncryptionConverter;

/**
 * PasswordEncoder de transición que mantiene BCrypt para nuevas contraseñas
 * y permite validar hashes antiguos que quedaron cifrados en la base de datos.
 */
public class LegacyAwarePasswordEncoder implements PasswordEncoder {

    private final BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();
    private final AESEncryptionConverter encryptionConverter;

    public LegacyAwarePasswordEncoder(AESEncryptionConverter encryptionConverter) {
        this.encryptionConverter = encryptionConverter;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return bcryptPasswordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }

        if (isBcryptHash(encodedPassword)) {
            return bcryptPasswordEncoder.matches(rawPassword, encodedPassword);
        }

        try {
            String decryptedPassword = encryptionConverter.convertToEntityAttribute(encodedPassword);
            return decryptedPassword != null
                && isBcryptHash(decryptedPassword)
                && bcryptPasswordEncoder.matches(rawPassword, decryptedPassword);
        } catch (Exception _) {
            return false;
        }
    }

    private boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}