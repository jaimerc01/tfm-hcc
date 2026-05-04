package com.hcc.tfm_hcc.dto;

import java.util.Date;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class UpdateUsuarioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "Nombre demasiado largo")
    private String nombre;

    @NotBlank(message = "El primer apellido es obligatorio")
    @Size(max = 100, message = "Primer apellido demasiado largo")
    private String apellido1;

    @Size(max = 100, message = "Segundo apellido demasiado largo")
    private String apellido2;

    @NotBlank(message = "El NIF es obligatorio")
    @Pattern(regexp = "^[0-9A-Za-z]{6,15}$", message = "Formato de NIF inválido")
    private String nif;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    @Pattern(regexp = "^[0-9+\\-() ]{0,20}$", message = "Formato de teléfono inválido")
    private String telefono;

    private Date fechaNacimiento; // Validación adicional de rango se podría añadir
}
