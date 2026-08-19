package com.hcc.tfm_hcc.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException;
import com.hcc.tfm_hcc.model.Perfil;
import com.hcc.tfm_hcc.model.PerfilUsuario;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.PerfilUsuarioRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.PerfilService;
import com.hcc.tfm_hcc.service.PerfilUsuarioService;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio de relaciones usuario-perfil.
 */
@Service
@RequiredArgsConstructor
public class PerfilUsuarioServiceImpl implements PerfilUsuarioService {

    private static final String CAMPO_ID_USUARIO = "ID del usuario";
    private static final String ZONE_ID_EUROPA_MADRID = "Europe/Madrid";

    private final PerfilService perfilService;
    private final UsuarioRepository usuarioRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;

    @Override
    @Transactional
    public void asignarPerfil(UUID usuarioId, String rol) {
        Usuario usuario = obtenerUsuario(usuarioId);
        Perfil perfil = obtenerPerfil(rol);

        if (perfilUsuarioRepository.existsByUsuarioIdAndPerfilRol(usuarioId, rol)) {
            return;
        }

        PerfilUsuario perfilUsuario = new PerfilUsuario();
        perfilUsuario.setPerfil(perfil);
        perfilUsuario.setUsuario(usuario);
        perfilUsuario.setFechaCreacion(LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPA_MADRID)));
        perfilUsuarioRepository.save(perfilUsuario);
    }

    @Override
    @Transactional
    public void revocarPerfil(UUID usuarioId, String rol) {
        if (usuarioId == null) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido(CAMPO_ID_USUARIO));
        }
        obtenerPerfil(rol);

        perfilUsuarioRepository.findByUsuarioIdAndPerfilRol(usuarioId, rol)
            .ifPresent(perfilUsuarioRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tienePerfil(UUID usuarioId, String rol) {
        if (usuarioId == null) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido(CAMPO_ID_USUARIO));
        }
        obtenerPerfil(rol);

        return perfilUsuarioRepository.existsByUsuarioIdAndPerfilRol(usuarioId, rol);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarUsuariosPorRol(String rol) {
        obtenerPerfil(rol);
        return perfilUsuarioRepository.findUsuariosByPerfilRol(rol);
    }

    private Usuario obtenerUsuario(UUID usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException(ErrorMessages.campoRequerido(CAMPO_ID_USUARIO));
        }
        return usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new UsuarioNoEncontradoException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO));
    }

    private Perfil obtenerPerfil(String rol) {
        return perfilService.getPerfilByRol(rol);
    }
}