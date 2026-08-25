package com.hcc.tfm_hcc.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Documento que representa un archivo clínico en el sistema HCC.
 * Almacena en MongoDB tanto los metadatos como el contenido binario (cifrado)
 * de documentos médicos, imágenes y otros archivos relacionados con la
 * información clínica de los usuarios del sistema.
 *
 * <p>El nombre original se guarda cifrado con {@code FieldEncryptionService}
 * (mismo mecanismo que {@link AuditoriaCambio}, por tratarse de un documento
 * fuera de JPA donde no aplica {@code AESEncryptionConverter}) porque puede
 * revelar información clínica (p. ej. "analitica_vih.pdf"): el cifrado y
 * descifrado los gestiona {@code ArchivoClinicoServiceImpl} de forma
 * transparente, de modo que este campo contiene texto en claro mientras el
 * documento está en memoria. El contenido binario se cifra por separado con
 * AES/GCM mediante {@code ArchivoCifradoService}, igual que cuando se
 * almacenaba en disco.</p>
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Document(collection = "archivo_clinico")
@Data
@NoArgsConstructor
public class ArchivoClinico {

    /**
     * Identificador único del archivo clínico.
     */
    @Id
    private UUID id;

    /**
     * ID del usuario propietario del archivo clínico.
     * Establece la relación de propiedad y acceso al documento.
     */
    @Field("id_usuario")
    @Indexed
    private UUID usuarioId;

    /**
     * Nombre original del archivo cuando fue subido por el usuario.
     * Conserva el nombre con el que el usuario identificó el documento.
     * Campo cifrado por poder revelar información clínica (p. ej. "analitica_vih.pdf").
     */
    @Field("nombre_original")
    private String nombreOriginal;

    /**
     * Tipo MIME del archivo (ej: application/pdf, image/jpeg).
     * Permite identificar el formato y habilitar la visualización adecuada.
     */
    @Field("content_type")
    private String contentType;

    /**
     * Tamaño del archivo en bytes.
     * Útil para control de almacenamiento y validaciones de tamaño.
     */
    @Field("size_bytes")
    private Long sizeBytes;

    /**
     * Contenido binario del archivo, cifrado con AES/GCM (ver
     * {@code ArchivoCifradoService}) antes de guardarse en MongoDB.
     */
    @Field("contenido")
    private byte[] contenido;

    /**
     * Fecha y hora de creación del registro.
     */
    @Field("fecha_creacion")
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de la última modificación del registro.
     */
    @Field("fecha_ultima_modificacion")
    private LocalDateTime fechaUltimaModificacion;
}
