package com.hcc.tfm_hcc.service;

import com.hcc.tfm_hcc.model.Rango;

import java.util.List;

/**
 * Servicio que encapsula operaciones sobre la entidad Rango.
 */
public interface RangoService {

    /**
     * Lista todos los rangos almacenados en el sistema.
     *
     * @return Lista de entidades `Rango`
     */
    List<Rango> listarTodos();
}
