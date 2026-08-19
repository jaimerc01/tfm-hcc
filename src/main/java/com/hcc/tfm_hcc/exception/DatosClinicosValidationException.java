package com.hcc.tfm_hcc.exception;

/**
 * Excepción personalizada para errores de validación de datos clínicos.
 * Se lanza cuando los datos proporcionados no cumplen con los requisitos
 * de validación esperados (campos requeridos, formato inválido, etc.).
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public class DatosClinicosValidationException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public DatosClinicosValidationException() {
        super();
    }

    public DatosClinicosValidationException(String message) {
        super(message);
    }

    public DatosClinicosValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
