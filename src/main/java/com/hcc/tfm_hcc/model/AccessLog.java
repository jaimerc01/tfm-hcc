package com.hcc.tfm_hcc.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Entity
@Data
@Table(name = "log_acceso")
@NoArgsConstructor
public class AccessLog {

    /**
     * Identificador único del registro de acceso.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Fecha y hora exacta cuando se realizó el acceso.
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * ID del usuario que realizó el acceso.
     * Almacena el identificador único del usuario para trazabilidad.
     */
    @Column(name = "id_usuario")
    private String usuarioId;

    /**
     * Método HTTP utilizado en la solicitud (GET, POST, PUT, DELETE, etc.).
     */
    @Column(name = "metodo", length = 10)
    private String metodo;

    /**
     * Ruta o endpoint accedido en la solicitud.
     */
    @Column(name = "ruta", length = 500)
    private String ruta;

    /**
     * Código de estado HTTP de la respuesta (200, 404, 500, etc.).
     */
    @Column(name = "estado")
    private Integer estado;

    /**
     * Dirección IP desde la cual se realizó la solicitud.
     */
    @Column(name = "ip", length = 50)
    private String ip;

    /**
     * User-Agent del navegador o cliente que realizó la solicitud.
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Duración en milisegundos que tomó procesar la solicitud.
     */
    @Column(name = "duracion_ms")
    private Long duracionMs;

    /**
     * Obtiene el ID único del registro de acceso.
     * 
     * @return UUID del registro de acceso
     */
    public UUID getId() { 
        return id; 
    }
}
