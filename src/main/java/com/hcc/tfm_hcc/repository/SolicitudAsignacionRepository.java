package com.hcc.tfm_hcc.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.hcc.tfm_hcc.model.SolicitudAsignacion;

public interface SolicitudAsignacionRepository extends CrudRepository<SolicitudAsignacion, UUID> {
	boolean existsByMedicoNifAndPacienteNifAndEstado(String medicoNif, String pacienteNif, String estado);
	List<SolicitudAsignacion> findByMedicoNifAndEstado(String medicoNif, String estado);
	List<SolicitudAsignacion> findByPacienteNifAndEstado(String pacienteNif, String estado);
	// devuelve todo el historial de solicitudes recibidas por un paciente, ordenado por fecha de creación descendente
	List<SolicitudAsignacion> findByPacienteNifOrderByFechaCreacionDesc(String pacienteNif);

	// devuelve todo el historial de solicitudes enviadas por un médico, ordenado por fecha de creación descendente
	List<SolicitudAsignacion> findByMedicoNifOrderByFechaCreacionDesc(String medicoNif);
}
