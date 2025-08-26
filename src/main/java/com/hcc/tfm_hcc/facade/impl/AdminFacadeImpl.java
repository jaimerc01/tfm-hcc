package com.hcc.tfm_hcc.facade.impl;

import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.facade.AdminFacade;
import com.hcc.tfm_hcc.service.MedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;


@Component
public class AdminFacadeImpl implements AdminFacade {
    @Autowired
    private MedicoService medicoService;

    @Override
    public ResponseEntity<List<UsuarioDTO>> listarMedicos() {
        return ResponseEntity.ok(medicoService.listarMedicos());
    }

    @Override
    public ResponseEntity<UsuarioDTO> crearMedico(UsuarioDTO medicoDTO) {
        try {
            return ResponseEntity.ok(medicoService.crearMedico(medicoDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<UsuarioDTO> actualizarMedico(UUID id, UsuarioDTO medicoDTO) {
        try {
            return ResponseEntity.ok(medicoService.actualizarMedico(id, medicoDTO));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<Void> eliminarMedico(UUID id) {
        try {
            medicoService.eliminarMedico(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<Void> setPerfilMedico(UUID id, boolean asignar) {
        try {
            medicoService.setPerfilMedico(id, asignar);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
