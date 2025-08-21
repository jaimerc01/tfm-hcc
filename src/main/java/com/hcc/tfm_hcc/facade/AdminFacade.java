package com.hcc.tfm_hcc.facade;

import com.hcc.tfm_hcc.dto.UsuarioDTO;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.UUID;

public interface AdminFacade {
    ResponseEntity<List<UsuarioDTO>> listarMedicos();
    ResponseEntity<UsuarioDTO> crearMedico(UsuarioDTO medicoDTO);
    ResponseEntity<UsuarioDTO> actualizarMedico(UUID id, UsuarioDTO medicoDTO);
    ResponseEntity<Void> eliminarMedico(UUID id);
}
