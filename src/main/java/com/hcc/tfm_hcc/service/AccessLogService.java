package com.hcc.tfm_hcc.service;

import com.hcc.tfm_hcc.model.AccessLog;

public interface AccessLogService {

    /*
     * Registra un nuevo acceso en el sistema.
     * @param log El objeto AccessLog que contiene la información del acceso.
     * 
     */
    public void log(AccessLog log);
}
