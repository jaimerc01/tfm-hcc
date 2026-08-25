package com.hcc.tfm_hcc.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

/**
 * Entidad base abstracta para todas las entidades del sistema HCC.
 * Proporciona campos comunes de auditoría y identificación que son
 * heredados por todas las entidades del modelo de datos.
 * 
 * <p>Esta clase base incluye:</p>
 * <ul>
 *   <li>ID único generado automáticamente (UUID)</li>
 *   <li>Fecha de creación del registro</li>
 *   <li>Fecha de última modificación para auditoría</li>
 *   <li>Implementación de Serializable para persistencia</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 15L;
    
    /**
     * Identificador único de la entidad.
     * Se genera automáticamente utilizando estrategia IDENTITY.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;    
    
    /**
     * Fecha y hora de creación del registro.
     *
     * <p>Se establece manualmente en el servicio correspondiente (no vía
     * {@code @CreatedDate}) porque en al menos una entidad, {@link DatoClinico},
     * este campo no representa "cuándo se insertó la fila" sino la fecha real del
     * dato clínico (p. ej. la fecha de un análisis de laboratorio pasado indicada
     * por el usuario), que la auditoría automática de JPA sobrescribiría siempre
     * con la fecha actual al persistir.</p>
     */
    @Column(name = "fecha_creacion", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de la última modificación del registro.
     * Se actualiza automáticamente cada vez que se crea o modifica la entidad,
     * mediante la auditoría de JPA (ver {@code JpaAuditingConfig}).
     */
    @LastModifiedDate
    @Column(name = "fecha_ultima_modificacion")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime fechaUltimaModificacion;
}
