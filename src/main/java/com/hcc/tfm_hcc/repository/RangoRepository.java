package com.hcc.tfm_hcc.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hcc.tfm_hcc.model.Rango;

/**
 * Repositorio para operaciones de datos sobre la entidad Rango.
 * 
 * <p>Este repositorio proporciona métodos para la gestión de rangos de valores
 * médicos y parámetros clínicos, permitiendo la consulta y manipulación de
 * límites normales para diferentes tipos de análisis.</p>
 * 
 * <p>Funcionalidades principales:</p>
 * <ul>
 *   <li>Consulta de rangos por nombre de parámetro médico</li>
 *   <li>Búsqueda case-insensitive para mayor flexibilidad</li>
 *   <li>Operaciones CRUD estándar para gestión de rangos</li>
 *   <li>Consultas optimizadas para integración con datos clínicos</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 * @see Rango
 * @see JpaRepository
 */
@Repository
public interface RangoRepository extends JpaRepository<Rango, UUID> {

    /**
     * Busca un rango por su nombre exacto.
     * 
     * <p>Este método permite encontrar rangos utilizando el nombre exacto
     * del parámetro médico. Es útil para asociar datos clínicos con sus
     * rangos de referencia correspondientes.</p>
     * 
     * @param nombre Nombre exacto del rango a buscar
     * @return Optional<Rango> El rango encontrado o vacío si no existe
     * 
     * @example
     * <pre>
     * Optional<Rango> rango = rangoRepository.findByNombre("COLESTEROL TOTAL");
     * if (rango.isPresent()) {
     *     // Usar el rango encontrado
     * }
     * </pre>
     */
    Optional<Rango> findByNombre(String nombre);

    /**
     * Busca un rango por su nombre ignorando mayúsculas y minúsculas.
     * 
     * <p>Método más flexible que permite encontrar rangos sin preocuparse
     * por la capitalización del nombre. Útil cuando los datos pueden venir
     * con diferentes formatos de capitalización.</p>
     * 
     * @param nombre Nombre del rango a buscar (case-insensitive)
     * @return Optional<Rango> El rango encontrado o vacío si no existe
     * 
     * @example
     * <pre>
     * // Todos estos encontrarán el mismo rango:
     * findByNombreIgnoreCase("colesterol total");
     * findByNombreIgnoreCase("COLESTEROL TOTAL"); 
     * findByNombreIgnoreCase("Colesterol Total");
     * </pre>
     */
    Optional<Rango> findByNombreIgnoreCase(String nombre);

    /**
     * Busca rangos por nombre utilizando búsqueda parcial (contiene).
     * 
     * <p>Permite búsquedas más flexibles donde el nombre puede contener
     * la cadena especificada en cualquier posición, ignorando mayúsculas.</p>
     * 
     * @param nombre Parte del nombre a buscar
     * @return Optional<Rango> El primer rango que contenga el texto o vacío
     * 
     * @example
     * <pre>
     * // Puede encontrar "COLESTEROL TOTAL" buscando solo "COLESTEROL"
     * Optional<Rango> rango = rangoRepository.findByNombreContainingIgnoreCase("colesterol");
     * </pre>
     */
    @Query("SELECT r FROM Rango r WHERE LOWER(r.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    Optional<Rango> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Verifica si existe un rango con el nombre especificado.
     * 
     * <p>Método de conveniencia para verificar la existencia de un rango
     * sin necesidad de recuperar el objeto completo.</p>
     * 
     * @param nombre Nombre del rango a verificar
     * @return boolean true si existe, false en caso contrario
     */
    boolean existsByNombre(String nombre);

    /**
     * Verifica si existe un rango con el nombre especificado (case-insensitive).
     * 
     * @param nombre Nombre del rango a verificar (ignorando mayúsculas)
     * @return boolean true si existe, false en caso contrario
     */
    boolean existsByNombreIgnoreCase(String nombre);
}
