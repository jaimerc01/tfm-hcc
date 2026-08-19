package com.hcc.tfm_hcc.exception;

/**
 * Excepción personalizada para errores en la gestión de archivos clínicos.
 * Se lanza cuando hay problemas en operaciones de subida, descarga, eliminación
 * o procesamiento de archivos del historial clínico.
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public class ArchivoClinicoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ArchivoClinicoException() {
        super();
    }

    public ArchivoClinicoException(String message) {
        super(message);
    }

    public ArchivoClinicoException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArchivoClinicoException(Throwable cause) {
        super(cause);
    }
}
