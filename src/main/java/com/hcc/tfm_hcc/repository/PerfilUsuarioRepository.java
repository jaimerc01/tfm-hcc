package com.hcc.tfm_hcc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hcc.tfm_hcc.model.Perfil;
import com.hcc.tfm_hcc.model.PerfilUsuario;

@Repository
public interface PerfilUsuarioRepository extends CrudRepository<PerfilUsuario, Long> {

    @Query("SELECT p.perfil FROM PerfilUsuario p WHERE p.usuario.nif = :nif")
    List<Perfil> getPerfilesByNif(@Param("nif") String nif);
}
