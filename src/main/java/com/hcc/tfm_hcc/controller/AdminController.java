package com.hcc.tfm_hcc.controller;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
import com.hcc.tfm_hcc.dto.UsuarioDTO;

public interface AdminController {
    @GetMapping("/medicos")
    ResponseEntity<List<UsuarioDTO>> listarMedicos();

    @PostMapping("/medicos")
    ResponseEntity<UsuarioDTO> crearMedico(@RequestBody UsuarioDTO medicoDTO);

    @PutMapping("/medicos/{id}")
    ResponseEntity<UsuarioDTO> actualizarMedico(@PathVariable("id") UUID id, @RequestBody UsuarioDTO medicoDTO);

    @DeleteMapping("/medicos/{id}")
    ResponseEntity<Void> eliminarMedico(@PathVariable("id") UUID id);

    @PutMapping("/medicos/{id}/perfil-medico")
    ResponseEntity<Void> setPerfilMedico(@PathVariable("id") UUID id, @RequestParam("asignar") boolean asignar);
}
