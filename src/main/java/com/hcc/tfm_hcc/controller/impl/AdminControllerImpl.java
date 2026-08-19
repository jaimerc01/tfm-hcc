package com.hcc.tfm_hcc.controller.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hcc.tfm_hcc.constants.RestUrls;
import com.hcc.tfm_hcc.controller.AdminController;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.facade.AdminFacade;
import com.hcc.tfm_hcc.exception.AdminValidationException;
import com.hcc.tfm_hcc.exception.AdminOperacionException;
import com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException;

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
 *   <li>Validación de autorización delegada a la capa facade</li>
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
@RequestMapping(RestUrls.ADMIN_BASE)
@RequiredArgsConstructor
public class AdminControllerImpl implements AdminController {

    /** Facade para operaciones administrativas */
    private final AdminFacade adminFacade;

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.ADMIN_MEDICOS)
    public ResponseEntity<List<UsuarioDTO>> listarMedicos() {
        log.info("Listando todos los médicos del sistema");
        try {
            ResponseEntity<List<UsuarioDTO>> result = adminFacade.listarMedicos();
            log.info("Médicos listados exitosamente");
            return result;
        } catch (Exception e) {
            log.error("Error al listar médicos: {}", e.getMessage(), e);
            throw new AdminOperacionException("Error al listar médicos", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.ADMIN_USUARIOS_BY_NIF)
    public ResponseEntity<UsuarioDTO> buscarUsuarioPorNif(@RequestParam("nif") String nif) {
        log.info("Buscando usuario por NIF: {}", nif);
        try {
            if (nif == null || nif.trim().isEmpty()) {
                log.warn("Intento de búsqueda con NIF nulo o vacío");
                throw new AdminValidationException("El NIF es obligatorio");
            }

            ResponseEntity<UsuarioDTO> result = adminFacade.buscarUsuarioPorNif(nif);
            if (result.getBody() == null) {
                log.warn("Usuario no encontrado con NIF: {}", nif);
                throw new UsuarioNoEncontradoException("Usuario no encontrado con NIF: " + nif);
            }

            log.info("Usuario encontrado exitosamente: {}", nif);
            return result;
        } catch (AdminValidationException | UsuarioNoEncontradoException e) {
            log.warn("Error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al buscar usuario por NIF: {}", e.getMessage(), e);
            throw new AdminOperacionException("Error al buscar usuario", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping(RestUrls.ADMIN_MEDICOS)
    public ResponseEntity<UsuarioDTO> crearMedico(@RequestBody UsuarioDTO medicoDTO) {
        try {
            if (medicoDTO == null) {
                log.warn("Intento de crear médico sin datos");
                throw new AdminValidationException("Los datos del médico son obligatorios");
            }

            if (medicoDTO.getNif() == null || medicoDTO.getNif().trim().isEmpty()) {
                log.warn("Intento de crear médico sin NIF");
                throw new AdminValidationException("El NIF del médico es obligatorio");
            }

            log.info("Creando nuevo médico: {}", medicoDTO.getNif());
            ResponseEntity<UsuarioDTO> result = adminFacade.crearMedico(medicoDTO);
            log.info("Médico creado exitosamente: {}", medicoDTO.getNif());
            return result;
        } catch (AdminValidationException e) {
            log.warn("Error de validación al crear médico: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al crear médico: {}", e.getMessage(), e);
            throw new AdminOperacionException("Error al crear médico", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PutMapping(RestUrls.ADMIN_MEDICO_ID)
    public ResponseEntity<UsuarioDTO> actualizarMedico(@PathVariable("id") UUID id, @RequestBody UsuarioDTO medicoDTO) {
        try {
            if (id == null) {
                log.warn("Intento de actualizar médico sin ID");
                throw new AdminValidationException("El ID del médico es obligatorio");
            }

            if (medicoDTO == null) {
                log.warn("Intento de actualizar médico sin datos");
                throw new AdminValidationException("Los datos del médico son obligatorios");
            }

            log.info("Actualizando médico con ID: {}", id);
            ResponseEntity<UsuarioDTO> result = adminFacade.actualizarMedico(id, medicoDTO);
            log.info("Médico actualizado exitosamente: {}", id);
            return result;
        } catch (AdminValidationException e) {
            log.warn("Error de validación al actualizar médico: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al actualizar médico: {}", e.getMessage(), e);
            throw new AdminOperacionException("Error al actualizar médico", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DeleteMapping(RestUrls.ADMIN_MEDICO_ID)
    public ResponseEntity<UUID> eliminarMedico(@PathVariable("id") UUID id) {
        try {
            if (id == null) {
                log.warn("Intento de eliminar médico sin ID");
                throw new AdminValidationException("El ID del médico es obligatorio");
            }

            log.info("Eliminando médico con ID: {}", id);
            ResponseEntity<UUID> result = adminFacade.eliminarMedico(id);
            log.info("Médico eliminado exitosamente: {}", id);
            return result;
        } catch (AdminValidationException e) {
            log.warn("Error de validación al eliminar médico: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al eliminar médico: {}", e.getMessage(), e);
            throw new AdminOperacionException("Error al eliminar médico", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PutMapping(RestUrls.ADMIN_MEDICO_PERFIL)
    public ResponseEntity<UUID> setPerfilMedico(@PathVariable("id") UUID id, @RequestParam("asignar") boolean asignar) {
        try {
            if (id == null) {
                log.warn("Intento de modificar perfil médico sin ID");
                throw new AdminValidationException("El ID del usuario es obligatorio");
            }

            log.info("Modificando perfil médico para usuario ID: {}, asignar: {}", id, asignar);
            ResponseEntity<UUID> result = adminFacade.setPerfilMedico(id, asignar);
            log.info("Perfil médico modificado exitosamente: {} -> {}", id, asignar);
            return result;
        } catch (AdminValidationException e) {
            log.warn("Error de validación al modificar perfil: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al modificar perfil médico: {}", e.getMessage(), e);
            throw new AdminOperacionException("Error al modificar perfil médico", e);
        }
    }
}
