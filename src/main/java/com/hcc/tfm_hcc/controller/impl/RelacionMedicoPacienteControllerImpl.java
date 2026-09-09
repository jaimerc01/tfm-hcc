package com.hcc.tfm_hcc.controller.impl;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcc.tfm_hcc.constants.RestUrls;
import com.hcc.tfm_hcc.controller.RelacionMedicoPacienteController;
import com.hcc.tfm_hcc.dto.MedicoResumenDTO;
import com.hcc.tfm_hcc.facade.RelacionMedicoPacienteFacade;
import com.hcc.tfm_hcc.util.LogMaskUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del controlador REST de la relación asistencial médico-paciente.
 *
 * <p>El controlador se limita a validar la forma de la petición y delegar en
 * {@link RelacionMedicoPacienteFacade}.</p>
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping(RestUrls.RELACIONES_BASE)
@RequiredArgsConstructor
public class RelacionMedicoPacienteControllerImpl implements RelacionMedicoPacienteController {

    /** Facade para operaciones sobre la relación asistencial médico-paciente. */
    private final RelacionMedicoPacienteFacade relacionMedicoPacienteFacade;

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping(RestUrls.RELACIONES_MIS_MEDICOS)
    public ResponseEntity<List<MedicoResumenDTO>> listarMisMedicos() {
        log.info("Listando médicos asignados al usuario autenticado");
        return ResponseEntity.ok(relacionMedicoPacienteFacade.listarMisMedicos());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DeleteMapping(RestUrls.RELACIONES_MIS_MEDICOS_NIF)
    public ResponseEntity<Void> desasignarMiMedico(@PathVariable("nif") String nifMedico) {
        log.info("El paciente autenticado revoca la relación con el médico NIF: {}", LogMaskUtil.enmascarar(nifMedico));
        relacionMedicoPacienteFacade.desasignarMiMedico(nifMedico);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DeleteMapping(RestUrls.RELACIONES_MIS_PACIENTES_NIF)
    public ResponseEntity<Void> desasignarMiPaciente(@PathVariable("nif") String nifPaciente) {
        log.info("El médico autenticado revoca la relación con el paciente NIF: {}", LogMaskUtil.enmascarar(nifPaciente));
        relacionMedicoPacienteFacade.desasignarMiPaciente(nifPaciente);
        return ResponseEntity.noContent().build();
    }
}
