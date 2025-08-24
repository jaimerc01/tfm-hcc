package com.hcc.tfm_hcc.repository;

import com.hcc.tfm_hcc.model.SolicitudAsignacion;
import org.springframework.data.repository.CrudRepository;

public interface SolicitudAsignacionRepository extends CrudRepository<SolicitudAsignacion, Long> {
	boolean existsByMedicoNifAndPacienteNifAndEstado(String medicoNif, String pacienteNif, String estado);
	java.util.List<SolicitudAsignacion> findByMedicoNifAndEstado(String medicoNif, String estado);
	java.util.List<SolicitudAsignacion> findByPacienteNifAndEstado(String pacienteNif, String estado);
}
