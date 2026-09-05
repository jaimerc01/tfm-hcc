package com.hcc.tfm_hcc.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.hcc.tfm_hcc.constants.ErrorMessages;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleMensajeIlegible_devuelve400ConMensajeDeJsonMalFormado() {
        HttpInputMessage mensajeEntrada = mock(HttpInputMessage.class);
        HttpMessageNotReadableException excepcion =
                new HttpMessageNotReadableException("cuerpo ilegible", mensajeEntrada);

        ResponseEntity<String> respuesta = handler.handleMensajeIlegible(excepcion);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(ErrorMessages.ERROR_JSON_MAL_FORMADO, respuesta.getBody());
    }

    @Test
    void handleValidacionInvalida_devuelve400ConLosErroresPorCampo() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "usuarioDTO");
        bindingResult.addError(new FieldError("usuarioDTO", "email", "Formato de email inválido"));
        bindingResult.addError(new FieldError("usuarioDTO", "nif", "El campo 'nif' es requerido"));

        MethodParameter parametro = mock(MethodParameter.class);
        MethodArgumentNotValidException excepcion = new MethodArgumentNotValidException(parametro, bindingResult);

        ResponseEntity<Map<String, String>> respuesta = handler.handleValidacionInvalida(excepcion);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("Formato de email inválido", respuesta.getBody().get("email"));
        assertEquals("El campo 'nif' es requerido", respuesta.getBody().get("nif"));
    }

    @Test
    void handleValidacionDatosClinicos_devuelve400ConElMensajeDeLaExcepcion() {
        DatosClinicosValidationException excepcion = new DatosClinicosValidationException("El archivo debe tener un nombre válido");

        ResponseEntity<String> respuesta = handler.handleValidacionDatosClinicos(excepcion);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("El archivo debe tener un nombre válido", respuesta.getBody());
    }

    @Test
    void handleArchivoClinico_sinCausaConcreta_devuelve500Generico() {
        ArchivoClinicoException excepcion = new ArchivoClinicoException(ErrorMessages.ERROR_INTERNO_SERVIDOR);

        ResponseEntity<String> respuesta = handler.handleArchivoClinico(excepcion);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
        assertEquals(ErrorMessages.ERROR_INTERNO_SERVIDOR, respuesta.getBody());
    }

    @Test
    void handleArchivoClinico_conCausaDeArgumentoInvalido_devuelve400ConElMensajeReal() {
        // La cadena real: el controlador envuelve lo que envuelve la fachada, que envuelve
        // el IllegalArgumentException de ArchivoClinicoServiceImpl (p. ej. extensión no permitida).
        ArchivoClinicoException excepcion = new ArchivoClinicoException(
                ErrorMessages.ERROR_INTERNO_SERVIDOR,
                new ArchivoClinicoException("Error interno durante la subida del archivo",
                        new IllegalArgumentException(ErrorMessages.ERROR_EXTENSION_NO_PERMITIDA)));

        ResponseEntity<String> respuesta = handler.handleArchivoClinico(excepcion);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(ErrorMessages.ERROR_EXTENSION_NO_PERMITIDA, respuesta.getBody());
    }

    @Test
    void handleArchivoClinico_conFalloRealDelServidor_devuelve500Generico() {
        ArchivoClinicoException excepcion = new ArchivoClinicoException(
                "Error interno durante la subida del archivo",
                new NullPointerException("bug"));

        ResponseEntity<String> respuesta = handler.handleArchivoClinico(excepcion);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
        assertEquals(ErrorMessages.ERROR_INTERNO_SERVIDOR, respuesta.getBody());
    }

    @Test
    void handleArchivoDemasiadoGrande_devuelve400ConMensajeDeTamañoExcedido() {
        MaxUploadSizeExceededException excepcion = new MaxUploadSizeExceededException(10485760L);

        ResponseEntity<String> respuesta = handler.handleArchivoDemasiadoGrande(excepcion);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(ErrorMessages.ERROR_TAMAÑO_EXCEDIDO, respuesta.getBody());
    }

    @Test
    void handleUsuarioNoEncontrado_devuelve404ConElMensajeDeLaExcepcion() {
        UsuarioNoEncontradoException excepcion = new UsuarioNoEncontradoException("Usuario con ID 'X' no encontrado");

        ResponseEntity<String> respuesta = handler.handleUsuarioNoEncontrado(excepcion);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("Usuario con ID 'X' no encontrado", respuesta.getBody());
    }

    @Test
    void handleHistorialClinico_conCausaDeArgumentoInvalido_devuelve400ConElMensajeReal() {
        // La cadena real: el controlador envuelve lo que envuelve la fachada, que envuelve
        // el IllegalArgumentException de la capa de servicio.
        HistorialClinicoException excepcion = new HistorialClinicoException(
                ErrorMessages.ERROR_INTERNO_SERVIDOR,
                new HistorialClinicoException("Error interno durante la edición del antecedente",
                        new IllegalArgumentException(ErrorMessages.ERROR_NO_PERMITIDO)));

        ResponseEntity<String> respuesta = handler.handleHistorialClinico(excepcion);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(ErrorMessages.ERROR_NO_PERMITIDO, respuesta.getBody());
    }

    @Test
    void handleHistorialClinico_conCausaDeFaltaDeAutenticacion_devuelve401() {
        HistorialClinicoException excepcion = new HistorialClinicoException(
                ErrorMessages.ERROR_INTERNO_SERVIDOR,
                new IllegalStateException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO));

        ResponseEntity<String> respuesta = handler.handleHistorialClinico(excepcion);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
    }

    @Test
    void handleHistorialClinico_conFalloRealDelServidor_devuelve500Generico() {
        HistorialClinicoException excepcion = new HistorialClinicoException(
                "Error interno durante la consulta del historial clínico",
                new NullPointerException("bug"));

        ResponseEntity<String> respuesta = handler.handleHistorialClinico(excepcion);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
        assertEquals(ErrorMessages.ERROR_INTERNO_SERVIDOR, respuesta.getBody());
    }
}
