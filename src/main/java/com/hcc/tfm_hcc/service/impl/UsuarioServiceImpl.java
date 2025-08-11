package com.hcc.tfm_hcc.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.UsuarioService;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioMapper usuarioMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // No inyectar AutenticacionService aquí para evitar dependencia cíclica

    @Override
    public Usuario altaUsuario(UsuarioDTO usuarioDTO) {

        usuarioDTO.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        
        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);
        usuario.setFechaCreacion(LocalDateTime.now());

        this.usuarioRepository.save(usuario);

        return usuario;
    }

    public String getNifUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return null;
        }
        return auth.getName();
    }

    @Override
    public String getNombreUsuario() {
        String nif = this.getNifUsuarioAutenticado();
        return usuarioRepository.findByNif(nif)
                .map(Usuario::getNombre)
                .orElse(null);
    }

}
