package com.hcc.tfm_hcc.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.hcc.tfm_hcc.constants.ErrorMessages;

import lombok.extern.slf4j.Slf4j;

/**
 * Manejador global para errores de parseo y validación de peticiones entrantes.
 *
 * <p>Sin este manejador, un JSON mal formado o un fallo de validación de {@code @Valid}
 * provoca que el contenedor reenvíe la petición internamente a {@code /error} para
 * construir la respuesta de error por defecto. Ese reenvío es un despacho de tipo
 * ERROR que {@code JwtAuthenticationFilter} (por ser un {@code OncePerRequestFilter})
 * no procesa por defecto, dejando esa segunda pasada sin autenticar; como {@code /error}
 * no está en la lista de rutas públicas de {@code WebSecurityConfig}, Spring Security
 * la rechaza con 403 en lugar de devolver el 400 real, ocultando el verdadero error de
 * validación tras un "Forbidden" engañoso. Resolviendo la excepción aquí, dentro del
 * mismo ciclo de la petición original, se evita ese reenvío y se devuelve el código de
 * estado correcto.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Cuerpo de la petición no legible: JSON mal formado o tipos incompatibles
     * (por ejemplo, una fecha que no cumple el formato esperado).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleMensajeIlegible(HttpMessageNotReadableException e) {
        log.warn("Cuerpo de la petición ilegible o mal formado: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorMessages.ERROR_JSON_MAL_FORMADO);
    }

    /**
     * Fallo de validación de un DTO anotado con {@code @Valid}.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidacionInvalida(MethodArgumentNotValidException e) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errores.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("Validación de datos de entrada fallida: {}", errores);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }

    /**
     * Datos clínicos inválidos (por ejemplo, un archivo sin nombre o un JSON
     * de dato clínico mal formado). Sin este manejador, {@code DatosClinicosValidationException}
     * caía en el manejo de error por defecto de Spring Boot (500 genérico) en
     * lugar del 400 que corresponde a un error de validación del cliente.
     */
    @ExceptionHandler(DatosClinicosValidationException.class)
    public ResponseEntity<String> handleValidacionDatosClinicos(DatosClinicosValidationException e) {
        log.warn("Validación de datos clínicos fallida: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    /**
     * Error inesperado durante la gestión de archivos clínicos (subida, descarga
     * o eliminación). Igual que el resto de manejadores, evita que la excepción
     * caiga en el manejo de error por defecto de Spring Boot.
     */
    @ExceptionHandler(ArchivoClinicoException.class)
    public ResponseEntity<String> handleArchivoClinico(ArchivoClinicoException e) {
        log.error("Error en la gestión de archivos clínicos: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    /**
     * Archivo subido que excede el límite configurado en
     * {@code spring.servlet.multipart.max-file-size}. Se lanza durante el
     * parseo del multipart, antes de que la petición llegue al controlador,
     * por lo que la validación de tamaño de {@code ArchivoClinicoServiceImpl}
     * nunca llega a ejecutarse en este caso.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleArchivoDemasiadoGrande(MaxUploadSizeExceededException e) {
        log.warn("Archivo subido demasiado grande: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorMessages.ERROR_TAMAÑO_EXCEDIDO);
    }

    /**
     * Usuario inexistente al buscar por NIF (panel de administración).
     *
     * <p>{@link UsuarioNoEncontradoException} lleva {@code @ResponseStatus(NOT_FOUND)}, pero
     * si se deja que Spring la resuelva por esa anotación acaba haciendo
     * {@code response.sendError(404)}, lo que dispara un reenvío interno a {@code /error}.
     * Ese reenvío es un despacho ERROR que {@code JwtAuthenticationFilter} no procesa y,
     * como {@code /error} no es una ruta pública, Spring Security responde <b>403</b> en
     * lugar del 404 real (mismo problema descrito en el Javadoc de esta clase para el JSON
     * mal formado). Resolviéndola aquí, dentro del ciclo de la petición, el cliente recibe
     * el 404 que espera (el frontend lo usa para pasar al alta completa del médico).</p>
     */
    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<String> handleUsuarioNoEncontrado(UsuarioNoEncontradoException e) {
        log.warn("Usuario no encontrado: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    /**
     * Error en una operación de historial clínico.
     *
     * <p>Los controladores y fachadas de historial clínico envuelven cualquier fallo
     * inesperado en {@link HistorialClinicoException}, incluyendo los errores atribuibles
     * al cliente (validación de un JSON de análisis, id inexistente, dato que no pertenece
     * al usuario...) que la capa de servicio expresa como {@link IllegalArgumentException}.
     * Sin este manejador todos ellos acababan como un {@code 500} genérico que además
     * ocultaba el motivo real. Aquí se recorre la cadena de causas para devolver:</p>
     * <ul>
     *   <li>{@code 401} si el origen es una falta de autenticación,</li>
     *   <li>{@code 400} con el mensaje real si el origen es un {@link IllegalArgumentException}
     *       (petición mal formada o recurso inexistente/ajeno),</li>
     *   <li>{@code 500} genérico solo cuando el fallo es realmente del servidor.</li>
     * </ul>
     */
    @ExceptionHandler(HistorialClinicoException.class)
    public ResponseEntity<String> handleHistorialClinico(HistorialClinicoException e) {
        Throwable causaRelevante = buscarCausaRelevante(e);

        // La capa de servicio protege el acceso con un IllegalStateException("usuario no
        // autenticado") en obtenerUsuarioAutenticado(); es el único IllegalStateException
        // que puede acabar envuelto en un HistorialClinicoException.
        if (causaRelevante instanceof UsuarioNoAutenticadoException
                || causaRelevante instanceof IllegalStateException) {
            log.warn("Operación de historial clínico sin usuario autenticado: {}", causaRelevante.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }

        if (causaRelevante instanceof IllegalArgumentException) {
            log.warn("Operación de historial clínico rechazada: {}", causaRelevante.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(causaRelevante.getMessage());
        }

        log.error("Error interno en operación de historial clínico: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorMessages.ERROR_INTERNO_SERVIDOR);
    }

    /**
     * Recorre la cadena de causas (máximo 10 niveles, por prudencia frente a ciclos)
     * buscando la primera que sea significativa para decidir el código de estado:
     * una falta de autenticación, un estado inválido o un error de argumento. Si no
     * encuentra ninguna, devuelve la excepción original.
     */
    private Throwable buscarCausaRelevante(Throwable e) {
        Throwable actual = e;
        for (int i = 0; i < 10 && actual != null; i++) {
            if (actual instanceof UsuarioNoAutenticadoException
                    || actual instanceof IllegalArgumentException
                    || actual instanceof IllegalStateException) {
                return actual;
            }
            actual = actual.getCause();
        }
        return e;
    }
}
