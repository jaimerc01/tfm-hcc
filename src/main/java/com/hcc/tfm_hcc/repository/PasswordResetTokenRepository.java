package com.hcc.tfm_hcc.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hcc.tfm_hcc.model.PasswordResetToken;

/**
 * Repositorio de los tokens de un solo uso del flujo de restablecimiento de
 * contraseña.
 */
public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {

    /**
     * Busca un token por el hash SHA-256 del valor recibido en el enlace de correo.
     *
     * @param tokenHash hash hexadecimal del token
     * @return el token si existe
     */
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /**
     * Elimina todos los tokens pendientes de un usuario. Se usa para invalidar
     * solicitudes anteriores cuando se genera una nueva y tras un restablecimiento
     * con éxito.
     *
     * @param usuarioId UUID (como texto) del usuario
     * @return número de documentos eliminados
     */
    long deleteByUsuarioId(String usuarioId);
}
