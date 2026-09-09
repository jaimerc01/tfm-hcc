package com.hcc.tfm_hcc.model;

import com.hcc.tfm_hcc.converter.AESEncryptionConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un antecedente clínico (personal o familiar) del paciente.
 *
 * <p>Sustituye al almacenamiento previo de antecedentes como un único bloque de texto
 * libre en {@code HistorialClinico.antecedentesFamiliares}, permitiendo ahora gestionar
 * cada antecedente como una entrada independiente y categorizada.</p>
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "antecedente_clinico")
public class AntecedenteClinico extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Categoría del antecedente: personal o familiar.
     */
    public enum Categoria {
        PERSONAL,
        FAMILIAR
    }

    /**
     * Categoría del antecedente clínico.
     */
    @Column(name = "categoria", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    /**
     * Descripción del antecedente (p. ej. "Diabetes tipo 2 diagnosticada en 2015").
     * Campo encriptado por tratarse de un dato clínico del paciente.
     */
    @Column(name = "descripcion", nullable = false)
    @Convert(converter = AESEncryptionConverter.class)
    private String descripcion;

    /**
     * Historial clínico al que pertenece este antecedente.
     */
    @ManyToOne
    @JoinColumn(name = "id_historial_clinico", referencedColumnName = "id", nullable = false)
    private HistorialClinico historialClinico;
}
