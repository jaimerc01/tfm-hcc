package com.hcc.tfm_hcc.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hcc.tfm_hcc.model.AntecedenteClinico;
import com.hcc.tfm_hcc.model.HistorialClinico;

/**
 * Repositorio para la gestión de antecedentes clínicos (personales y familiares)
 * en el sistema HCC.
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface AntecedenteClinicoRepository extends JpaRepository<AntecedenteClinico, UUID> {

    /**
     * Busca todos los antecedentes clínicos asociados a un historial clínico específico.
     *
     * @param historial HistorialClinico del cual obtener los antecedentes
     * @return Lista de AntecedenteClinico asociados al historial especificado
     */
    List<AntecedenteClinico> findByHistorialClinico(HistorialClinico historial);
}
