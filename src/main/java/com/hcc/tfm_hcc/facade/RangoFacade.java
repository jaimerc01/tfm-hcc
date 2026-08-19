package com.hcc.tfm_hcc.facade;

import com.hcc.tfm_hcc.dto.RangoDTO;

import java.util.List;

/**
 * Fachada para la consulta de rangos de referencia médicos.
 */
public interface RangoFacade {

    /**
     * Obtiene todos los rangos de referencia disponibles en el sistema.
     *
     * @return lista no modificable de RangoDTO
     */
    List<RangoDTO> obtenerTodosLosRangos();
}