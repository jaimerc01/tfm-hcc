package com.hcc.tfm_hcc.controller;

import com.hcc.tfm_hcc.dto.RangoDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Interfaz REST para gestionar los rangos de referencia médicos.
 *
 * <p>Define los contratos de los endpoints expuestos por el controlador de rangos.</p>
 */
public interface RangoController {

    /**
     * Obtiene todos los rangos de referencia disponibles en el sistema.
     *
     * @return ResponseEntity con lista de RangoDTO
     */
    ResponseEntity<List<RangoDTO>> obtenerTodosLosRangos();
}
