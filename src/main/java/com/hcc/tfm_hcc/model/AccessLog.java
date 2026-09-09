package com.hcc.tfm_hcc.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un registro de acceso (log) en el sistema HCC.
 * Almacena información detallada sobre cada solicitud HTTP realizada
 * por los usuarios del sistema para fines de auditoría y monitoreo.
 * 
 * <p>Esta entidad registra:</p>
 * <ul>
 *   <li>Información temporal del acceso (timestamp)</li>
 *   <li>Identificación del usuario que realizó la solicitud</li>
 *   <li>Detalles técnicos de la solicitud HTTP</li>
 *   <li>Métricas de rendimiento y estado de respuesta</li>
 * </ul>
 *
 * <p><b>Cifrado en reposo:</b> a diferencia de {@code AuditoriaCambio} (que sí cifra
 * {@code valorAnterior}/{@code valorNuevo} porque pueden contener datos clínicos
 * completos), los campos de este documento ({@code ip}, {@code ruta}, {@code userAgent}...)
 * no se cifran a nivel de campo. Ninguno contiene datos de salud, y cifrarlos impediría
 * filtrar/depurar accesos por IP o ruta sin una necesidad concreta que lo justifique hoy.
 * Su protección en reposo se apoya en el cifrado nativo del proveedor de MongoDB (p. ej.
 * MongoDB Atlas). Si en el futuro se requiere protección adicional, revisar de nuevo esta
 * decisión.</p>
 *
 * <p><b>Excepción — NIF en la ruta:</b> algunas rutas incluyen el NIF del paciente como
 * variable de path (p. ej. {@code /pacientes/{nif}/historial}), que sí es un dato personal
 * identificativo cifrado en el resto de la aplicación ({@code Usuario.nif}). Por eso
 * {@link com.hcc.tfm_hcc.config.AccessLogFilter} enmascara cualquier NIF/NIE detectado en
 * {@code ruta} antes de guardarla (se conservan los últimos caracteres, igual que en el
 * resto de logs de la aplicación, para poder correlacionar accesos del mismo paciente).</p>
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Document(collection = "log_acceso")
@CompoundIndex(name = "idx_log_acceso_usuario_timestamp", def = "{'id_usuario': 1, 'timestamp': -1}")
@CompoundIndex(name = "idx_log_acceso_timestamp", def = "{'timestamp': -1}")
@Data
@NoArgsConstructor
public class AccessLog {

    /**
     * Identificador único del registro de acceso.
     */
    @Id
    private String id;

    /**
     * Fecha y hora exacta cuando se realizó el acceso.
     */
    @Field("timestamp")
    private LocalDateTime timestamp;

    /**
     * ID del usuario que realizó el acceso.
     * Almacena el identificador único del usuario para trazabilidad.
     */
    @Field("id_usuario")
    private String usuarioId;

    /**
     * Método HTTP utilizado en la solicitud (GET, POST, PUT, DELETE, etc.).
     */
    @Field("metodo")
    private String metodo;

    /**
     * Ruta o endpoint accedido en la solicitud.
     */
    @Field("ruta")
    private String ruta;

    /**
     * Código de estado HTTP de la respuesta (200, 404, 500, etc.).
     */
    @Field("estado")
    private Integer estado;

    /**
     * Dirección IP desde la cual se realizó la solicitud.
     */
    @Field("ip")
    private String ip;

    /**
     * User-Agent del navegador o cliente que realizó la solicitud.
     */
    @Field("user_agent")
    private String userAgent;

    /**
     * Duración en milisegundos que tomó procesar la solicitud.
     */
    @Field("duracion_ms")
    private Long duracionMs;

    /**
     * Obtiene el ID único del registro de acceso.
     * 
     * @return UUID del registro de acceso
     */
    public String getId() { 
        return id; 
    }
}
