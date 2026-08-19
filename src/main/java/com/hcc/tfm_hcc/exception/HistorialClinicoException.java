package com.hcc.tfm_hcc.exception;

/**
 * Excepción personalizada para errores en la gestión del historial clínico.
 * Se lanza cuando hay problemas en operaciones relacionadas con la lectura,
 * actualización o manipulación de datos del historial médico del paciente.
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public class HistorialClinicoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public HistorialClinicoException() {
        super();
    }

    public HistorialClinicoException(String message) {
        super(message);
    }

    public HistorialClinicoException(String message, Throwable cause) {
        super(message, cause);
    }

    public HistorialClinicoException(Throwable cause) {
        super(cause);
    }
}
