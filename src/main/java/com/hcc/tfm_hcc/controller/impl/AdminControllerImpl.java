package com.hcc.tfm_hcc.controller.impl;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.UUID;
import com.hcc.tfm_hcc.facade.AdminFacade;
import com.hcc.tfm_hcc.controller.AdminController;
import com.hcc.tfm_hcc.dto.UsuarioDTO;

@RestController
@RequestMapping("/admin")
public class AdminControllerImpl implements AdminController {

    @Autowired
    private AdminFacade adminFacade;

    // ping endpoint removed

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/medicos")
    public ResponseEntity<List<UsuarioDTO>> listarMedicos() {
        return adminFacade.listarMedicos();
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/medicos")
    public ResponseEntity<UsuarioDTO> crearMedico(@RequestBody UsuarioDTO medicoDTO) {
        return adminFacade.crearMedico(medicoDTO);
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/medicos/{id}")
    public ResponseEntity<UsuarioDTO> actualizarMedico(@PathVariable("id") UUID id, @RequestBody UsuarioDTO medicoDTO) {
        return adminFacade.actualizarMedico(id, medicoDTO);
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/medicos/{id}")
    public ResponseEntity<Void> eliminarMedico(@PathVariable("id") UUID id) {
        return adminFacade.eliminarMedico(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/medicos/{id}/perfil-medico")
    public ResponseEntity<Void> setPerfilMedico(@PathVariable("id") UUID id, @RequestParam("asignar") boolean asignar) {
        return adminFacade.setPerfilMedico(id, asignar);
    }
}
