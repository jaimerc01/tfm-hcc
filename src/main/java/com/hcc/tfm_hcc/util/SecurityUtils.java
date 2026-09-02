package com.hcc.tfm_hcc.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hcc.tfm_hcc.model.Usuario;

/**
 * Utilidades relacionadas con seguridad y autenticación.
 */
public final class SecurityUtils {
    private SecurityUtils() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no debe ser instanciada");
    }

    // Obtiene el NIF del usuario autenticado, o null si no hay autenticación o el principal no es Usuario
    public static String getCurrentUserNif() {
        Usuario usuario = getCurrentUser();
        return usuario != null ? usuario.getNif() : null;
    }

    // Obtiene el usuario autenticado, o null si no hay autenticación o el principal no es Usuario
    public static Usuario getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        return principal instanceof Usuario usuario ? usuario : null;
    }
}
