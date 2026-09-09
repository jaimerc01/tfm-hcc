package com.hcc.tfm_hcc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una notificación en el sistema HCC.
 * Gestiona los mensajes y alertas dirigidos a usuarios específicos,
 * permitiendo la comunicación interna del sistema.
 * 
 * <p>Esta entidad gestiona:</p>
 * <ul>
 *   <li>Contenido textual de la notificación</li>
 *   <li>Estado de lectura para control de notificaciones pendientes</li>
 *   <li>Asociación con el usuario destinatario</li>
 *   <li>Hereda campos de auditoría de BaseEntity</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "notificacion")
public class Notificacion extends BaseEntity {
    
    private static final long serialVersionUID = 5L;

    /**
     * Contenido textual de la notificación.
     * Mensaje que será mostrado al usuario destinatario.
     */
    @Column(name = "mensaje", nullable = false)
    private String mensaje;

    /**
     * Indica si la notificación ha sido leída por el usuario.
     * Permite identificar notificaciones pendientes y mostrar indicadores visuales.
     */
    @Column(name = "leida", nullable = false)
    private boolean leida;

    /**
     * Indica si el usuario ha eliminado la notificación (borrado lógico). Las notificaciones
     * eliminadas se conservan en base de datos pero no se devuelven en los listados ni se
     * cuentan como no leídas.
     */
    @Column(name = "eliminada", nullable = false)
    private boolean eliminada;

    /**
     * Usuario destinatario de la notificación.
     * Establece a quién está dirigido el mensaje.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id", nullable = false)
    private Usuario usuario;
}
