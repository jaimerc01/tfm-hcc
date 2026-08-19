package com.hcc.tfm_hcc.exception;

/**
 * Excepción personalizada para cuando no se encuentra un paciente.
 * Se lanza cuando una búsqueda de paciente por criterios específicos
 * (DNI, fecha nacimiento, etc.) no retorna resultados.
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public class PacienteNoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PacienteNoEncontradoException() {
        super();
    }

    public PacienteNoEncontradoException(String message) {
        super(message);
    }

    public PacienteNoEncontradoException(String message, Throwable cause) {
        super(message, cause);
    }

    public PacienteNoEncontradoException(Throwable cause) {
        super(cause);
    }
}
