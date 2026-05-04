package com.hcc.tfm_hcc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un archivo clínico en el sistema HCC.
 * Almacena documentos médicos, imágenes y otros archivos relacionados
 * con la información clínica de los usuarios del sistema.
 * 
 * <p>Esta entidad gestiona:</p>
 * <ul>
 *   <li>Metadatos del archivo (nombre, tipo, tamaño)</li>
 *   <li>Ubicación física del archivo en el sistema de almacenamiento</li>
 *   <li>Asociación con el usuario propietario del archivo</li>
 *   <li>Hereda campos de auditoría de BaseEntity</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "archivo_clinico")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class ArchivoClinico extends BaseEntity {

    /**
     * Usuario propietario del archivo clínico.
     * Establece la relación de propiedad y acceso al documento.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /**
     * Nombre original del archivo cuando fue subido por el usuario.
     * Conserva el nombre con el que el usuario identificó el documento.
     */
    @Column(name = "nombre_original", nullable = false)
    private String nombreOriginal;

    /**
     * Tipo MIME del archivo (ej: application/pdf, image/jpeg).
     * Permite identificar el formato y habilitar la visualización adecuada.
     */
    @Column(name = "content_type")
    private String contentType;

    /**
     * Tamaño del archivo en bytes.
     * Útil para control de almacenamiento y validaciones de tamaño.
     */
    @Column(name = "size_bytes")
    private Long sizeBytes;

    /**
     * Ruta o identificador donde se almacena físicamente el archivo.
     * Puede ser una ruta de sistema de archivos o un identificador de almacenamiento en la nube.
     */
    @Column(name = "ruta_almacenada", nullable = false)
    private String rutaAlmacenada;
}
