package com.hcc.tfm_hcc.constants;

import java.util.List;

/**
 * Catálogo centralizado de los valores de {@code tipo} (dato_clinico.tipo) que pertenecen
 * a cada dominio de datos clínicos cuantitativos: análisis de sangre, signos vitales y
 * análisis de orina.
 *
 * <p>Se usa tanto para decidir qué registros existentes se sustituyen en un reemplazo
 * completo (PUT) como para clasificar los {@code DatoClinico} recuperados en los distintos
 * apartados del {@code HistorialClinicoDTO}. Los nombres se mantienen sin tildes en los
 * tipos nuevos (signos vitales / orina) para que coincidan exactamente, ignorando mayúsculas,
 * con los nombres de {@code Rango} correspondientes y así garantizar la asignación automática
 * del rango de referencia.</p>
 */
public final class TiposDatoClinico {

    private TiposDatoClinico() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no debe ser instanciada");
    }

    public static final List<String> ANALISIS_SANGRE = List.of(
            "Hemoglobina", "Glucosa", "Colesterol", "Colesterol Total",
            "Triglicéridos", "Creatinina", "Hematocrito",
            "Leucocitos", "Plaquetas", "Transaminasas ALT",
            "Transaminasas AST", "Bilirrubina Total", "Urea",
            "Eritrocitos", "Colesterol LDL", "Colesterol HDL",
            // Versiones con unidades por compatibilidad con datos antiguos
            "Hemoglobina (g/dL)", "Glucosa (mg/dL)", "Colesterol Total (mg/dL)",
            "Triglicéridos (mg/dL)", "Creatinina (mg/dL)", "Hematocrito (%)",
            "Leucocitos (10³/µL)", "Plaquetas (10³/µL)", "Transaminasas ALT (U/L)",
            "Transaminasas AST (U/L)", "Bilirrubina Total (mg/dL)", "Urea (mg/dL)"
    );

    public static final List<String> SIGNOS_VITALES = List.of(
            "Frecuencia Cardiaca", "Presion Arterial Sistolica",
            "Presion Arterial Diastolica", "IMC"
    );

    public static final List<String> ANALISIS_ORINA = List.of(
            "PH Orina"
    );
}
