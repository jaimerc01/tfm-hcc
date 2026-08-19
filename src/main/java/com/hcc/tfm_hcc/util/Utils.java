package com.hcc.tfm_hcc.util;

import java.util.Date;

public class Utils {

    private Utils() {
        // Constructor privado para evitar instanciación
    }

    // Método para obtener la hora actual, utilizado en auditoría
    public static Date obtenerHoraActual() {
        return new Date();
    }
}
