package com.hcc.tfm_hcc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "historial_clinico")
public class HistorialClinico extends BaseEntity{

    private static final long serialVersionUID = 1L;

    @Column(name = "antecedentes_familiares")
    private String antecedentesFamiliares;

    @ManyToOne
    @JoinColumn(name = "id_paciente", referencedColumnName = "id", nullable = false)
    private Usuario usuario;

}
