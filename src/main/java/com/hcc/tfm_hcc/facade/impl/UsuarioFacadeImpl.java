package com.hcc.tfm_hcc.facade.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.dto.UserExportDTO;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.service.UsuarioService;

@Component
public class UsuarioFacadeImpl implements UsuarioFacade {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioMapper usuarioMapper;

    @Override
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    public UsuarioDTO altaUsuario(UsuarioDTO usuarioDTO) {
        return usuarioMapper.toDto(usuarioService.altaUsuario(usuarioDTO));
    }

    @Override
    public String getNombreUsuario() {
        return usuarioService.getNombreUsuario();
    }

    @Override
    public UsuarioDTO getUsuarioActual() {
        return usuarioService.getUsuarioActual();
    }

    @Override
    public void changePassword(String currentPassword, String newPassword) {
        usuarioService.changePassword(currentPassword, newPassword);
    }

    @Override
    public UsuarioDTO updateUsuarioActual(UsuarioDTO parcial) {
        return usuarioService.updateUsuarioActual(parcial);
    }

    @Override
    public void deleteCuentaActual() {
        usuarioService.deleteCuentaActual();
    }

    @Override
    public java.util.List<UserExportDTO.AccesoDTO> getMisLogs(java.time.LocalDateTime desde, java.time.LocalDateTime hasta) {
        return usuarioService.getMisLogs(desde, hasta);
    }

    @Override
    public UserExportDTO exportUsuario() {
        return usuarioService.exportUsuario();
    }

    @Override
    public java.util.List<com.hcc.tfm_hcc.model.SolicitudAsignacion> listarMisSolicitudes() {
        return usuarioService.listarMisSolicitudes();
    }

    @Override
    public com.hcc.tfm_hcc.model.SolicitudAsignacion actualizarEstadoSolicitud(Long solicitudId, String nuevoEstado) {
        return usuarioService.actualizarEstadoSolicitud(solicitudId, nuevoEstado);
    }
}
