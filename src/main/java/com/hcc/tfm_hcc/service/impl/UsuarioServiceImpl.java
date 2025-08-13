package com.hcc.tfm_hcc.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.model.PerfilUsuario;
import com.hcc.tfm_hcc.repository.PerfilRepository;
import com.hcc.tfm_hcc.repository.PerfilUsuarioRepository;
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

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private PerfilUsuarioRepository perfilUsuarioRepository;

    // No inyectar AutenticacionService aquí para evitar dependencia cíclica

    @Override
    @Transactional
    public Usuario altaUsuario(UsuarioDTO usuarioDTO) {

        usuarioDTO.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        
        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);
        usuario.setFechaCreacion(LocalDateTime.now());

        this.usuarioRepository.save(usuario);

        // Buscar perfil PACIENTE y crear relación obligatoria
        var perfilOpt = perfilRepository.getPerfilByRol("PACIENTE");
        if (perfilOpt.isEmpty()) {
            // Forzar rollback si el perfil requerido no existe
            throw new IllegalStateException("Perfil PACIENTE no encontrado; se cancela el alta de usuario");
        }
        try {
            PerfilUsuario pu = new PerfilUsuario();
            pu.setPerfil(perfilOpt.get());
            pu.setUsuario(usuario);
            pu.setFechaCreacion(LocalDateTime.now());
            perfilUsuarioRepository.save(pu);
        } catch (Exception e) {
            // Re-lanzar para provocar rollback de la transacción
            throw new RuntimeException("Error creando relación PerfilUsuario (PACIENTE)", e);
        }

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
