package com.hcc.tfm_hcc.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.dto.PacienteDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.PerfilUsuario;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.PerfilRepository;
import com.hcc.tfm_hcc.repository.PerfilUsuarioRepository;
import com.hcc.tfm_hcc.repository.SolicitudAsignacionRepository;
import com.hcc.tfm_hcc.repository.MedicoPacienteRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.MedicoService;
import com.hcc.tfm_hcc.service.UsuarioService;
import com.hcc.tfm_hcc.util.FechaUtils;

@Service
public class MedicoServiceImpl implements MedicoService {
    @Autowired
    private SolicitudAsignacionRepository solicitudAsignacionRepository;
    // Internal helper that creates a solicitud given both nifMedico and nifPaciente
    private SolicitudAsignacion crearSolicitudAsignacionConMedico(String nifMedico, String nifPaciente) {
        Usuario medico = usuarioRepository.findByNif(nifMedico).orElse(null);
        Usuario paciente = usuarioRepository.findByNif(nifPaciente).orElse(null);
        if (medico == null || paciente == null) return null;
        // Check for existing pending solicitud
        boolean exists = solicitudAsignacionRepository.existsByMedicoNifAndPacienteNifAndEstado(nifMedico, nifPaciente, "PENDIENTE");
        if (exists) {
            throw new com.hcc.tfm_hcc.exception.SolicitudExistenteException("Ya existe una solicitud pendiente para este médico y paciente");
        }
        SolicitudAsignacion solicitud = new SolicitudAsignacion();
        solicitud.setMedico(medico);
        solicitud.setPaciente(paciente);
        solicitud.setEstado("PENDIENTE");
        solicitud.setFechaCreacion(java.time.LocalDateTime.now());
        return solicitudAsignacionRepository.save(solicitud);
    }

    @Override
    public SolicitudAsignacion crearSolicitudAsignacion(String nifPaciente) {
        String nifMedico = com.hcc.tfm_hcc.util.SecurityUtils.getCurrentUserNif();
        if (nifMedico == null) return null;
    return crearSolicitudAsignacionConMedico(nifMedico, nifPaciente);
    }

    @Override
    public List<SolicitudAsignacion> listarSolicitudesPendientes() {
        String nifMedico = com.hcc.tfm_hcc.util.SecurityUtils.getCurrentUserNif();
        if (nifMedico == null) return Collections.emptyList();
        return solicitudAsignacionRepository.findByMedicoNifAndEstado(nifMedico, "PENDIENTE");
    }

    @Override
    public List<SolicitudAsignacion> listarSolicitudesEnviadas() {
        String nifMedico = com.hcc.tfm_hcc.util.SecurityUtils.getCurrentUserNif();
        if (nifMedico == null) return Collections.emptyList();
        return solicitudAsignacionRepository.findByMedicoNifOrderByFechaCreacionDesc(nifMedico);
    }
    
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
    @Autowired
    private MedicoPacienteRepository medicoPacienteRepository;

    @Override
    public PacienteDTO buscarPacientePorDniYFechaNacimiento(String dni, String fechaNacimiento) {
        return usuarioRepository.findByNif(dni)
            .filter(usuario -> {
                if (usuario.getFechaNacimiento() == null) return false;
                String fechaUsuario = FechaUtils.toIsoDate(usuario.getFechaNacimiento());
                return fechaUsuario.equals(fechaNacimiento);
            })
            .map(usuario -> {
                PacienteDTO dto = new PacienteDTO();
                dto.setNombre(usuario.getNombre());
                dto.setApellido1(usuario.getApellido1());
                dto.setApellido2(usuario.getApellido2());
                dto.setNif(usuario.getNif());
                dto.setFechaNacimiento(FechaUtils.toIsoDate(usuario.getFechaNacimiento()));
                return dto;
            })
            .orElse(null);
    }

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

    @Override
    public void setPerfilMedico(UUID id, boolean asignar) {
        var usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) throw new IllegalArgumentException("Usuario no encontrado");
        Usuario usuario = usuarioOpt.get();
        var perfilMedicoOpt = perfilRepository.getPerfilByRol("MEDICO");
        var perfilPacienteOpt = perfilRepository.getPerfilByRol("PACIENTE");
        if (perfilMedicoOpt.isEmpty() || perfilPacienteOpt.isEmpty()) throw new IllegalStateException("Perfiles requeridos no configurados");

        var perfilMedico = perfilMedicoOpt.get();
        var perfilPaciente = perfilPacienteOpt.get();

        if (asignar) {
            // añadir relación PerfilUsuario MEDICO si no existe
            boolean already = StreamSupport.stream(perfilUsuarioRepository.findAll().spliterator(), false)
                .anyMatch(pu -> pu.getUsuario().getId().equals(id) && pu.getPerfil().getRol().equalsIgnoreCase("MEDICO"));
            if (!already) {
                PerfilUsuario pu = new PerfilUsuario();
                pu.setPerfil(perfilMedico);
                pu.setUsuario(usuario);
                pu.setFechaCreacion(java.time.LocalDateTime.now());
                perfilUsuarioRepository.save(pu);
            }
        } else {
            // Si tiene relaciones MedicoPaciente, marcarlas como REVOCADA
            var relacionesMP = medicoPacienteRepository.findByMedicoIdAndEstadoNot(id, "REVOCADA");
            if (relacionesMP != null && !relacionesMP.isEmpty()) {
                for (var rel : relacionesMP) {
                    rel.setEstado("REVOCADA");
                    rel.setFechaUltimaModificacion(java.time.LocalDateTime.now());
                }
                medicoPacienteRepository.saveAll(relacionesMP);
            }
            // revocar perfil MEDICO
            var relaciones = StreamSupport.stream(perfilUsuarioRepository.findAll().spliterator(), false)
                .filter(pu -> pu.getUsuario().getId().equals(id) && pu.getPerfil().getRol().equalsIgnoreCase("MEDICO"))
                .collect(Collectors.toList());
            if (!relaciones.isEmpty()) {
                perfilUsuarioRepository.deleteAll(relaciones);
            }
            // garantizar que tenga perfil PACIENTE
            boolean hasPaciente = StreamSupport.stream(perfilUsuarioRepository.findAll().spliterator(), false)
                .anyMatch(pu -> pu.getUsuario().getId().equals(id) && pu.getPerfil().getRol().equalsIgnoreCase("PACIENTE"));
            if (!hasPaciente) {
                PerfilUsuario pu2 = new PerfilUsuario();
                pu2.setPerfil(perfilPaciente);
                pu2.setUsuario(usuario);
                pu2.setFechaCreacion(java.time.LocalDateTime.now());
                perfilUsuarioRepository.save(pu2);
            }
        }
    }
}
