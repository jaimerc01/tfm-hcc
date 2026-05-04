package com.hcc.tfm_hcc.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa la asociación entre usuarios y perfiles en el sistema HCC.
 * Implementa la relación muchos-a-muchos que permite a los usuarios tener
 * múltiples roles y a los roles ser asignados a múltiples usuarios.
 * 
 * <p>Esta entidad gestiona:</p>
 * <ul>
 *   <li>Asociación dinámica usuario-perfil</li>
 *   <li>Control granular de permisos por usuario</li>
 *   <li>Auditoría de asignaciones de roles</li>
 *   <li>Hereda campos de auditoría de BaseEntity</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Data
@Table(name = "perfil_usuario")
public class PerfilUsuario extends BaseEntity {

    /**
     * Perfil o rol asignado en esta asociación.
     * Define qué permisos tiene el usuario.
     */
    @ManyToOne
    @JoinColumn(name = "id_perfil", referencedColumnName = "id", updatable = false)
    private Perfil perfil;

    /**
     * Usuario al que se le asigna el perfil.
     * Identifica quién recibe los permisos del rol.
     */
    @ManyToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id", updatable = false)
    private Usuario usuario;
}
