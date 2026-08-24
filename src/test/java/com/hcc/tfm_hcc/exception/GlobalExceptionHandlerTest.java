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
}
