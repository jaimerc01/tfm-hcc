package com.hcc.tfm_hcc.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@Data
@Entity
@Table(name = "medico_paciente")
public class MedicoPaciente extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "id_medico", referencedColumnName = "id", updatable = false)
    private Usuario medico;

    @ManyToOne
    @JoinColumn(name = "id_paciente", referencedColumnName = "id", updatable = false)
    private Usuario paciente;

}