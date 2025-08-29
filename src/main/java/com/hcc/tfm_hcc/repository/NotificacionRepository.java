package com.hcc.tfm_hcc.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hcc.tfm_hcc.model.Notificacion;
import com.hcc.tfm_hcc.model.Usuario;

/**
 * Repositorio para la gestión de notificaciones en el sistema HCC.
 * Proporciona operaciones de persistencia y consulta para las notificaciones
 * dirigidas a usuarios específicos del sistema.
 * 
 * <p>Este repositorio permite:</p>
 * <ul>
 *   <li>Consultar notificaciones por usuario con ordenamiento cronológico</li>
 *   <li>Implementar paginación para grandes volúmenes de notificaciones</li>
 *   <li>Contar notificaciones no leídas para indicadores visuales</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {
    
    /**
     * Busca todas las notificaciones de un usuario específico.
     * Los resultados se ordenan por fecha de creación de forma descendente (más recientes primero).
     * 
     * @param usuario Usuario del cual obtener las notificaciones
     * @return Lista de Notificacion del usuario ordenadas por fecha de creación descendente
     */
    List<Notificacion> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario);
    
    /**
     * Busca las notificaciones de un usuario con soporte para paginación.
     * Permite manejar eficientemente grandes volúmenes de notificaciones.
     * 
     * @param usuario Usuario del cual obtener las notificaciones
     * @param pageable Configuración de paginación y ordenamiento
     * @return Page con las notificaciones del usuario según la configuración de paginación
     */
    Page<Notificacion> findByUsuario(Usuario usuario, Pageable pageable);
    
    /**
     * Cuenta el número de notificaciones no leídas de un usuario específico.
     * Útil para mostrar indicadores de notificaciones pendientes en la interfaz.
     * 
     * @param usuario Usuario del cual contar las notificaciones no leídas
     * @return Número de notificaciones no leídas del usuario
     */
    long countByUsuarioAndLeidaFalse(Usuario usuario);
}
