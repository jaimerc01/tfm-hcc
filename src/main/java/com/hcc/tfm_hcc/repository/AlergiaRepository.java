package com.hcc.tfm_hcc.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hcc.tfm_hcc.model.Alergia;
import com.hcc.tfm_hcc.model.HistorialClinico;

/**
 * Repositorio para la gestión de alergias e intolerancias en el sistema HCC.
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface AlergiaRepository extends JpaRepository<Alergia, UUID> {

    /**
     * Busca todas las alergias asociadas a un historial clínico específico.
     *
     * @param historial HistorialClinico del cual obtener las alergias
     * @return Lista de Alergia asociadas al historial especificado
     */
    List<Alergia> findByHistorialClinico(HistorialClinico historial);
}
