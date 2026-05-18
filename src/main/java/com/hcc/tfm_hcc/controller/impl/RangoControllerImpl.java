package com.hcc.tfm_hcc.controller.impl;

import com.hcc.tfm_hcc.constants.RestUrls;
import com.hcc.tfm_hcc.controller.RangoController;
import com.hcc.tfm_hcc.dto.RangoDTO;
import com.hcc.tfm_hcc.model.Rango;
import com.hcc.tfm_hcc.repository.RangoRepository;
import com.hcc.tfm_hcc.exception.RangoOperacionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementación del controlador REST para gestionar los rangos de referencia médicos.
 */
@Slf4j
@RestController
@RequestMapping(RestUrls.RANGOS_BASE)
@RequiredArgsConstructor
public class RangoControllerImpl implements RangoController {

    private final RangoRepository rangoRepository;

    @Override
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RangoDTO>> obtenerTodosLosRangos() {
        log.info("Solicitando todos los rangos de referencia");
        try {
            List<Rango> rangos = rangoRepository.findAll();
            List<RangoDTO> rangosDTO = rangos.stream()
                .map(this::convertirARangoDTO)
                .toList();

            log.info("Devolviendo {} rangos de referencia", rangosDTO.size());
            return ResponseEntity.ok(rangosDTO);
        } catch (Exception e) {
            log.error("Error al obtener rangos de referencia: {}", e.getMessage(), e);
            throw new RangoOperacionException("Error al obtener rangos de referencia", e);
        }
    }

    /**
     * Convierte una entidad Rango a RangoDTO
     *
     * @param rango Entidad Rango
     * @return RangoDTO con valores numéricos parseados
     */
    private RangoDTO convertirARangoDTO(Rango rango) {
        RangoDTO dto = new RangoDTO();
        dto.setId(rango.getId().toString());
        dto.setNombre(rango.getNombre());
        dto.setValorInferior(rango.getValorInferior());
        dto.setValorSuperior(rango.getValorSuperior());

        // Intentar parsear valores numéricos
        try {
            if (rango.getValorInferior() != null && !rango.getValorInferior().trim().isEmpty()) {
                // Extraer solo números y punto decimal
                String numStr = rango.getValorInferior().replaceAll("[^0-9.]", "");
                if (!numStr.isEmpty()) {
                    dto.setValorInferiorNumerico(Double.parseDouble(numStr));
                }
            }
        } catch (NumberFormatException _) {
            log.warn("No se pudo parsear valor inferior para {}: {}", rango.getNombre(), rango.getValorInferior());
        }

        try {
            if (rango.getValorSuperior() != null && !rango.getValorSuperior().trim().isEmpty()) {
                // Extraer solo números y punto decimal
                String numStr = rango.getValorSuperior().replaceAll("[^0-9.]", "");
                if (!numStr.isEmpty()) {
                    dto.setValorSuperiorNumerico(Double.parseDouble(numStr));
                }
            }
        } catch (NumberFormatException _) {
            log.warn("No se pudo parsear valor superior para {}: {}", rango.getNombre(), rango.getValorSuperior());
        }

        return dto;
    }
}
