package com.hcc.tfm_hcc.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hcc.tfm_hcc.model.GoogleLoginCode;

/**
 * Repositorio para los códigos de un solo uso del login con Google.
 *
 * <p>La creación del código pasa por este repositorio ({@code save}); el canjeo
 * (búsqueda + borrado atómico en una sola operación, para garantizar que un
 * código nunca puede usarse dos veces) se hace directamente con
 * {@code MongoTemplate#findAndRemove} desde {@code AutenticacionServiceImpl},
 * ya que Spring Data no ofrece un método derivado atómico equivalente.</p>
 */
public interface GoogleLoginCodeRepository extends MongoRepository<GoogleLoginCode, String> {
}
