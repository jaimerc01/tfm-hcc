package com.hcc.tfm_hcc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "archivo_clinico")
@NoArgsConstructor
public class ArchivoClinico extends BaseEntity {

    @Getter 
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Getter 
    @Setter
    @Column(name = "nombre_original", nullable = false)
    private String nombreOriginal;

    @Getter 
    @Setter
    @Column(name = "content_type")
    private String contentType;

    @Getter 
    @Setter
    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Getter 
    @Setter
    @Column(name = "ruta_almacenada", nullable = false)
    private String rutaAlmacenada;
}
