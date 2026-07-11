package com.hcc.tfm_hcc.service;

import java.util.List;
import java.util.UUID;

import com.hcc.tfm_hcc.model.Usuario;

/**
 * Servicio para la gestión de relaciones entre usuarios y perfiles.
 */
public interface PerfilUsuarioService {

    /**
     * Asigna un perfil a un usuario si todavía no lo tiene.
     *
     * @param usuarioId identificador del usuario
     * @param rol rol a asignar
     */
    void asignarPerfil(UUID usuarioId, String rol);

    /**
     * Revoca un perfil de un usuario si existe la relación.
     *
     * @param usuarioId identificador del usuario
     * @param rol rol a revocar
     */
    void revocarPerfil(UUID usuarioId, String rol);

    /**
     * Indica si un usuario tiene asignado un rol concreto.
     *
     * @param usuarioId identificador del usuario
     * @param rol rol a comprobar
     * @return true si el usuario tiene el rol
     */
    boolean tienePerfil(UUID usuarioId, String rol);

    /**
     * Lista los usuarios que tienen asignado un rol concreto.
     *
     * @param rol rol a filtrar
     * @return usuarios que tienen ese rol
     */
    List<Usuario> listarUsuariosPorRol(String rol);
}