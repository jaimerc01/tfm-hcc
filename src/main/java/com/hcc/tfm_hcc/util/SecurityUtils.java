package com.hcc.tfm_hcc.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hcc.tfm_hcc.model.Usuario;

/**
 * Utility helpers to access security principal information.
 */
public final class SecurityUtils {
    private SecurityUtils() {}

    public static String getCurrentUserNif() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof Usuario) {
            return ((Usuario) principal).getNif();
        }
        return null;
    }
}
