package com.hcc.tfm_hcc.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.UsuarioRepository;

public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioMapper usuarioMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public String altaUsuario(UsuarioDTO usuarioDTO) {
        
        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);
        usuario.setFechaCreacion(new Date());

        this.usuarioRepository.save(usuario);

        return "Usuario creado con éxito";
    }
    
}
