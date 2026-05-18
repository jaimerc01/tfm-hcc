package com.hcc.tfm_hcc.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hcc.tfm_hcc.model.Usuario;

/**
 * Utilidades relacionadas con seguridad y autenticación.
 */
public final class SecurityUtils {
    private SecurityUtils() {
        // Constructor privado para evitar instanciación
    }

    // Obtiene el NIF del usuario autenticado, o null si no hay autenticación o el principal no es Usuario
    public static String getCurrentUserNif() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof Usuario usuario) {
            return usuario.getNif();
        }
        return null;
    }
}
