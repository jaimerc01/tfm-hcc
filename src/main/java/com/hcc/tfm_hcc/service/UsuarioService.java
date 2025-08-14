package com.hcc.tfm_hcc.service;

import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.dto.UserExportDTO;
import java.time.LocalDateTime;
import java.util.List;
import com.hcc.tfm_hcc.model.Usuario;

public interface UsuarioService {
    
    /**
     * Da de alta un nuevo usuario en el sistema.
     *
     * @param usuarioDTO el objeto que contiene los datos del usuario a dar de alta
     * @return el usuario creado
     */
    public Usuario altaUsuario(UsuarioDTO usuarioDTO);

    /**
     * Obtiene el nombre del usuario autenticado.
     *
     * @param token el token de autenticación del usuario
     * @return el nombre del usuario, o null si no está autenticado
     */
    String getNombreUsuario();

    /**
     * Obtiene el DTO completo del usuario autenticado.
     * @return UsuarioDTO o null si no autenticado
     */
    UsuarioDTO getUsuarioActual();

    /** Cambia contraseña del usuario autenticado */
    void changePassword(String currentPassword, String newPassword);

    /** Actualiza datos (no password) del usuario autenticado */
    UsuarioDTO updateUsuarioActual(UsuarioDTO parcial);

    /** Elimina la cuenta del usuario autenticado */
    void deleteCuentaActual();

    /** Devuelve los accesos del usuario autenticado opcionalmente filtrados por rango */
    List<UserExportDTO.AccesoDTO> getMisLogs(LocalDateTime desde, LocalDateTime hasta);

    /** Genera el export de datos del usuario autenticado */
    UserExportDTO exportUsuario();
}
