package com.hcc.tfm_hcc.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class UpdateUsuarioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre es demasiado largo")
    private String nombre;

    @NotBlank(message = "El primer apellido es obligatorio")
    @Size(max = 100, message = "El primer apellido es demasiado largo")
    private String apellido1;

    @Size(max = 100, message = "El segundo apellido es demasiado largo")
    private String apellido2;

    @NotBlank(message = "El NIF es obligatorio")
    @Pattern(regexp = "^[0-9A-Za-z]{6,15}$", message = "Formato de NIF inválido")
    private String nif;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    @Pattern(regexp = "^[0-9+\\-() ]{0,20}$", message = "Formato de teléfono inválido")
    private String telefono;

    private LocalDateTime fechaNacimiento; // Validación adicional de rango se podría añadir
}
