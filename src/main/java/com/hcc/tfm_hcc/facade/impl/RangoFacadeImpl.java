package com.hcc.tfm_hcc.facade.impl;

import com.hcc.tfm_hcc.dto.RangoDTO;
import com.hcc.tfm_hcc.exception.RangoOperacionException;
import com.hcc.tfm_hcc.facade.RangoFacade;
import com.hcc.tfm_hcc.model.Rango;
import com.hcc.tfm_hcc.converter.RangoConverter;
import com.hcc.tfm_hcc.service.RangoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de la fachada para consultar rangos de referencia médicos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RangoFacadeImpl implements RangoFacade {

    private final RangoService rangoService;
    private final RangoConverter rangoConverter;

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<RangoDTO> obtenerTodosLosRangos() {
        log.info("Solicitando todos los rangos de referencia");
        try {
            List<Rango> rangos = rangoService.listarTodos();
            List<RangoDTO> rangosDTO = rangos.stream()
                .map(rangoConverter::toDto)
                .toList();

            log.info("Devolviendo {} rangos de referencia", rangosDTO.size());
            return rangosDTO;
        } catch (Exception e) {
            log.error("Error al obtener rangos de referencia: {}", e.getMessage(), e);
            throw new RangoOperacionException("Error al obtener rangos de referencia", e);
        }
    }
}