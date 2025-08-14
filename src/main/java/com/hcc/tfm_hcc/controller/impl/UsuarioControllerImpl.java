package com.hcc.tfm_hcc.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.hcc.tfm_hcc.controller.UsuarioController;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.dto.ChangePasswordRequest;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.dto.UpdateUsuarioRequest;
// import com.hcc.tfm_hcc.repository.AccessLogRepository;
// import com.hcc.tfm_hcc.repository.UsuarioRepository;
import java.time.LocalDateTime;
// import java.util.stream.Collectors;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuario")
public class UsuarioControllerImpl implements UsuarioController{

    @Autowired
    private UsuarioFacade usuarioFacade;
    // Delegado a la capa de servicio/fachada; no se usa directamente aquí

    // @Override
    // @PostMapping("/alta")
    // public String altaUsuario(@RequestBody UsuarioDTO usuarioDTO) {
    //     return usuarioFacade.altaUsuario(usuarioDTO);
    // }

    @Override
    @GetMapping("/me")
    public String getNombreUsuario() {
        return usuarioFacade.getNombreUsuario();
    }

    @Override
    @GetMapping("/me/detalle")
    public ResponseEntity<?> getUsuarioActual() {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) return ResponseEntity.status(401).build();
        if (dto.getPassword() != null) dto.setPassword(null);
        return ResponseEntity.ok(dto);
    }

    @Override
    @PostMapping("/change-password")
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
            return ResponseEntity.ok(java.util.Map.of("id", dto.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al cambiar contraseña");
        }
    }

    @PutMapping("/me")
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
    public ResponseEntity<?> misLogs(String desde, String hasta) {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) return ResponseEntity.status(401).build();
        LocalDateTime d = null, h = null;
        try { if (desde != null) d = LocalDateTime.parse(desde); } catch (Exception ignored) {}
        try { if (hasta != null) h = LocalDateTime.parse(hasta); } catch (Exception ignored) {}
        return ResponseEntity.ok(usuarioFacade.getMisLogs(d, h));
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportUsuario() {
        var dto = usuarioFacade.getUsuarioActual();
        if (dto == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(usuarioFacade.exportUsuario());
    }
}
