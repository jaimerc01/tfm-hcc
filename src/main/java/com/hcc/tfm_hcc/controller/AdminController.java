package com.hcc.tfm_hcc.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.hcc.tfm_hcc.dto.UsuarioDTO;

/**
 * Controlador REST para operaciones administrativas en el sistema HCC.
 * Proporciona endpoints para la gestión de médicos y funcionalidades
 * administrativas exclusivas para usuarios con rol de administrador.
 * 
 * <p>Funcionalidades principales:</p>
 * <ul>
 *   <li>Gestión completa de médicos (CRUD)</li>
 *   <li>Asignación y revocación de perfiles médicos</li>
 *   <li>Listado y consulta de usuarios médicos</li>
 *   <li>Control de acceso administrativo</li>
 * </ul>
 * 
 * <p>Todos los endpoints requieren autenticación y rol de administrador.</p>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface AdminController {
    
    /**
     * Lista todos los usuarios con perfil médico en el sistema.
     * 
     * @return ResponseEntity con lista de UsuarioDTO de médicos registrados
     */
    @GetMapping("/medicos")
    ResponseEntity<List<UsuarioDTO>> listarMedicos();

    /**
     * Crea un nuevo usuario médico en el sistema.
     * 
     * @param medicoDTO Datos del médico a crear
     * @return ResponseEntity con el UsuarioDTO del médico creado
     */
    @PostMapping("/medicos")
    ResponseEntity<UsuarioDTO> crearMedico(@RequestBody UsuarioDTO medicoDTO);

    /**
     * Actualiza la información de un médico existente.
     * 
     * @param id ID único del médico a actualizar
     * @param medicoDTO Datos actualizados del médico
     * @return ResponseEntity con el UsuarioDTO del médico actualizado
     */
    @PutMapping("/medicos/{id}")
    ResponseEntity<UsuarioDTO> actualizarMedico(@PathVariable("id") UUID id, @RequestBody UsuarioDTO medicoDTO);

    /**
     * Elimina un médico del sistema.
     * Esta operación es permanente y elimina todos los datos asociados.
     * 
     * @param id ID único del médico a eliminar
     * @return ResponseEntity vacío confirmando la eliminación
     */
    @DeleteMapping("/medicos/{id}")
    ResponseEntity<Void> eliminarMedico(@PathVariable("id") UUID id);

    /**
     * Asigna o revoca el perfil médico a un usuario específico.
     * Permite convertir usuarios normales en médicos o viceversa.
     * 
     * @param id ID único del usuario
     * @param asignar true para asignar perfil médico, false para revocarlo
     * @return ResponseEntity vacío confirmando la operación
     */
    @PutMapping("/medicos/{id}/perfil-medico")
    ResponseEntity<Void> setPerfilMedico(@PathVariable("id") UUID id, @RequestParam("asignar") boolean asignar);
}
