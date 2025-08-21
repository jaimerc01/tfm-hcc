package com.hcc.tfm_hcc.service;

import com.hcc.tfm_hcc.dto.UsuarioDTO;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.UUID;

public interface MedicoService {
    List<UsuarioDTO> listarMedicos();
    UsuarioDTO crearMedico(UsuarioDTO medicoDTO);
    UsuarioDTO actualizarMedico(UUID id, UsuarioDTO medicoDTO);
    void eliminarMedico(UUID id);
}
