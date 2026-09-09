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
 * Entidad que representa la relación médico-paciente en el sistema HCC.
 * Gestiona las asignaciones activas entre profesionales médicos y sus
 * pacientes asignados, incluyendo el estado de la relación.
 * 
 * <p>Esta entidad gestiona:</p>
 * <ul>
 *   <li>Relación muchos-a-muchos entre médicos y pacientes</li>
 *   <li>Estado de la asignación (ACTIVA, REVOCADA, etc.)</li>
 *   <li>Hereda campos de auditoría de BaseEntity</li>
 *   <li>Control de acceso basado en asignaciones</li>
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
@Table(name = "medico_paciente")
public class MedicoPaciente extends BaseEntity {

    /**
     * Médico profesional asignado.
     * Usuario con perfil médico que tendrá acceso a la información del paciente.
     */
    @ManyToOne
    @JoinColumn(name = "id_medico", referencedColumnName = "id", updatable = false)
    private Usuario medico;

    /**
     * Paciente asignado al médico.
     * Usuario con perfil paciente cuya información será accesible al médico.
     */
    @ManyToOne
    @JoinColumn(name = "id_paciente", referencedColumnName = "id", updatable = false)
    private Usuario paciente;

    /** Relación vigente: el médico tiene acceso al historial del paciente. */
    public static final String ESTADO_ACTIVA = "ACTIVA";

    /** Relación finalizada (por el paciente, por baja del médico o por retirada del perfil). */
    public static final String ESTADO_REVOCADA = "REVOCADA";

    /**
     * Estado de la relación médico-paciente.
     * Valores posibles: {@link #ESTADO_ACTIVA}, {@link #ESTADO_REVOCADA}.
     */
    @Column(name = "estado", nullable = false)
    private String estado;
}