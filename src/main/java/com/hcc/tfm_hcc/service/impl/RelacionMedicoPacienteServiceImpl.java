package com.hcc.tfm_hcc.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.exception.RelacionMedicoPacienteNoEncontradaException;
import com.hcc.tfm_hcc.exception.UsuarioNoEncontradoException;
import com.hcc.tfm_hcc.exception.UsuarioSinPermisoException;
import com.hcc.tfm_hcc.model.AuditoriaCambio;
import com.hcc.tfm_hcc.model.MedicoPaciente;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.MedicoPacienteRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.AuditoriaCambioService;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;
import com.hcc.tfm_hcc.service.RelacionMedicoPacienteService;
import com.hcc.tfm_hcc.util.LogMaskUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de gestión de la relación asistencial médico-paciente.
 *
 * @author Sistema HCC
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RelacionMedicoPacienteServiceImpl implements RelacionMedicoPacienteService {

    private static final String TABLA_MEDICO_PACIENTE = "medico_paciente";
    private static final String TIPO_CAMBIO_REVOCACION = "REVOCACION_RELACION";
    private static final String RAZON_REVOCA_MEDICO = "Revocación de la relación asistencial iniciada por el médico";
    private static final String RAZON_REVOCA_PACIENTE = "Revocación de la relación asistencial iniciada por el paciente";

    private final MedicoPacienteRepository medicoPacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final HmacSearchIndexService hmacSearchIndexService;
    private final AuditoriaCambioService auditoriaCambioService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarMedicosActivos(String nifPaciente) {
        Usuario paciente = buscarUsuarioPorNif(nifPaciente);
        return medicoPacienteRepository.findByPacienteIdAndEstado(paciente.getId(), MedicoPaciente.ESTADO_ACTIVA)
            .stream()
            .map(MedicoPaciente::getMedico)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Usuario verificarAccesoMedicoActivo(String nifMedico, String nifPaciente) {
        Usuario medico = buscarUsuarioPorNif(nifMedico);
        Usuario paciente = buscarUsuarioPorNif(nifPaciente);

        if (Usuario.ESTADO_CUENTA_SUSPENDIDO.equals(paciente.getEstadoCuenta())) {
            throw new UsuarioSinPermisoException(ErrorMessages.ERROR_TRATAMIENTO_LIMITADO);
        }

        boolean autorizado = medicoPacienteRepository.existsByMedicoIdAndPacienteIdAndEstado(
            medico.getId(), paciente.getId(), MedicoPaciente.ESTADO_ACTIVA);
        if (!autorizado) {
            throw new UsuarioSinPermisoException(ErrorMessages.ERROR_ACCESO_DENEGADO);
        }
        return paciente;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MedicoPaciente revocarRelacion(String nifMedico, String nifPaciente, IniciadorRevocacion iniciadaPor) {
        Usuario medico = buscarUsuarioPorNif(nifMedico);
        Usuario paciente = buscarUsuarioPorNif(nifPaciente);

        MedicoPaciente relacion = medicoPacienteRepository
            .findByMedicoIdAndPacienteIdAndEstado(medico.getId(), paciente.getId(), MedicoPaciente.ESTADO_ACTIVA)
            .stream()
            .findFirst()
            .orElseThrow(() -> new RelacionMedicoPacienteNoEncontradaException(ErrorMessages.ERROR_RELACION_MEDICO_PACIENTE_NO_ACTIVA));

        relacion.setEstado(MedicoPaciente.ESTADO_REVOCADA);
        MedicoPaciente revocada = medicoPacienteRepository.save(relacion);

        registrarAuditoria(revocada, medico, paciente, iniciadaPor);

        log.info("Relación médico-paciente {} revocada (iniciada por {}): médico {} / paciente {}",
            revocada.getId(), iniciadaPor,
            LogMaskUtil.enmascarar(nifMedico), LogMaskUtil.enmascarar(nifPaciente));

        return revocada;
    }

    private void registrarAuditoria(MedicoPaciente relacion, Usuario medico, Usuario paciente, IniciadorRevocacion iniciadaPor) {
        boolean laIniciaElMedico = iniciadaPor == IniciadorRevocacion.MEDICO;
        String idQuienRevoca = laIniciaElMedico ? medico.getId().toString() : paciente.getId().toString();
        String razon = laIniciaElMedico ? RAZON_REVOCA_MEDICO : RAZON_REVOCA_PACIENTE;

        auditoriaCambioService.registrarCambio(
            idQuienRevoca,
            paciente.getId().toString(),
            medico.getId().toString(),
            TIPO_CAMBIO_REVOCACION,
            TABLA_MEDICO_PACIENTE,
            relacion.getId().toString(),
            MedicoPaciente.ESTADO_ACTIVA,
            MedicoPaciente.ESTADO_REVOCADA,
            AuditoriaCambio.TipoOperacion.UPDATE,
            razon
        );
    }

    private Usuario buscarUsuarioPorNif(String nif) {
        if (nif == null || nif.isBlank()) {
            throw new UsuarioNoEncontradoException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO);
        }
        return usuarioRepository.findByNifHash(hmacSearchIndexService.indexar(nif))
            .orElseThrow(() -> new UsuarioNoEncontradoException(ErrorMessages.ERROR_USUARIO_NO_ENCONTRADO));
    }
}
