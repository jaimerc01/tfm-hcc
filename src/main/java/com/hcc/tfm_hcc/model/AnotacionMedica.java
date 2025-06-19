package com.hcc.tfm_hcc.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@Entity
@Table(name = "anotacion_medica")
public class AnotacionMedica extends BaseEntity {

    private static final long serialVersionUID = 7L;

    @ManyToOne
    @JoinColumn(name = "id_historial_clinico", nullable = false)
    private HistorialClinico historialClinico;
    
}
