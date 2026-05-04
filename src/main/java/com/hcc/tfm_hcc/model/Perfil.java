package com.hcc.tfm_hcc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un perfil o rol de usuario en el sistema HCC.
 * Define los diferentes tipos de usuarios y sus permisos asociados
 * dentro del sistema de gestión clínica.
 * 
 * <p>Esta entidad gestiona:</p>
 * <ul>
 *   <li>Definición de roles del sistema (MEDICO, PACIENTE, ADMIN, etc.)</li>
 *   <li>Control de acceso basado en roles (RBAC)</li>
 *   <li>Unicidad de nombres de rol</li>
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
@Table(name = "perfil")
public class Perfil extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Nombre del rol o perfil de usuario.
     * Debe ser único en el sistema y define los permisos del usuario.
     * Ejemplos: "MEDICO", "PACIENTE", "ADMINISTRADOR", etc.
     */
    @Column(name = "rol", nullable = false, unique = true)
    private String rol;
}
