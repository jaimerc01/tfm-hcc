package com.hcc.tfm_hcc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "dato_clinico")
@Data
public class DatoClinico extends BaseEntity{

    private static final long serialVersionUID = 6L;

    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(name = "valor", nullable = false)
    private float valor;

    @Column(name = "unidad", nullable = false)
    private String unidad;

    @Column(name = "observacion")
    private String observacion;


    @ManyToOne
    @JoinColumn(name = "id_historial_clinico", nullable = false)
    private HistorialClinico historialClinico;
    
}
