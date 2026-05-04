package com.hcc.tfm_hcc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una solicitud de asignación médico-paciente en el sistema HCC.
 * Gestiona las peticiones de vinculación entre profesionales médicos y pacientes,
 * incluyendo el flujo de aprobación y estados de la solicitud.
 * 
 * <p>Esta entidad gestiona:</p>
 * <ul>
 *   <li>Solicitudes enviadas por médicos a pacientes</li>
 *   <li>Estados de la solicitud (PENDIENTE, ACEPTADA, RECHAZADA)</li>
 *   <li>Trazabilidad del proceso de asignación</li>
 *   <li>Hereda campos de auditoría de BaseEntity</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Data
@Table(name = "solicitud_asignacion")
public class SolicitudAsignacion extends BaseEntity {

    private static final long serialVersionUID = 4L;

    /**
     * Paciente que recibe la solicitud de asignación.
     * Usuario con perfil paciente al que se solicita acceso a su información médica.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_paciente", referencedColumnName = "id", nullable = false)
    private Usuario paciente;

    /**
     * Médico que envía la solicitud de asignación.
     * Usuario con perfil médico que solicita acceso a la información del paciente.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_medico", referencedColumnName = "id", nullable = false)
    private Usuario medico;

    /**
     * Estado actual de la solicitud de asignación.
     * Valores posibles: "PENDIENTE", "ACEPTADA", "RECHAZADA", "REVOCADA".
     */
    @Column(name = "estado", nullable = false)
    private String estado;
}
