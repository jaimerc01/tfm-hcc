package com.hcc.tfm_hcc.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hcc.tfm_hcc.model.DatoClinico;
import com.hcc.tfm_hcc.model.HistorialClinico;

public interface DatoClinicoRepository extends JpaRepository<DatoClinico, UUID> {
    List<DatoClinico> findByHistorialClinico(HistorialClinico historial);
    void deleteByHistorialClinicoAndTipo(HistorialClinico historial, String tipo);
}
