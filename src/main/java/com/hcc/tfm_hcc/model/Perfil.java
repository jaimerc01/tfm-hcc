package com.hcc.tfm_hcc.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@Entity
@Table(name = "perfil")
public class Perfil extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    @Column(name = "rol", nullable = false, unique = true)
    private String rol;

}
