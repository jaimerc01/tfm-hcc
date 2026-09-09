package com.hcc.tfm_hcc.util;

/**
 * Enmascara identificadores personales (NIF, DNI...) antes de escribirlos en los
 * logs técnicos de la aplicación.
 *
 * <p>Los logs de aplicación (consola/fichero) no están cifrados ni tienen la misma
 * política de retención que la base de datos, así que volcar el NIF completo en
 * cada intento de login o cada operación administrativa iba en contra del
 * principio de minimización de datos del RGPD: cualquiera con acceso a los logs
 * podía leer el documento de identidad de cualquier usuario mencionado en ellos,
 * sin que ese acceso quedara sujeto a los mismos controles que el acceso a los
 * datos cifrados en base de datos.</p>
 *
 * <p>Se mantienen visibles los últimos caracteres para que dos líneas de log del
 * mismo usuario se puedan seguir correlacionando a simple vista (útil para
 * depuración y soporte) sin exponer el identificador completo.</p>
 */
public final class LogMaskUtil {

    private static final int CARACTERES_VISIBLES = 3;
    private static final String MASCARA = "***";
    private static final String VALOR_NULO = "null";

    private LogMaskUtil() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no debe ser instanciada");
    }

    /**
     * Enmascara un identificador sensible dejando visibles solo sus últimos
     * caracteres, p. ej. {@code "12345678A"} → {@code "***78A"}.
     *
     * @param valor identificador a enmascarar (p. ej. un NIF); puede ser {@code null}
     * @return el valor enmascarado, {@code "null"} si {@code valor} es {@code null},
     *         o {@code "***"} si es demasiado corto para enmascarar con utilidad
     */
    public static String enmascarar(String valor) {
        if (valor == null) {
            return VALOR_NULO;
        }
        if (valor.isBlank() || valor.length() <= CARACTERES_VISIBLES) {
            return MASCARA;
        }
        return MASCARA + valor.substring(valor.length() - CARACTERES_VISIBLES);
    }
}
