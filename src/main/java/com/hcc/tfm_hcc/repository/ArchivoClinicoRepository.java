package com.hcc.tfm_hcc.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hcc.tfm_hcc.model.ArchivoClinico;

@Repository
public interface ArchivoClinicoRepository extends JpaRepository<ArchivoClinico, UUID> {
    List<ArchivoClinico> findByUsuarioIdOrderByFechaCreacionDesc(UUID usuarioId);
    Optional<ArchivoClinico> findByIdAndUsuarioId(UUID id, UUID usuarioId);
}
