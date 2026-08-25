package com.hcc.tfm_hcc.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hcc.tfm_hcc.model.TwoFactorChallenge;

/**
 * Repositorio para los retos pendientes de segundo factor (TOTP) durante el login.
 */
public interface TwoFactorChallengeRepository extends MongoRepository<TwoFactorChallenge, String> {
}
