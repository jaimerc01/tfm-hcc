package com.hcc.tfm_hcc.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hcc.tfm_hcc.model.Perfil;

@Repository
public interface PerfilRepository  extends CrudRepository<Perfil, Long> {

    @Query("SELECT p FROM Perfil p WHERE LOWER(p.rol) = LOWER(:rol)")
    Optional<Perfil> findByRol(@Param("rol") String rol);
    
}
