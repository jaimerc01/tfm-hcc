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
 * Entidad que representa una anotación médica en el sistema HCC.
 * Permite a los profesionales médicos registrar observaciones, notas
 * y comentarios específicos sobre sus pacientes asignados.
 * 
 * <p>Esta entidad maneja:</p>
 * <ul>
 *   <li>Relación entre médico y paciente específicos</li>
 *   <li>Contenido textual de la anotación médica</li>
 *   <li>Hereda campos de auditoría de BaseEntity</li>
 *   <li>Trazabilidad temporal de las anotaciones</li>
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
@Table(name = "anotacion_medica")
public class AnotacionMedica extends BaseEntity {

    private static final long serialVersionUID = 7L;

    /**
     * Médico profesional que realiza la anotación.
     * Establece quién es el autor de la observación médica.
     */
    @ManyToOne
    @JoinColumn(name = "id_profesional", nullable = false)
    private Usuario medico;
    
    /**
     * Paciente sobre el cual se realiza la anotación.
     * Identifica el sujeto de la observación médica.
     */
    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Usuario paciente;

    /**
     * Contenido textual de la anotación médica.
     * Incluye observaciones, recomendaciones o notas del profesional.
     */
    @Column(name = "mensaje", nullable = false)
    private String mensaje;
}
