package com.hcc.tfm_hcc.controller.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hcc.tfm_hcc.controller.AdminController;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.facade.AdminFacade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del controlador REST para operaciones administrativas.
 * Proporciona endpoints REST para la gestión de médicos y funcionalidades
 * administrativas, delegando la lógica de negocio al facade correspondiente.
 * 
 * <p>Características de seguridad:</p>
 * <ul>
 *   <li>Todos los endpoints requieren rol ADMINISTRADOR</li>
 *   <li>Validación de autorización mediante @PreAuthorize</li>
 *   <li>Logging detallado de operaciones administrativas</li>
 *   <li>Gestión centralizada de errores</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminControllerImpl implements AdminController {

    /** Facade para operaciones administrativas */
    private final AdminFacade adminFacade;

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/medicos")
    public ResponseEntity<List<UsuarioDTO>> listarMedicos() {
        log.info("Listando todos los médicos del sistema");
        return adminFacade.listarMedicos();
    }

    /**
     * Busca un usuario específico por su NIF.
     * Endpoint adicional para funcionalidades administrativas.
     * 
     * @param nif NIF del usuario a buscar
     * @return ResponseEntity con el UsuarioDTO encontrado
     */
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/usuarios/by-nif")
    public ResponseEntity<UsuarioDTO> buscarUsuarioPorNif(@RequestParam("nif") String nif) {
        log.info("Buscando usuario por NIF: {}", nif);
        return adminFacade.buscarUsuarioPorNif(nif);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/medicos")
    public ResponseEntity<UsuarioDTO> crearMedico(@RequestBody UsuarioDTO medicoDTO) {
        log.info("Creando nuevo médico: {}", medicoDTO.getNif());
        return adminFacade.crearMedico(medicoDTO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/medicos/{id}")
    public ResponseEntity<UsuarioDTO> actualizarMedico(@PathVariable("id") UUID id, @RequestBody UsuarioDTO medicoDTO) {
        log.info("Actualizando médico con ID: {}", id);
        return adminFacade.actualizarMedico(id, medicoDTO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/medicos/{id}")
    public ResponseEntity<Void> eliminarMedico(@PathVariable("id") UUID id) {
        log.info("Eliminando médico con ID: {}", id);
        return adminFacade.eliminarMedico(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/medicos/{id}/perfil-medico")
    public ResponseEntity<Void> setPerfilMedico(@PathVariable("id") UUID id, @RequestParam("asignar") boolean asignar) {
        log.info("Modificando perfil médico para usuario ID: {}, asignar: {}", id, asignar);
        return adminFacade.setPerfilMedico(id, asignar);
    }
}
