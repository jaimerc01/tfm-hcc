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
 * Entidad que representa el historial clínico de un paciente en el sistema HCC.
 * Contiene la información médica central del paciente, incluyendo antecedentes
 * familiares y otros datos clínicos relevantes para su atención médica.
 * 
 * <p>Esta entidad gestiona:</p>
 * <ul>
 *   <li>Relación uno-a-uno con el usuario/paciente</li>
 *   <li>Antecedentes familiares y heredofamiliares</li>
 *   <li>Base para asociar datos clínicos específicos</li>
 *   <li>Hereda campos de auditoría de BaseEntity</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "historial_clinico")
public class HistorialClinico extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Usuario/paciente propietario del historial clínico.
     * Establece la relación uno-a-uno entre usuario e historial médico.
     */
    @ManyToOne
    @JoinColumn(name = "id_paciente", referencedColumnName = "id", nullable = false)
    private Usuario usuario;

    /**
     * Antecedentes familiares del paciente.
     * Información médica heredofamiliar relevante para el diagnóstico y tratamiento.
     */
    @Column(name = "antecedentes_familiares")
    private String antecedentesFamiliares;
}
