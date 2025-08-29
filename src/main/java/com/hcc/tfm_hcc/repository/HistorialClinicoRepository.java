package com.hcc.tfm_hcc.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hcc.tfm_hcc.model.HistorialClinico;
import com.hcc.tfm_hcc.model.Usuario;

/**
 * Repositorio para la gestión de historiales clínicos en el sistema HCC.
 * Proporciona operaciones de persistencia y consulta para los historiales médicos
 * de los usuarios del sistema.
 * 
 * <p>Este repositorio permite:</p>
 * <ul>
 *   <li>Consultar el historial clínico de un usuario específico</li>
 *   <li>Mantener la relación uno-a-uno entre usuario e historial clínico</li>
 *   <li>Proporcionar acceso seguro a la información médica</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface HistorialClinicoRepository extends JpaRepository<HistorialClinico, UUID> {
    
    /**
     * Busca el historial clínico asociado a un usuario específico.
     * Cada usuario tiene un único historial clínico que contiene todos sus datos médicos.
     * 
     * @param usuario Usuario del cual obtener el historial clínico
     * @return Optional con el HistorialClinico del usuario si existe, empty() si no tiene historial
     */
    Optional<HistorialClinico> findByUsuario(Usuario usuario);
}
