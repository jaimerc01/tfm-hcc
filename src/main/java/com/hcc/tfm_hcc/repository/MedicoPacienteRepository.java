package com.hcc.tfm_hcc.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hcc.tfm_hcc.model.MedicoPaciente;

@Repository
public interface MedicoPacienteRepository extends JpaRepository<MedicoPaciente, UUID> {
    boolean existsByMedicoIdAndPacienteId(UUID medicoId, UUID pacienteId);
}
