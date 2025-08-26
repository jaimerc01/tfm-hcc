package com.hcc.tfm_hcc.facade.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.dto.UserExportDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.mapper.UsuarioMapper;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;
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
    public List<UserExportDTO.AccesoDTO> getMisLogs(LocalDateTime desde, LocalDateTime hasta) {
        return usuarioService.getMisLogs(desde, hasta);
    }

    @Override
    public UserExportDTO exportUsuario() {
        return usuarioService.exportUsuario();
    }

    @Override
    public List<SolicitudAsignacion> listarMisSolicitudes() {
        return usuarioService.listarMisSolicitudes();
    }

    @Override
    public SolicitudAsignacion actualizarEstadoSolicitud(String solicitudId, String nuevoEstado) {
        return usuarioService.actualizarEstadoSolicitud(solicitudId, nuevoEstado);
    }
}
