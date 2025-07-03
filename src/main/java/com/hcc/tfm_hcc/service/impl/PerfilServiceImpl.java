package com.hcc.tfm_hcc.service.impl;

import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.exception.PerfilNotFoundException;
import com.hcc.tfm_hcc.model.Perfil;
import com.hcc.tfm_hcc.repository.PerfilRepository;
import com.hcc.tfm_hcc.service.PerfilService;

@Service
public class PerfilServiceImpl implements PerfilService {

    private final PerfilRepository perfilRepository;

    public PerfilServiceImpl(PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
    }

    @Override
    public Perfil getPerfilByRol(String rol) throws PerfilNotFoundException {
        return perfilRepository.getPerfilByRol(rol)
            .orElseThrow(() -> new PerfilNotFoundException(rol));
    }
}