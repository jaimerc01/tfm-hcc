package com.hcc.tfm_hcc.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hcc.tfm_hcc.model.Notificacion;
import com.hcc.tfm_hcc.model.Usuario;

public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {
    List<Notificacion> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario);
    Page<Notificacion> findByUsuario(Usuario usuario, Pageable pageable);
    long countByUsuarioAndLeidaFalse(Usuario usuario);
}
