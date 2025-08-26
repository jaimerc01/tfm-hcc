package com.hcc.tfm_hcc.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.hcc.tfm_hcc.dto.ChangePasswordRequest;
import com.hcc.tfm_hcc.model.SolicitudAsignacion;

public interface UsuarioController {

    /**
     * Da de alta un nuevo usuario en el sistema.
     *
     * @param usuarioDTO el objeto que contiene los datos del usuario a dar de alta
     * @return el id del usuario creado, o un mensaje de error si la operación falla
     */
    // String altaUsuario(UsuarioDTO usuarioDTO);

    /**
     * Obtiene el nombre del usuario autenticado.
     *
     * @return el nombre del usuario, o null si no está autenticado
     */
    String getNombreUsuario();

    /*
     * Obtiene los detalles del usuario autenticado.
     *
     * @return los detalles del usuario, o un mensaje de error si no está autenticado
     */
    ResponseEntity<?> getUsuarioActual();


    /*
     * Cambia la contraseña del usuario autenticado.
     *
     * @param body el objeto que contiene la contraseña actual y la nueva
     * @return un mensaje de éxito o un mensaje de error si la operación falla
     */
    ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest body);

    ResponseEntity<List<SolicitudAsignacion>> listarMisSolicitudes();
    ResponseEntity<?> actualizarEstadoSolicitud(String solicitudId, Map<String, String> body);
}
