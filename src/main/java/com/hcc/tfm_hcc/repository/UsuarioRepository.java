package com.hcc.tfm_hcc.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import java.util.UUID;
import org.springframework.stereotype.Repository;

import com.hcc.tfm_hcc.model.Usuario;

@Repository
public interface UsuarioRepository  extends CrudRepository<Usuario, UUID> {

    Optional<Usuario> findByNif(String nif);

    Optional<Usuario> findByEmail(String email);

    boolean existsByNifAndIdNot(String nif, UUID id);
    boolean existsByEmailAndIdNot(String email, UUID id);

}
