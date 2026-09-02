package com.hcc.tfm_hcc.facade.impl;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.constants.ErrorMessages;
import com.hcc.tfm_hcc.converter.MedicoResumenConverter;
import com.hcc.tfm_hcc.dto.MedicoResumenDTO;
import com.hcc.tfm_hcc.exception.UsuarioNoAutenticadoException;
import com.hcc.tfm_hcc.facade.NotificacionFacade;
import com.hcc.tfm_hcc.facade.RelacionMedicoPacienteFacade;
import com.hcc.tfm_hcc.model.MedicoPaciente;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.service.RelacionMedicoPacienteService;
import com.hcc.tfm_hcc.service.RelacionMedicoPacienteService.IniciadorRevocacion;
import com.hcc.tfm_hcc.util.LogMaskUtil;
import com.hcc.tfm_hcc.util.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación de la fachada de gestión de la relación asistencial médico-paciente.
 *
 * <p>Resuelve al usuario autenticado a partir del contexto de seguridad, delega la
 * lógica de dominio en {@link RelacionMedicoPacienteService} y, tras revocar una
 * relación, emite el aviso a la otra parte a través de {@link NotificacionFacade}
 * (un fallo al notificar no invalida la revocación ya efectuada).</p>
 *
 * @author Sistema HCC
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RelacionMedicoPacienteFacadeImpl implements RelacionMedicoPacienteFacade {

    private static final String MENSAJE_MEDICO_REVOCO_PREFIJO = "El médico ";
    private static final String MENSAJE_MEDICO_REVOCO_SUFIJO = " ha finalizado la relación asistencial contigo";
    private static final String MENSAJE_PACIENTE_REVOCO_PREFIJO = "El paciente ";
    private static final String MENSAJE_PACIENTE_REVOCO_SUFIJO = " ha finalizado vuestra relación asistencial";

    private final RelacionMedicoPacienteService relacionMedicoPacienteService;
    private final NotificacionFacade notificacionFacade;
    private final MedicoResumenConverter medicoResumenConverter;

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public List<MedicoResumenDTO> listarMisMedicos() {
        String nifPaciente = nifUsuarioAutenticado();
        List<Usuario> medicos = relacionMedicoPacienteService.listarMedicosActivos(nifPaciente);
        log.info("Médicos asignados obtenidos para el usuario autenticado: {} registros", medicos.size());
        return medicoResumenConverter.toDtoList(medicos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("isAuthenticated()")
    public void desasignarMiMedico(String nifMedico) {
        String nifPaciente = nifUsuarioAutenticado();
        log.info("El paciente autenticado revoca la relación con el médico {}", LogMaskUtil.enmascarar(nifMedico));

        MedicoPaciente revocada = relacionMedicoPacienteService.revocarRelacion(nifMedico, nifPaciente, IniciadorRevocacion.PACIENTE);
        notificar(revocada.getMedico(), nombreVisible(revocada.getPaciente()),
                MENSAJE_PACIENTE_REVOCO_PREFIJO, MENSAJE_PACIENTE_REVOCO_SUFIJO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public void desasignarMiPaciente(String nifPaciente) {
        String nifMedico = nifUsuarioAutenticado();
        log.info("El médico autenticado revoca la relación con el paciente {}", LogMaskUtil.enmascarar(nifPaciente));

        MedicoPaciente revocada = relacionMedicoPacienteService.revocarRelacion(nifMedico, nifPaciente, IniciadorRevocacion.MEDICO);
        notificar(revocada.getPaciente(), nombreVisible(revocada.getMedico()),
                MENSAJE_MEDICO_REVOCO_PREFIJO, MENSAJE_MEDICO_REVOCO_SUFIJO);
    }

    private String nifUsuarioAutenticado() {
        String nif = SecurityUtils.getCurrentUserNif();
        if (nif == null) {
            throw new UsuarioNoAutenticadoException(ErrorMessages.ERROR_USUARIO_NO_AUTENTICADO);
        }
        return nif;
    }

    private void notificar(Usuario destinatario, String nombreQuienRevoca, String prefijo, String sufijo) {
        if (destinatario == null) {
            return;
        }
        try {
            notificacionFacade.crearNotificacionParaUsuario(destinatario.getNif(), prefijo + nombreQuienRevoca + sufijo);
        } catch (Exception e) {
            log.warn("No se pudo notificar la revocación de la relación asistencial: {}", e.getMessage());
        }
    }

    private String nombreVisible(Usuario usuario) {
        if (usuario == null) {
            return "";
        }
        return usuario.getNombre() != null ? usuario.getNombre() : usuario.getNif();
    }
}
