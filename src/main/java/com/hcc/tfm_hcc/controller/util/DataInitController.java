package com.hcc.tfm_hcc.controller.util;

import com.hcc.tfm_hcc.model.Rango;
import com.hcc.tfm_hcc.repository.RangoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Controlador temporal para inicializar datos de rangos de referencia.
 * SOLO PARA DESARROLLO - Remover en producción.
 */
@Slf4j
@RestController
@RequestMapping("/util")
@RequiredArgsConstructor
public class DataInitController {

    private final RangoRepository rangoRepository;

    @PostMapping("/init-rangos")
    public ResponseEntity<String> initRangos() {
        log.info("Inicializando rangos de referencia...");
        
        try {
            List<Rango> rangos = Arrays.asList(
                crearRango("Glucosa", "70", "140"),
                crearRango("Hemoglobina", "12.0", "16.0"),
                crearRango("Colesterol total", "100", "200"),
                crearRango("Triglicéridos", "50", "150"),
                crearRango("Creatinina", "0.6", "1.3"),
                crearRango("Hematocrito", "35", "50"),
                crearRango("Colesterol", "100", "200"),
                crearRango("GLUCOSA", "70", "140"),
                crearRango("HEMOGLOBINA", "12.0", "16.0"),
                crearRango("COLESTEROL TOTAL", "100", "200"),
                crearRango("TRIGLICÉRIDOS", "50", "150"),
                crearRango("CREATININA", "0.6", "1.3"),
                crearRango("HEMATOCRITO", "35", "50")
            );
            
            // Solo insertar si no existen
            for (Rango rango : rangos) {
                if (!rangoRepository.existsByNombreIgnoreCase(rango.getNombre())) {
                    rangoRepository.save(rango);
                    log.info("Rango creado: {}", rango.getNombre());
                }
            }
            
            long total = rangoRepository.count();
            log.info("Inicialización completada. Total rangos en BD: {}", total);
            
            return ResponseEntity.ok("Rangos inicializados correctamente. Total: " + total);
        } catch (Exception e) {
            log.error("Error inicializando rangos: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    
    private Rango crearRango(String nombre, String valorInferior, String valorSuperior) {
        Rango rango = new Rango();
        rango.setNombre(nombre);
        rango.setValorInferior(valorInferior);
        rango.setValorSuperior(valorSuperior);
        return rango;
    }
}
