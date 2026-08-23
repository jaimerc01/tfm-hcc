package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.hcc.tfm_hcc.converter.AlergiaConverter;
import com.hcc.tfm_hcc.converter.AntecedenteClinicoConverter;
import com.hcc.tfm_hcc.converter.HistorialClinicoConverter;
import com.hcc.tfm_hcc.dto.AntecedenteClinicoDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.model.AntecedenteClinico;
import com.hcc.tfm_hcc.model.AuditoriaCambio;
import com.hcc.tfm_hcc.model.DatoClinico;
import com.hcc.tfm_hcc.model.HistorialClinico;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.AlergiaRepository;
import com.hcc.tfm_hcc.repository.AntecedenteClinicoRepository;
import com.hcc.tfm_hcc.repository.DatoClinicoRepository;
import com.hcc.tfm_hcc.repository.HistorialClinicoRepository;
import com.hcc.tfm_hcc.repository.RangoRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.impl.HistorialClinicoServiceImpl;

class HistorialClinicoServiceImplTest {

    @Mock
    UsuarioFacade usuarioFacade;
    @Mock
    UsuarioRepository usuarioRepository;
    @Mock
    HistorialClinicoRepository historiaRepo;
    @Mock
    DatoClinicoRepository datoRepo;
    @Mock
    AlergiaRepository alergiaRepository;
    @Mock
    AntecedenteClinicoRepository antecedenteClinicoRepository;
    @Mock
    RangoRepository rangoRepository;
    @Mock
    AuditoriaCambioService auditoriaCambioService;
    @Mock
    HistorialClinicoConverter historialClinicoConverter;
    @Mock
    AlergiaConverter alergiaConverter;
    @Mock
    AntecedenteClinicoConverter antecedenteClinicoConverter;

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

    private void mockUsuarioAutenticado() {
        var userDto = new com.hcc.tfm_hcc.dto.UsuarioDTO();
        UUID userId = u.getId();
        if (userId != null) {
            userDto.setId(userId.toString());
        }
        when(usuarioFacade.getUsuarioActual()).thenReturn(userDto);
        if (userId != null) {
            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(u));
        }
        when(historiaRepo.findByUsuario(u)).thenReturn(Optional.of(h));
    }

    @SuppressWarnings("null")
    @Test
    void borrarDatoClinico_checksOwnershipAndDeletes() {
        mockUsuarioAutenticado();

        DatoClinico d = new DatoClinico();
        UUID datoId = UUID.randomUUID();
        d.setId(datoId);
        d.setHistorialClinico(h);
        when(datoRepo.findById(datoId)).thenReturn(Optional.of(d));

        svc.borrarDatoClinico(datoId);

        verify(datoRepo).delete(d);
    }

    @SuppressWarnings("null")
    @Test
    void editarAntecedente_updatesDescripcionYCategoria() {
        mockUsuarioAutenticado();

        UUID antecedenteId = UUID.randomUUID();
        AntecedenteClinico existente = new AntecedenteClinico();
        existente.setId(antecedenteId);
        existente.setHistorialClinico(h);
        existente.setCategoria(AntecedenteClinico.Categoria.PERSONAL);
        existente.setDescripcion("descripcion anterior");
        when(antecedenteClinicoRepository.findById(antecedenteId)).thenReturn(Optional.of(existente));
        when(antecedenteClinicoConverter.parseCategoria(anyString())).thenReturn(AntecedenteClinico.Categoria.FAMILIAR);
        when(datoRepo.findByHistorialClinico(h)).thenReturn(List.of());
        when(alergiaRepository.findByHistorialClinico(h)).thenReturn(List.of());
        when(antecedenteClinicoRepository.findByHistorialClinico(h)).thenReturn(List.of(existente));
        when(historialClinicoConverter.toDto(any(), any(), any(), any())).thenReturn(new HistorialClinicoDTO());

        AntecedenteClinicoDTO dto = new AntecedenteClinicoDTO();
        dto.setCategoria("FAMILIAR");
        dto.setDescripcion("descripcion nueva");

        var res = svc.editarAntecedente(antecedenteId, dto);

        assertNotNull(res);
        assertEquals("descripcion nueva", existente.getDescripcion());
        assertEquals(AntecedenteClinico.Categoria.FAMILIAR, existente.getCategoria());
        verify(antecedenteClinicoRepository).save(existente);
        verify(auditoriaCambioService).registrarCambio(
            any(), any(), any(), any(), any(), any(), any(), any(), any(AuditoriaCambio.TipoOperacion.class), any());
    }
}
