package com.hcc.tfm_hcc.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.hcc.tfm_hcc.model.Usuario;

@Repository
public interface UsuarioRepository  extends CrudRepository<Usuario, Long> {

    Optional<Usuario> findByNif(String nif);

}
