package com.hcc.tfm_hcc.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@Entity
@Table(name = "perfil_usuario")
public class PerfilUsuario extends BaseEntity{

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "id_perfil", referencedColumnName = "id", updatable = false)
    private Perfil perfil;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id", updatable = false)
    private Usuario usuario;   

}
