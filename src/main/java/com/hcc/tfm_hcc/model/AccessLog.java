package com.hcc.tfm_hcc.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "log_acceso")
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Getter @Setter
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Getter @Setter
    @Column(name = "id_usuario")
    private String usuarioId; // UUID en texto

    @Getter @Setter
    @Column(name = "metodo", length = 10)
    private String metodo;

    @Getter @Setter
    @Column(name = "ruta", length = 500)
    private String ruta;

    @Getter @Setter
    @Column(name = "estado")
    private Integer estado;

    @Getter @Setter
    @Column(name = "ip", length = 50)
    private String ip;

    @Getter @Setter
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Getter @Setter
    @Column(name = "duracion_ms")
    private Long duracionMs;

    public UUID getId() { return id; }
}
