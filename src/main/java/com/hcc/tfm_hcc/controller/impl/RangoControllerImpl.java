package com.hcc.tfm_hcc.controller.impl;

import com.hcc.tfm_hcc.constants.RestUrls;
import com.hcc.tfm_hcc.controller.RangoController;
import com.hcc.tfm_hcc.dto.RangoDTO;
import com.hcc.tfm_hcc.facade.RangoFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementación del controlador REST para gestionar los rangos de referencia médicos.
 */
@RestController
@RequestMapping(RestUrls.RANGOS_BASE)
@RequiredArgsConstructor
public class RangoControllerImpl implements RangoController {

    private final RangoFacade rangoFacade;

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping
    public ResponseEntity<List<RangoDTO>> obtenerTodosLosRangos() {
        return ResponseEntity.ok(rangoFacade.obtenerTodosLosRangos());
    }
}
