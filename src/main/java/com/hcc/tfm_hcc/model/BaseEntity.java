package com.hcc.tfm_hcc.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.proxy.HibernateProxy;
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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidad base abstracta para todas las entidades JPA del sistema HCC.
 * Proporciona campos comunes de auditoría e identificación que son
 * heredados por todas las entidades del modelo de datos.
 *
 * <p>Esta clase base incluye:</p>
 * <ul>
 *   <li>ID único (UUID) generado automáticamente</li>
 *   <li>Fecha de creación del registro</li>
 *   <li>Fecha de última modificación para auditoría</li>
 *   <li>{@code equals}/{@code hashCode} basados <b>solo en el identificador</b> (ver abajo)</li>
 * </ul>
 *
 * <p><b>Identidad:</b> la igualdad se define únicamente por {@link #id}, no por el resto
 * de campos. Comparar entidades por todos sus atributos (lo que hacía Lombok con
 * {@code @Data}) rompe su uso en {@code HashSet}/{@code HashMap} —el {@code hashCode}
 * cambiaba al mutar cualquier campo, incluidas las {@code authorities} transitorias de
 * {@code Usuario}— y no refleja la semántica de JPA, donde dos instancias de la misma
 * fila son la misma entidad. El {@code hashCode} es constante por tipo para que una
 * entidad recién creada (con {@code id} aún nulo) siga funcionando en colecciones hasta
 * que se persista.</p>
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@ToString
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 15L;
    
    /**
     * Identificador único de la entidad (UUID).
     *
     * <p>Se genera con {@link GenerationType#UUID} (JPA 3.1): Hibernate asigna un UUID v4
     * en la propia aplicación antes de insertar, sin depender de que la columna de base de
     * datos tenga un {@code DEFAULT gen_random_uuid()}. {@code GenerationType.IDENTITY}, que
     * se usaba antes, está pensado para columnas autoincrementales y no encaja con un
     * identificador UUID.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    /**
     * Dos entidades son iguales si son del mismo tipo efectivo (resolviendo proxies de
     * Hibernate) y comparten un {@code id} no nulo. Dos entidades sin persistir
     * ({@code id == null}) solo son iguales si son la misma instancia.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Class<?> tipoEste = tipoEfectivo(this);
        Class<?> tipoOtro = tipoEfectivo(o);
        if (tipoEste != tipoOtro) {
            return false;
        }
        BaseEntity otra = (BaseEntity) o;
        return id != null && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        // Constante por tipo: estable aunque el id se asigne después de construir la entidad.
        return tipoEfectivo(this).hashCode();
    }

    private static Class<?> tipoEfectivo(Object entidad) {
        return entidad instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : entidad.getClass();
    }
}
