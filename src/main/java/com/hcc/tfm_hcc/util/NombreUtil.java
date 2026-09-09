package com.hcc.tfm_hcc.util;

import com.hcc.tfm_hcc.model.Usuario;

/**
 * Construye el nombre completo mostrable de un usuario a partir de su nombre y
 * apellidos, omitiendo las partes ausentes (p. ej. el segundo apellido, opcional).
 */
public final class NombreUtil {

    private NombreUtil() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no debe ser instanciada");
    }

    /**
     * @param usuario usuario del que construir el nombre completo; puede ser {@code null}
     * @return {@code "nombre apellido1 apellido2"} con las partes ausentes omitidas,
     *         o {@code null} si {@code usuario} es {@code null}
     */
    public static String nombreCompleto(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        StringBuilder nombreCompleto = new StringBuilder();
        appendSiPresente(nombreCompleto, usuario.getNombre());
        appendSiPresente(nombreCompleto, usuario.getApellido1());
        appendSiPresente(nombreCompleto, usuario.getApellido2());
        return nombreCompleto.toString();
    }

    private static void appendSiPresente(StringBuilder builder, String parte) {
        if (parte != null && !parte.isBlank()) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(parte);
        }
    }
}
