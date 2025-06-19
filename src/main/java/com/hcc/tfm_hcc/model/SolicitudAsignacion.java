package com.hcc.tfm_hcc.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "solicitud_asignacion")
public class SolicitudAsignacion extends BaseEntity {

    private static final long serialVersionUID = 4L;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_paciente", referencedColumnName = "id", nullable = false)
    private Usuario paciente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_medico", referencedColumnName = "id", nullable = false)
    private Usuario medico;
}
