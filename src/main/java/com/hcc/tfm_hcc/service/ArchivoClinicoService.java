package com.hcc.tfm_hcc.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.model.ArchivoClinico;

/**
 * Servicio para la gestión de archivos clínicos.
 * 
 * <p>Este servicio proporciona operaciones para la gestión segura de archivos
 * clínicos de los usuarios, incluyendo subida, descarga, listado y eliminación.</p>
 * 
 * <p>Características principales:</p>
 * <ul>
 *   <li>Control de acceso basado en el usuario autenticado</li>
 *   <li>Validaciones de seguridad para archivos</li>
 *   <li>Almacenamiento seguro en el sistema de archivos</li>
 *   <li>Gestión del ciclo de vida de archivos</li>
 * </ul>
 * 
 * @author Sistema HCC
 * @version 1.0
 * @since 2024
 */
public interface ArchivoClinicoService {
    
    /**
     * Lista todos los archivos clínicos del usuario autenticado.
     *
     * @return lista de archivos clínicos ordenados por fecha de creación descendente
     */
    List<ArchivoClinico> listMine();
    
    /**
     * Sube un archivo clínico para el usuario autenticado.
     *
     * @param file el archivo a subir
     * @return el registro del archivo clínico creado
     * @throws IOException si ocurre un error durante el almacenamiento
     * @throws IllegalArgumentException si el archivo es inválido
     */
    ArchivoClinico uploadMine(MultipartFile file) throws IOException;
    
    /**
     * Obtiene el recurso de un archivo clínico del usuario autenticado.
     *
     * @param id el ID del archivo clínico
     * @return el recurso del archivo
     * @throws IllegalArgumentException si el archivo no existe o no pertenece al usuario
     */
    Resource getMineResource(UUID id);
    
    /**
     * Elimina un archivo clínico del usuario autenticado.
     *
     * @param id el ID del archivo clínico a eliminar
     * @throws IOException si ocurre un error durante la eliminación
     * @throws IllegalArgumentException si el archivo no existe o no pertenece al usuario
     */
    void deleteMine(UUID id) throws IOException;
}
