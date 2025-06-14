package com.hcc.tfm_hcc.service;

import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.exception.PerfilNotFoundException;
import com.hcc.tfm_hcc.model.Perfil;
import com.hcc.tfm_hcc.repository.PerfilRepository;

@Service
public class PerfilServiceImpl implements PerfilService {

    private final PerfilRepository perfilRepository;

    public PerfilServiceImpl(PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
    }

    @Override
    public Perfil getPerfilByRol(String rol) {
        return perfilRepository.getPerfilByRol(rol)
            .orElseThrow(() -> new PerfilNotFoundException(rol));
    }
}