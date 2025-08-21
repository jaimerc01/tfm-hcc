package com.hcc.tfm_hcc.service.impl;

import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.PerfilUsuario;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.PerfilRepository;
import com.hcc.tfm_hcc.repository.PerfilUsuarioRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.MedicoService;
import com.hcc.tfm_hcc.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class MedicoServiceImpl implements MedicoService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private UsuarioMapper usuarioMapper;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private PerfilRepository perfilRepository;
    @Autowired
    private PerfilUsuarioRepository perfilUsuarioRepository;

    @Override
    public List<UsuarioDTO> listarMedicos() {
        return StreamSupport.stream(usuarioRepository.findAll().spliterator(), false)
            .filter(u -> perfilUsuarioRepository.getPerfilesByNif(u.getNif())
                .stream().anyMatch(p -> p.getRol().equalsIgnoreCase("MEDICO")))
            .map(usuarioMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public UsuarioDTO crearMedico(UsuarioDTO medicoDTO) {
        var perfilOpt = perfilRepository.getPerfilByRol("MEDICO");
        if (perfilOpt.isEmpty()) {
            throw new IllegalStateException("Perfil MEDICO no encontrado");
        }
        medicoDTO.setEstadoCuenta("ACTIVO"); // Forzar estado activo
        Usuario usuario = usuarioService.altaUsuario(medicoDTO);
        PerfilUsuario pu = new PerfilUsuario();
        pu.setPerfil(perfilOpt.get());
        pu.setUsuario(usuario);
        pu.setFechaCreacion(java.time.LocalDateTime.now());
        perfilUsuarioRepository.save(pu);
        return usuarioMapper.toDto(usuario);
    }

    @Override
    public UsuarioDTO actualizarMedico(UUID id, UsuarioDTO medicoDTO) {
        var usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) throw new IllegalArgumentException("No existe el médico");
        Usuario usuario = usuarioOpt.get();
        if (medicoDTO.getNombre() != null) usuario.setNombre(medicoDTO.getNombre());
        if (medicoDTO.getApellido1() != null) usuario.setApellido1(medicoDTO.getApellido1());
        if (medicoDTO.getApellido2() != null) usuario.setApellido2(medicoDTO.getApellido2());
        if (medicoDTO.getEmail() != null) usuario.setEmail(medicoDTO.getEmail());
        if (medicoDTO.getTelefono() != null) usuario.setTelefono(medicoDTO.getTelefono());
        if (medicoDTO.getEspecialidad() != null) usuario.setEspecialidad(medicoDTO.getEspecialidad());
        usuario.setFechaUltimaModificacion(java.time.LocalDateTime.now());
        usuarioRepository.save(usuario);
        return usuarioMapper.toDto(usuario);
    }

    @Override
    public void eliminarMedico(UUID id) {
        var usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) throw new IllegalArgumentException("No existe el médico");
        Usuario usuario = usuarioOpt.get();
        var relaciones = StreamSupport.stream(perfilUsuarioRepository.findAll().spliterator(), false)
            .filter(pu -> pu.getUsuario().getId().equals(id) && pu.getPerfil().getRol().equalsIgnoreCase("MEDICO"))
            .collect(Collectors.toList());
        perfilUsuarioRepository.deleteAll(relaciones);
        usuarioRepository.delete(usuario);
    }
}
