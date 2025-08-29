package com.hcc.tfm_hcc.constants;

/**
 * Clase centralizada que contiene todos los mensajes de error utilizados en la aplicación.
 * Esta clase proporciona consistencia en los mensajes y facilita su mantenimiento.
 * 
 * @author Sistema HCC
 * @version 1.0
 */
public final class ErrorMessages {

    // Constructor privado para evitar instanciación
    private ErrorMessages() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no debe ser instanciada");
    }

    // =====================================================
    // MENSAJES DE ERROR - AUTENTICACIÓN Y AUTORIZACIÓN
    // =====================================================
    
    public static final String ERROR_USUARIO_NO_AUTENTICADO = "Usuario no autenticado";
    public static final String ERROR_USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
    public static final String ERROR_ACCESO_DENEGADO = "Acceso denegado";
    public static final String ERROR_SESION_EXPIRADA = "La sesión ha expirado";
    public static final String ERROR_CREDENCIALES_INVALIDAS = "Credenciales inválidas";
    public static final String ERROR_USUARIO_BLOQUEADO = "Usuario bloqueado";
    public static final String ERROR_PERMISOS_INSUFICIENTES = "Permisos insuficientes";

    // =====================================================
    // MENSAJES DE ERROR - GESTIÓN DE ARCHIVOS
    // =====================================================
    
    public static final String ERROR_NO_ARCHIVO = "No se ha enviado ningún archivo";
    public static final String ERROR_ARCHIVO_VACIO = "El archivo está vacío";
    public static final String ERROR_TAMAÑO_EXCEDIDO = "El archivo supera el tamaño máximo permitido";
    public static final String ERROR_NOMBRE_INVALIDO = "Nombre de archivo no válido";
    public static final String ERROR_TIPO_NO_PERMITIDO = "Tipo de archivo no permitido";
    public static final String ERROR_GUARDAR_ARCHIVO = "No se pudo guardar el archivo";
    public static final String ERROR_ARCHIVO_NO_EXISTE = "No existe el archivo";
    public static final String ERROR_ARCHIVO_NO_ACCESIBLE = "Archivo no accesible";
    public static final String ERROR_RUTA_INVALIDA = "Ruta inválida";
    public static final String ERROR_ELIMINAR_ARCHIVO = "No se pudo eliminar el archivo";
    public static final String ERROR_CREAR_DIRECTORIO = "No se pudo crear el directorio";
    public static final String ERROR_ARCHIVO_CORRUPTO = "El archivo está corrupto";
    public static final String ERROR_EXTENSION_NO_PERMITIDA = "Extensión de archivo no permitida";

    // =====================================================
    // MENSAJES DE ERROR - HISTORIAL CLÍNICO
    // =====================================================
    
    public static final String ERROR_HISTORIAL_NO_EXISTE = "No existe historial clínico";
    public static final String ERROR_DATO_NO_ENCONTRADO = "Dato clínico no encontrado";
    public static final String ERROR_NO_PERMITIDO = "Operación no permitida";
    public static final String ERROR_ANTECEDENTE_NO_ENCONTRADO = "Antecedente no encontrado";
    public static final String ERROR_INDICE_FUERA_RANGO = "Índice fuera de rango";
    public static final String ERROR_NO_HAY_ANTECEDENTES = "No hay antecedentes registrados";
    public static final String ERROR_FORMATO_FECHA_INVALIDO = "Formato de fecha inválido";
    public static final String ERROR_DATO_YA_EXISTE = "El dato clínico ya existe";

    // =====================================================
    // MENSAJES DE ERROR - VALIDACIÓN DE DATOS
    // =====================================================
    
    public static final String ERROR_CAMPO_REQUERIDO = "Campo requerido";
    public static final String ERROR_FORMATO_INVALIDO = "Formato inválido";
    public static final String ERROR_VALOR_FUERA_RANGO = "Valor fuera del rango permitido";
    public static final String ERROR_LONGITUD_INVALIDA = "Longitud inválida";
    public static final String ERROR_CARACTERES_INVALIDOS = "Caracteres no válidos";
    public static final String ERROR_EMAIL_INVALIDO = "Formato de email inválido";
    public static final String ERROR_EMAIL_YA_EXISTE = "El email ya está en uso";
    public static final String ERROR_TELEFONO_INVALIDO = "Formato de teléfono inválido";
    public static final String ERROR_DNI_INVALIDO = "Formato de DNI inválido";
    public static final String ERROR_DNI_YA_EXISTE = "El DNI ya está en uso";
    public static final String ERROR_FECHA_INVALIDA = "Fecha inválida";
    public static final String ERROR_HORA_INVALIDA = "Hora inválida";

    // =====================================================
    // MENSAJES DE ERROR - JSON Y SERIALIZACIÓN
    // =====================================================
    
    public static final String ERROR_JSON_INVALIDO = "JSON inválido";
    public static final String ERROR_JSON_MAL_FORMADO = "JSON mal formado";
    public static final String ERROR_CAMPO_JSON_FALTANTE = "Campo requerido faltante en JSON";
    public static final String ERROR_TIPO_JSON_INCORRECTO = "Tipo de dato incorrecto en JSON";
    public static final String ERROR_ANALISIS_JSON_ARRAY_ESPERADO = "Se esperaba un array JSON para análisis de sangre";
    public static final String ERROR_ANALISIS_VALUE_REQUERIDO = "Cada entrada debe incluir 'value'";
    public static final String ERROR_VALOR_NUMERICO_INVALIDO = "Valor numérico inválido";

    // =====================================================
    // MENSAJES DE ERROR - BASE DE DATOS
    // =====================================================
    
    public static final String ERROR_BD_CONEXION = "Error de conexión a la base de datos";
    public static final String ERROR_BD_CONSULTA = "Error en la consulta a la base de datos";
    public static final String ERROR_BD_INTEGRIDAD = "Error de integridad en la base de datos";
    public static final String ERROR_BD_DUPLICADO = "Registro duplicado";
    public static final String ERROR_BD_REFERENCIA = "Error de referencia en la base de datos";
    public static final String ERROR_BD_TRANSACCION = "Error en la transacción";
    public static final String ERROR_ENTIDAD_NO_ENCONTRADA = "Entidad no encontrada";
    public static final String ERROR_OPTIMISTIC_LOCKING = "El registro ha sido modificado por otro usuario";

    // =====================================================
    // MENSAJES DE ERROR - MÉDICOS Y ESPECIALIDADES
    // =====================================================
    
    public static final String ERROR_MEDICO_NO_ENCONTRADO = "Médico no encontrado";
    public static final String ERROR_ESPECIALIDAD_NO_ENCONTRADA = "Especialidad no encontrada";
    public static final String ERROR_MEDICO_NO_DISPONIBLE = "Médico no disponible";
    public static final String ERROR_HORARIO_NO_DISPONIBLE = "Horario no disponible";
    public static final String ERROR_CITA_NO_ENCONTRADA = "Cita no encontrada";
    public static final String ERROR_CITA_YA_EXISTE = "Ya existe una cita en ese horario";
    public static final String ERROR_CANCELAR_CITA = "No se puede cancelar la cita";
    public static final String ERROR_LISTADO_MEDICOS = "Error al listar médicos: {0}";
    public static final String ERROR_CREAR_MEDICO = "Error al crear médico: {0}";
    public static final String PERFIL_NO_ENCONTRADO = "Perfil '{0}' no encontrado";

    // =====================================================
    // MENSAJES DE ERROR - NOTIFICACIONES
    // =====================================================
    
    public static final String ERROR_NOTIFICACION_NO_ENCONTRADA = "Notificación no encontrada";
    public static final String ERROR_ENVIAR_NOTIFICACION = "Error al enviar notificación";
    public static final String ERROR_DESTINATARIO_INVALIDO = "Destinatario inválido";
    public static final String ERROR_MENSAJE_VACIO = "El mensaje no puede estar vacío";

    // =====================================================
    // MENSAJES DE ERROR - SISTEMA
    // =====================================================
    
    public static final String ERROR_INTERNO_SERVIDOR = "Error interno del servidor";
    public static final String ERROR_SERVICIO_NO_DISPONIBLE = "Servicio no disponible";
    public static final String ERROR_TIMEOUT = "Tiempo de espera agotado";
    public static final String ERROR_CONFIGURACION = "Error de configuración";
    public static final String ERROR_OPERACION_NO_SOPORTADA = "Operación no soportada";
    public static final String ERROR_RECURSO_NO_DISPONIBLE = "Recurso no disponible";

    // =====================================================
    // MÉTODOS DE UTILIDAD PARA MENSAJES PARAMETRIZADOS
    // =====================================================

    /**
     * Formatea un mensaje de error con parámetros
     * 
     * @param template Plantilla del mensaje con placeholders {0}, {1}, etc.
     * @param params Parámetros a sustituir en la plantilla
     * @return Mensaje formateado
     */
    public static String formatError(String template, Object... params) {
        if (template == null || params == null) {
            return template;
        }
        
        String result = template;
        for (int i = 0; i < params.length; i++) {
            String placeholder = "{" + i + "}";
            String value = params[i] != null ? params[i].toString() : "null";
            result = result.replace(placeholder, value);
        }
        
        return result;
    }

    /**
     * Crea un mensaje de error para campo requerido
     * 
     * @param nombreCampo Nombre del campo requerido
     * @return Mensaje de error formateado
     */
    public static String campoRequerido(String nombreCampo) {
        return formatError("El campo '{0}' es requerido", nombreCampo);
    }

    /**
     * Crea un mensaje de error para valor fuera de rango
     * 
     * @param valor Valor que está fuera de rango
     * @param minimo Valor mínimo permitido
     * @param maximo Valor máximo permitido
     * @return Mensaje de error formateado
     */
    public static String valorFueraDeRango(Object valor, Object minimo, Object maximo) {
        return formatError("El valor '{0}' está fuera del rango permitido [{1} - {2}]", valor, minimo, maximo);
    }

    /**
     * Crea un mensaje de error para longitud inválida
     * 
     * @param campo Nombre del campo
     * @param longitudActual Longitud actual
     * @param longitudEsperada Longitud esperada
     * @return Mensaje de error formateado
     */
    public static String longitudInvalida(String campo, int longitudActual, int longitudEsperada) {
        return formatError("El campo '{0}' tiene {1} caracteres, se esperaban {2}", 
                          campo, longitudActual, longitudEsperada);
    }

    /**
     * Crea un mensaje de error para entidad no encontrada
     * 
     * @param entidad Tipo de entidad
     * @param id Identificador de la entidad
     * @return Mensaje de error formateado
     */
    public static String entidadNoEncontrada(String entidad, Object id) {
        return formatError("{0} con ID '{1}' no encontrado", entidad, id);
    }
}
