package com.hcc.tfm_hcc.exception;

/**
 * Excepción personalizada para errores en operaciones de solicitudes de asignación.
 * Se lanza cuando hay problemas al crear, listar o procesar solicitudes de asignación
 * de pacientes a médicos.
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public class SolicitudAsignacionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SolicitudAsignacionException() {
        super();
    }

    public SolicitudAsignacionException(String message) {
        super(message);
    }

    public SolicitudAsignacionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SolicitudAsignacionException(Throwable cause) {
        super(cause);
    }
}
