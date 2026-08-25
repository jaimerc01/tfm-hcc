package com.hcc.tfm_hcc.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hcc.tfm_hcc.model.DatoClinico;
import com.hcc.tfm_hcc.model.HistorialClinico;

/**
 * Repositorio para la gestión de datos clínicos en el sistema HCC.
 * Proporciona operaciones de persistencia y consulta para información clínica
 * asociada a historiales médicos específicos.
 * 
 * <p>Este repositorio permite:</p>
 * <ul>
 *   <li>Consultar datos clínicos por historial médico</li>
 *   <li>Eliminar datos clínicos por tipo y historial</li>
 *   <li>Mantener la integridad entre datos clínicos e historiales</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface DatoClinicoRepository extends JpaRepository<DatoClinico, UUID> {
    
    /**
     * Busca todos los datos clínicos cuantitativos asociados a un historial clínico específico
     * (análisis de sangre, signos vitales, análisis de orina).
     *
     * @param historial HistorialClinico del cual obtener los datos clínicos
     * @return Lista de DatoClinico asociados al historial especificado
     */
    List<DatoClinico> findByHistorialClinico(HistorialClinico historial);

    /**
     * Busca todos los datos clínicos asociados a un historial que pertenezcan a un conjunto de tipos.
     * Útil para obtener múltiples tipos de análisis de sangre de una vez.
     *
     * <p>El tipo está cifrado de forma no determinista, así que el filtro se aplica sobre
     * {@code tipoHash}, no sobre el tipo cifrado (ver {@link com.hcc.tfm_hcc.service.HmacSearchIndexService}).</p>
     *
     * @param historial HistorialClinico del cual obtener los datos clínicos
     * @param tipoHashes Lista de índices de búsqueda (HMAC) de los tipos de dato clínico a buscar
     * @return Lista de DatoClinico que coinciden con los tipos especificados
     */
    List<DatoClinico> findByHistorialClinicoAndTipoHashIn(HistorialClinico historial, List<String> tipoHashes);
}
