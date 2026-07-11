package com.hcc.tfm_hcc.service.impl;

import com.hcc.tfm_hcc.exception.RangoOperacionException;
import com.hcc.tfm_hcc.model.Rango;
import com.hcc.tfm_hcc.repository.RangoRepository;
import com.hcc.tfm_hcc.service.RangoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RangoServiceImpl implements RangoService {

    private final RangoRepository rangoRepository;

    @Override
    public List<Rango> listarTodos() {
        try {
            return rangoRepository.findAll();
        } catch (Exception e) {
            log.error("Error al listar rangos: {}", e.getMessage(), e);
            throw new RangoOperacionException("Error al listar rangos", e);
        }
    }
}
