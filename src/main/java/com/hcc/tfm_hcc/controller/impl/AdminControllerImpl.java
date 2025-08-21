package com.hcc.tfm_hcc.controller.impl;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.UUID;
import com.hcc.tfm_hcc.controller.AdminController;
import com.hcc.tfm_hcc.dto.UsuarioDTO;

@RestController
@RequestMapping("/admin")
public class AdminControllerImpl implements AdminController {

    @Autowired
    private com.hcc.tfm_hcc.facade.AdminFacade adminFacade;

    @Override
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("OK-ADMIN");
    }

    @Override
    @GetMapping("/medicos")
    public ResponseEntity<List<UsuarioDTO>> listarMedicos() {
        return adminFacade.listarMedicos();
    }

    @Override
    @PostMapping("/medicos")
    public ResponseEntity<UsuarioDTO> crearMedico(@RequestBody UsuarioDTO medicoDTO) {
        return adminFacade.crearMedico(medicoDTO);
    }

    @Override
    @PutMapping("/medicos/{id}")
    public ResponseEntity<UsuarioDTO> actualizarMedico(@PathVariable("id") UUID id, @RequestBody UsuarioDTO medicoDTO) {
        return adminFacade.actualizarMedico(id, medicoDTO);
    }

    @Override
    @DeleteMapping("/medicos/{id}")
    public ResponseEntity<Void> eliminarMedico(@PathVariable("id") UUID id) {
        return adminFacade.eliminarMedico(id);
    }
}
