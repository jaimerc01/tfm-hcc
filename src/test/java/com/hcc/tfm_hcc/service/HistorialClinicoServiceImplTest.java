package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.model.DatoClinico;
import com.hcc.tfm_hcc.model.HistorialClinico;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.DatoClinicoRepository;
import com.hcc.tfm_hcc.repository.HistorialClinicoRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.impl.HistorialClinicoServiceImpl;

public class HistorialClinicoServiceImplTest {

    @Mock
    UsuarioFacade usuarioFacade;
    @Mock
    UsuarioRepository usuarioRepository;
    @Mock
    HistorialClinicoRepository historiaRepo;
    @Mock
    DatoClinicoRepository datoRepo;

    @InjectMocks
    HistorialClinicoServiceImpl svc;

    Usuario u;
    HistorialClinico h;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        u = new Usuario();
        u.setId(UUID.randomUUID());
        h = new HistorialClinico();
        h.setId(UUID.randomUUID());
        h.setUsuario(u);
    }

    @Test
    void borrarDatoClinico_checksOwnershipAndDeletes() {
        var userDto = new com.hcc.tfm_hcc.dto.UsuarioDTO();
        userDto.setId(u.getId().toString());
        when(usuarioFacade.getUsuarioActual()).thenReturn(userDto);
        when(usuarioRepository.findById(u.getId())).thenReturn(Optional.of(u));
        when(historiaRepo.findByUsuario(u)).thenReturn(Optional.of(h));

        DatoClinico d = new DatoClinico();
        d.setId(UUID.randomUUID());
        d.setHistorialClinico(h);
        when(datoRepo.findById(d.getId())).thenReturn(Optional.of(d));

        svc.borrarDatoClinico(d.getId());

        verify(datoRepo).delete(d);
    }

    @Test
    void editarAntecedente_replacesEntry() {
        var userDto = new com.hcc.tfm_hcc.dto.UsuarioDTO();
        userDto.setId(u.getId().toString());
        when(usuarioFacade.getUsuarioActual()).thenReturn(userDto);
        when(usuarioRepository.findById(u.getId())).thenReturn(Optional.of(u));
        when(historiaRepo.findByUsuario(u)).thenReturn(Optional.of(h));

        String existing = "[2025-01-01 10:00] primera entrada\n\n[2025-02-01 11:00] segunda entrada";
        h.setAntecedentesFamiliares(existing);

        var res = svc.editarAntecedente(1, "modificada segunda");

        assertTrue(res.getAntecedentesFamiliares().contains("modificada segunda"));
        verify(historiaRepo).save(any(HistorialClinico.class));
    }
}
