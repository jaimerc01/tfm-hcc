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
import com.hcc.tfm_hcc.dto.UserExportDTO;
import com.hcc.tfm_hcc.repository.PerfilRepository;
import com.hcc.tfm_hcc.repository.PerfilUsuarioRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.repository.AccessLogRepository;
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

    @Autowired
    private AccessLogRepository accessLogRepository;

    // No inyectar AutenticacionService aquí para evitar dependencia cíclica

    @Override
    @Transactional
    public Usuario altaUsuario(UsuarioDTO usuarioDTO) {

        usuarioDTO.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        
        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);
        usuario.setFechaCreacion(LocalDateTime.now());

    usuario.setLastPasswordChange(LocalDateTime.now());
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

    @Override
    public UsuarioDTO getUsuarioActual() {
        String nif = this.getNifUsuarioAutenticado();
        if (nif == null) return null;
        return usuarioRepository.findByNif(nif)
                .map(usuarioMapper::toDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        String nif = this.getNifUsuarioAutenticado();
        if (nif == null) throw new IllegalStateException("No autenticado");
        Usuario usuario = usuarioRepository.findByNif(nif)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        if (!passwordEncoder.matches(currentPassword, usuario.getPassword())) {
            throw new IllegalArgumentException("Contraseña actual incorrecta");
        }
    usuario.setPassword(passwordEncoder.encode(newPassword));
    usuario.setFechaUltimaModificacion(LocalDateTime.now());
    usuario.setLastPasswordChange(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public UsuarioDTO updateUsuarioActual(UsuarioDTO parcial) {
        String nifAuth = this.getNifUsuarioAutenticado();
        if (nifAuth == null) throw new IllegalStateException("No autenticado");
        Usuario usuario = usuarioRepository.findByNif(nifAuth)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        // Actualizar campos permitidos si vienen no nulos
        // Validaciones manuales adicionales (defensa extra)
        if (parcial.getNombre() != null && parcial.getNombre().length() > 100)
            throw new IllegalArgumentException("Nombre demasiado largo");
        if (parcial.getApellido1() != null && parcial.getApellido1().length() > 100)
            throw new IllegalArgumentException("Primer apellido demasiado largo");
        if (parcial.getApellido2() != null && parcial.getApellido2().length() > 100)
            throw new IllegalArgumentException("Segundo apellido demasiado largo");
        if (parcial.getEmail() != null) {
            String email = parcial.getEmail();
            if (!email.matches("^[^@\n]+@[^@\n]+\\.[^@\n]+$")) {
                throw new IllegalArgumentException("Formato de email inválido");
            }
            if (usuarioRepository.existsByEmailAndIdNot(email, usuario.getId())) {
                throw new IllegalArgumentException("Email ya en uso");
            }
        }
        if (parcial.getTelefono() != null && !parcial.getTelefono().isBlank()) {
            if (!parcial.getTelefono().matches("^[0-9+\\-() ]{0,20}$")) {
                throw new IllegalArgumentException("Formato de teléfono inválido");
            }
        }
        if (parcial.getNif() != null) {
            String nifNuevo = parcial.getNif();
            if (!nifNuevo.matches("^[0-9A-Za-z]{6,15}$")) {
                throw new IllegalArgumentException("Formato de NIF inválido");
            }
            if (!nifNuevo.equals(usuario.getNif()) && usuarioRepository.existsByNifAndIdNot(nifNuevo, usuario.getId())) {
                throw new IllegalArgumentException("NIF ya en uso");
            }
        }

        if (parcial.getNombre() != null) usuario.setNombre(parcial.getNombre());
        if (parcial.getApellido1() != null) usuario.setApellido1(parcial.getApellido1());
        if (parcial.getApellido2() != null) usuario.setApellido2(parcial.getApellido2());
        if (parcial.getEmail() != null) usuario.setEmail(parcial.getEmail());
        if (parcial.getTelefono() != null) usuario.setTelefono(parcial.getTelefono());
        if (parcial.getFechaNacimiento() != null) usuario.setFechaNacimiento(parcial.getFechaNacimiento());
        if (parcial.getNif() != null && !parcial.getNif().equals(usuario.getNif())) {
            usuario.setNif(parcial.getNif());
        }
        usuario.setFechaUltimaModificacion(java.time.LocalDateTime.now());
        usuarioRepository.save(usuario);
        return usuarioMapper.toDto(usuario);
    }

    @Override
    @Transactional
    public void deleteCuentaActual() {
        String nif = this.getNifUsuarioAutenticado();
        if (nif == null) throw new IllegalStateException("No autenticado");
        var usuarioOpt = usuarioRepository.findByNif(nif);
        if (usuarioOpt.isEmpty()) return; // idempotente
        Usuario usuario = usuarioOpt.get();
    // Soft delete + anonimización básica
    usuario.setEstadoCuenta("ELIMINADO");
    usuario.setFechaEliminacion(LocalDateTime.now());
    usuario.setNombre("_eliminado_");
    usuario.setApellido1(null);
    usuario.setApellido2(null);
    usuario.setEmail("anon-" + usuario.getId() + "@local");
    usuario.setTelefono(null);
    usuario.setNif("DEL-" + usuario.getId().toString().substring(0,8));
    usuario.setEspecialidad(null);
    usuario.setFechaUltimaModificacion(LocalDateTime.now());
    usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<UserExportDTO.AccesoDTO> getMisLogs(java.time.LocalDateTime desde, java.time.LocalDateTime hasta) {
        String nif = this.getNifUsuarioAutenticado();
        if (nif == null) throw new IllegalStateException("No autenticado");
        var dto = usuarioRepository.findByNif(nif).map(usuarioMapper::toDto).orElse(null);
        if (dto == null) throw new IllegalStateException("Usuario no encontrado");
        var logs = (desde != null && hasta != null)
            ? accessLogRepository.findByUsuarioIdAndTimestampBetweenOrderByTimestampDesc(dto.getId(), desde, hasta)
            : accessLogRepository.findByUsuarioIdOrderByTimestampDesc(dto.getId());
        return logs.stream().map(l -> UserExportDTO.AccesoDTO.builder()
            .timestamp(l.getTimestamp())
            .metodo(l.getMetodo())
            .ruta(l.getRuta())
            .estado(l.getEstado())
            .duracionMs(l.getDuracionMs())
            .ip(l.getIp())
            .userAgent(l.getUserAgent())
            .build()).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserExportDTO exportUsuario() {
        String nif = this.getNifUsuarioAutenticado();
        if (nif == null) throw new IllegalStateException("No autenticado");
        var dto = usuarioRepository.findByNif(nif).map(usuarioMapper::toDto).orElse(null);
        if (dto == null) throw new IllegalStateException("Usuario no encontrado");
        var logs = accessLogRepository.findByUsuarioIdOrderByTimestampDesc(dto.getId());
        var accesos = logs.stream().limit(500).map(l -> UserExportDTO.AccesoDTO.builder()
            .timestamp(l.getTimestamp())
            .metodo(l.getMetodo())
            .ruta(l.getRuta())
            .estado(l.getEstado())
            .duracionMs(l.getDuracionMs())
            .ip(l.getIp())
            .userAgent(l.getUserAgent())
            .build()).toList();
        return UserExportDTO.builder()
            .id(dto.getId())
            .nombre(dto.getNombre())
            .apellido1(dto.getApellido1())
            .apellido2(dto.getApellido2())
            .email(dto.getEmail())
            .nif(dto.getNif())
            .telefono(dto.getTelefono())
            .fechaNacimiento(dto.getFechaNacimiento())
            .fechaCreacion(dto.getFechaCreacion())
            .fechaUltimaModificacion(dto.getFechaUltimaModificacion())
            .estadoCuenta(usuarioRepository.findByNif(dto.getNif()).map(u->u.getEstadoCuenta()).orElse(null))
            .fechaEliminacion(usuarioRepository.findByNif(dto.getNif()).map(u->u.getFechaEliminacion()).orElse(null))
            .accesos(accesos)
            .build();
    }

}
