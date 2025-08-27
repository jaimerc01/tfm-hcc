package com.hcc.tfm_hcc.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hcc.tfm_hcc.model.HistorialClinico;
import com.hcc.tfm_hcc.model.Usuario;

public interface HistorialClinicoRepository extends JpaRepository<HistorialClinico, UUID> {
    Optional<HistorialClinico> findByUsuario(Usuario usuario);
}
