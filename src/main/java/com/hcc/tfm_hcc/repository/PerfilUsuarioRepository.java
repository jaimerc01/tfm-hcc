package com.hcc.tfm_hcc.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.hcc.tfm_hcc.model.PerfilUsuario;

@Repository
public interface PerfilUsuarioRepository extends CrudRepository<PerfilUsuario, Long> {
}
