package com.hcc.tfm_hcc.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
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
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Document(collection = "log_acceso")
@CompoundIndexes({
    @CompoundIndex(name = "idx_log_acceso_usuario_timestamp", def = "{'id_usuario': 1, 'timestamp': -1}"),
    @CompoundIndex(name = "idx_log_acceso_timestamp", def = "{'timestamp': -1}")
})
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
