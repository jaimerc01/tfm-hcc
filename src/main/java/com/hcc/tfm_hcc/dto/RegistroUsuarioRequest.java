package com.hcc.tfm_hcc.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * Cuerpo de la petición pública de registro ({@code POST /authentication/signup}).
 *
 * <p>Deliberadamente separado de {@link UsuarioDTO}: ese DTO se usa también como
 * representación de salida en varios endpoints (perfil propio, búsqueda de usuario por
 * un administrador...) y lleva campos internos (id, estadoCuenta, fechaEliminacion,
 * especialidad...) que un cliente no autenticado nunca debe poder fijar. Si el registro
 * aceptase directamente un {@code UsuarioDTO} como cuerpo de la petición, Jackson
 * asignaría cualquiera de esos campos que el cliente incluyera en el JSON -- en
 * particular {@code id}, que al mapearse a la entidad y guardarse permitiría a un
 * atacante no autenticado sobrescribir una cuenta ya existente cuyo UUID conociera
 * (JPA trata un id no nulo como "entidad existente" y hace un merge/actualización en
 * vez de crear una fila nueva). Este DTO solo declara los campos que el formulario de
 * registro necesita.
 */
@Data
public class RegistroUsuarioRequest {
    private String nombre;
    private String apellido1;
    private String apellido2;
    private String nif;
    private String email;
    private String password;
    private String telefono;
    private LocalDateTime fechaNacimiento;

    /**
     * Consentimiento explícito del interesado para el tratamiento de sus datos de salud
     * (RGPD art. 9.2.a). El alta se rechaza si no es {@code true}: es un acto afirmativo
     * inequívoco, separado del resto de campos del formulario.
     */
    private Boolean aceptaTratamientoDatos;
}
