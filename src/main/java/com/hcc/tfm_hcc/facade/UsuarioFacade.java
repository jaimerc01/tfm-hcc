package com.hcc.tfm_hcc.facade;

import com.hcc.tfm_hcc.dto.UsuarioDTO;

public interface UsuarioFacade {
    /**
     * Da de alta un nuevo usuario en el sistema.
     *
     * @param usuarioDTO el objeto que contiene los datos del usuario a dar de alta
     * @return el usuario dado de alta
     */
    UsuarioDTO altaUsuario(UsuarioDTO usuarioDTO);

    /**
     * Obtiene el nombre del usuario autenticado.
     *
     * @return el nombre del usuario, o null si no está autenticado
     */
    String getNombreUsuario();

    /** Devuelve datos completos del usuario autenticado */
    UsuarioDTO getUsuarioActual();

    /** Cambia la contraseña del usuario autenticado */
    void changePassword(String currentPassword, String newPassword);

    /** Actualiza campos editables del usuario autenticado */
    UsuarioDTO updateUsuarioActual(UsuarioDTO parcial);

    /** Elimina la cuenta del usuario autenticado */
    void deleteCuentaActual();
}
