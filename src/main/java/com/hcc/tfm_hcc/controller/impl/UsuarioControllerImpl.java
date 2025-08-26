package com.hcc.tfm_hcc.controller.impl;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.hcc.tfm_hcc.controller.UsuarioController;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import com.hcc.tfm_hcc.dto.ChangePasswordRequest;
import com.hcc.tfm_hcc.dto.UpdateUsuarioRequest;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.facade.UsuarioFacade;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuario")
public class UsuarioControllerImpl implements UsuarioController{

    @Autowired
    private UsuarioFacade usuarioFacade;

    @Override
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public String getNombreUsuario() {
        return usuarioFacade.getNombreUsuario();
    }

    @Override
    @GetMapping("/me/detalle")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUsuarioActual() {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) return ResponseEntity.status(401).build();
        if (dto.getPassword() != null) dto.setPassword(null);
        return ResponseEntity.ok(dto);
    }

    @Override
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest body) {
        if (body.getNewPassword() == null || body.getNewPassword().length() < 6) {
            return ResponseEntity.badRequest().body("Nueva contraseña demasiado corta");
        }
        try {
            usuarioFacade.changePassword(body.getCurrentPassword(), body.getNewPassword());
            var dto = usuarioFacade.getUsuarioActual();
            if (dto == null) {
                return ResponseEntity.status(401).build();
            }
            return ResponseEntity.ok(Map.of("id", dto.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al cambiar contraseña");
        }
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateUsuarioActual(@Valid @RequestBody UpdateUsuarioRequest req) {
        try {
            UsuarioDTO parcial = new UsuarioDTO();
            parcial.setNombre(req.getNombre());
            parcial.setApellido1(req.getApellido1());
            parcial.setApellido2(req.getApellido2());
            parcial.setNif(req.getNif());
            parcial.setEmail(req.getEmail());
            parcial.setTelefono(req.getTelefono());
            parcial.setFechaNacimiento(req.getFechaNacimiento());
            UsuarioDTO before = usuarioFacade.getUsuarioActual();
            String oldNif = before != null ? before.getNif() : null;
            UsuarioDTO actualizado = usuarioFacade.updateUsuarioActual(parcial);
            if (actualizado.getPassword() != null) actualizado.setPassword(null);
            boolean nifChanged = oldNif != null && actualizado.getNif() != null && !oldNif.equals(actualizado.getNif());
            ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
            if (nifChanged) {
                builder.header("X-Reauth-Required", "true");
            }
            return builder.body(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al actualizar datos de usuario");
        }
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteCuenta() {
        try {
            usuarioFacade.deleteCuentaActual();
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body("No autenticado");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al eliminar cuenta");
        }
    }

    @GetMapping("/logs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> misLogs(String desde, String hasta) {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) return ResponseEntity.status(401).build();
        LocalDateTime d = null, h = null;
        try { if (desde != null) d = LocalDateTime.parse(desde); } catch (Exception ignored) {}
        try { if (hasta != null) h = LocalDateTime.parse(hasta); } catch (Exception ignored) {}
        return ResponseEntity.ok(usuarioFacade.getMisLogs(d, h));
    }

    @GetMapping("/solicitud/mis")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SolicitudAsignacion>> listarMisSolicitudes() {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(usuarioFacade.listarMisSolicitudes());
    }

    @PostMapping("/solicitud/{id}/estado")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> actualizarEstadoSolicitud(@PathVariable("id") String id, @RequestBody Map<String, String> body) {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) return ResponseEntity.status(401).build();
        String nuevoEstado = body.get("estado");
        try {
            var updated = usuarioFacade.actualizarEstadoSolicitud(id, nuevoEstado);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error actualizando estado");
        }
    }

    @GetMapping("/export")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> exportUsuario() {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(usuarioFacade.exportUsuario());
    }
}
