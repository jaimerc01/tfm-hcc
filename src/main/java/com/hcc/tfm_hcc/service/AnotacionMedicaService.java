package com.hcc.tfm_hcc.service;

import java.time.LocalDateTime;
import java.util.List;

import com.hcc.tfm_hcc.model.AnotacionMedica;

/**
 * Servicio para la gestión de anotaciones médicas en el sistema HCC.
 * Una anotación médica es una observación de texto libre que un médico registra
 * sobre un paciente con el que mantiene una relación de asignación activa; el
 * paciente puede consultar en todo momento las anotaciones que ha recibido.
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
public interface AnotacionMedicaService {

    /**
     * Crea una anotación médica de un médico sobre un paciente.
     * Exige que exista una relación médico-paciente activa y que el paciente
     * no haya limitado el tratamiento de sus datos (art. 18 RGPD). El aviso al
     * paciente lo emite la capa de fachada tras crear la anotación.
     *
     * @param nifMedico NIF del médico autor de la anotación
     * @param nifPaciente NIF del paciente sobre el que se escribe la anotación
     * @param mensaje contenido de la anotación
     * @return AnotacionMedica creada
     * @throws IllegalArgumentException si el paciente no existe o el mensaje está vacío
     * @throws IllegalStateException si el médico no existe, no hay relación médico-paciente
     *         activa, o el paciente ha limitado el tratamiento de sus datos
     */
    AnotacionMedica crearAnotacion(String nifMedico, String nifPaciente, String mensaje);

    /**
     * Lista las anotaciones médicas recibidas por un paciente, de la más reciente a la
     * más antigua, con filtro opcional por médico autor y por rango de fechas.
     *
     * @param nifPaciente NIF del paciente cuyas anotaciones se consultan
     * @param nifMedicoFiltro NIF del médico por el que filtrar (opcional, puede ser {@code null})
     * @param desde fecha de inicio del rango, inclusive (opcional, puede ser {@code null})
     * @param hasta fecha de fin del rango, inclusive (opcional, puede ser {@code null})
     * @return lista de anotaciones que cumplen los filtros indicados; vacía si el médico
     *         del filtro no existe
     * @throws IllegalArgumentException si el paciente no existe
     */
    List<AnotacionMedica> listarAnotacionesPaciente(String nifPaciente, String nifMedicoFiltro, LocalDateTime desde, LocalDateTime hasta);

    /**
     * Lista las anotaciones que un médico ha escrito sobre un paciente concreto, de la
     * más reciente a la más antigua. A diferencia de {@link #listarAnotacionesPaciente},
     * pensada para que el paciente consulte lo que ha recibido, esta comprueba el acceso
     * del médico: exige una relación médico-paciente activa y que el paciente no haya
     * limitado el tratamiento de sus datos.
     *
     * @param nifMedico NIF del médico autenticado
     * @param nifPaciente NIF del paciente
     * @return lista de anotaciones escritas por ese médico sobre ese paciente
     * @throws IllegalArgumentException si el paciente no existe
     * @throws IllegalStateException si el médico no existe, no hay relación médico-paciente
     *         activa, o el paciente ha limitado el tratamiento de sus datos
     */
    List<AnotacionMedica> listarAnotacionesEscritasPorMedico(String nifMedico, String nifPaciente);
}
