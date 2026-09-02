package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.hcc.tfm_hcc.converter.AlergiaConverter;
import com.hcc.tfm_hcc.converter.AntecedenteClinicoConverter;
import com.hcc.tfm_hcc.converter.HistorialClinicoConverter;
import com.hcc.tfm_hcc.dto.AlergiaDTO;
import com.hcc.tfm_hcc.dto.AntecedenteClinicoDTO;
import com.hcc.tfm_hcc.dto.DatoClinicoEntradaDTO;
import com.hcc.tfm_hcc.dto.HistorialClinicoDTO;
import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.model.Alergia;
import com.hcc.tfm_hcc.model.AntecedenteClinico;
import com.hcc.tfm_hcc.model.AuditoriaCambio;
import com.hcc.tfm_hcc.model.DatoClinico;
import com.hcc.tfm_hcc.model.HistorialClinico;
import com.hcc.tfm_hcc.model.Rango;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.AlergiaRepository;
import com.hcc.tfm_hcc.repository.AntecedenteClinicoRepository;
import com.hcc.tfm_hcc.repository.DatoClinicoRepository;
import com.hcc.tfm_hcc.repository.HistorialClinicoRepository;
import com.hcc.tfm_hcc.repository.MedicoPacienteRepository;
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
    MedicoPacienteRepository medicoPacienteRepository;
    @Mock
    AuditoriaCambioService auditoriaCambioService;
    @Mock
    HistorialClinicoConverter historialClinicoConverter;
    @Mock
    AlergiaConverter alergiaConverter;
    @Mock
    AntecedenteClinicoConverter antecedenteClinicoConverter;
    @Mock
    HmacSearchIndexService hmacSearchIndexService;
    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

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
        var userDto = new UsuarioDTO();
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

    private void mockConstruccionDtoVacia() {
        when(datoRepo.findByHistorialClinico(h)).thenReturn(List.of());
        when(alergiaRepository.findByHistorialClinico(h)).thenReturn(List.of());
        when(antecedenteClinicoRepository.findByHistorialClinico(h)).thenReturn(List.of());
        when(historialClinicoConverter.toDto(any(), any(), any(), any())).thenReturn(new HistorialClinicoDTO());
    }

    // ---- obtenerHistoriaUsuarioActual ----

    @Test
    void obtenerHistoriaUsuarioActual_conUsuarioAutenticado_construyeElDto() {
        mockUsuarioAutenticado();
        mockConstruccionDtoVacia();

        assertNotNull(svc.obtenerHistoriaUsuarioActual());
    }

    @Test
    void obtenerHistoriaUsuarioActual_sinUsuarioAutenticado_devuelveNull() {
        when(usuarioFacade.getUsuarioActual()).thenReturn(null);

        assertNull(svc.obtenerHistoriaUsuarioActual());
    }

    @Test
    void obtenerHistoriaUsuarioActual_conIdDeUsuarioVacio_devuelveNull() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId("  ");
        when(usuarioFacade.getUsuarioActual()).thenReturn(dto);

        assertNull(svc.obtenerHistoriaUsuarioActual());
    }

    @Test
    void obtenerHistoriaUsuarioActual_conUsuarioNoEncontradoEnBd_devuelveNull() {
        UsuarioDTO dto = new UsuarioDTO();
        UUID id = UUID.randomUUID();
        dto.setId(id.toString());
        when(usuarioFacade.getUsuarioActual()).thenReturn(dto);
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        assertNull(svc.obtenerHistoriaUsuarioActual());
    }

    @Test
    void obtenerHistoriaUsuarioActual_sinHistorialExistente_devuelveNull() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(u.getId().toString());
        when(usuarioFacade.getUsuarioActual()).thenReturn(dto);
        when(usuarioRepository.findById(u.getId())).thenReturn(Optional.of(u));
        when(historiaRepo.findByUsuario(u)).thenReturn(Optional.empty());

        assertNull(svc.obtenerHistoriaUsuarioActual());
    }

    // ---- obtenerHistorialPaciente ----

    @Test
    void obtenerHistorialPaciente_conRelacionActiva_devuelveElHistorialDelPaciente() {
        mockUsuarioAutenticado();

        Usuario paciente = new Usuario();
        paciente.setId(UUID.randomUUID());
        HistorialClinico historialPaciente = new HistorialClinico();
        historialPaciente.setId(UUID.randomUUID());
        historialPaciente.setUsuario(paciente);

        when(hmacSearchIndexService.indexar("12345678A")).thenReturn("hash-12345678A");
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(paciente));
        when(medicoPacienteRepository.existsByMedicoIdAndPacienteIdAndEstado(u.getId(), paciente.getId(), "ACTIVA"))
                .thenReturn(true);
        when(historiaRepo.findByUsuario(paciente)).thenReturn(Optional.of(historialPaciente));
        when(datoRepo.findByHistorialClinico(historialPaciente)).thenReturn(List.of());
        when(alergiaRepository.findByHistorialClinico(historialPaciente)).thenReturn(List.of());
        when(antecedenteClinicoRepository.findByHistorialClinico(historialPaciente)).thenReturn(List.of());
        when(historialClinicoConverter.toDto(any(), any(), any(), any())).thenReturn(new HistorialClinicoDTO());

        assertNotNull(svc.obtenerHistorialPaciente("12345678A"));
    }

    @Test
    void obtenerHistorialPaciente_conNifVacio_lanzaIllegalArgumentException() {
        mockUsuarioAutenticado();

        assertThrows(IllegalArgumentException.class, () -> svc.obtenerHistorialPaciente("  "));
    }

    @Test
    void obtenerHistorialPaciente_conPacienteNoEncontrado_lanzaIllegalArgumentException() {
        mockUsuarioAutenticado();
        when(hmacSearchIndexService.indexar("12345678A")).thenReturn("hash-12345678A");
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> svc.obtenerHistorialPaciente("12345678A"));
    }

    @Test
    void obtenerHistorialPaciente_sinRelacionActiva_lanzaIllegalStateException() {
        mockUsuarioAutenticado();
        Usuario paciente = new Usuario();
        paciente.setId(UUID.randomUUID());

        when(hmacSearchIndexService.indexar("12345678A")).thenReturn("hash-12345678A");
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(paciente));
        when(medicoPacienteRepository.existsByMedicoIdAndPacienteIdAndEstado(u.getId(), paciente.getId(), "ACTIVA"))
                .thenReturn(false);

        assertThrows(IllegalStateException.class, () -> svc.obtenerHistorialPaciente("12345678A"));
    }

    @Test
    void obtenerHistorialPaciente_sinHistorialCreado_devuelveNull() {
        mockUsuarioAutenticado();
        Usuario paciente = new Usuario();
        paciente.setId(UUID.randomUUID());

        when(hmacSearchIndexService.indexar("12345678A")).thenReturn("hash-12345678A");
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(paciente));
        when(medicoPacienteRepository.existsByMedicoIdAndPacienteIdAndEstado(u.getId(), paciente.getId(), "ACTIVA"))
                .thenReturn(true);
        when(historiaRepo.findByUsuario(paciente)).thenReturn(Optional.empty());

        assertNull(svc.obtenerHistorialPaciente("12345678A"));
    }

    // ---- obtenerUsuarioAutenticado (a través de crearAntecedente) ----

    @Test
    void crearAntecedente_sinUsuarioAutenticado_lanzaIllegalStateException() {
        when(usuarioFacade.getUsuarioActual()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> svc.crearAntecedente(new AntecedenteClinicoDTO()));
    }

    @Test
    void crearAntecedente_conUsuarioNoEncontradoEnBd_lanzaIllegalArgumentException() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(u.getId().toString());
        when(usuarioFacade.getUsuarioActual()).thenReturn(dto);
        when(usuarioRepository.findById(u.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> svc.crearAntecedente(new AntecedenteClinicoDTO()));
    }

    // ---- crearAntecedente / editarAntecedente / borrarAntecedente ----

    @Test
    void crearAntecedente_loGuardaYRegistraAuditoria() {
        mockUsuarioAutenticado();
        mockConstruccionDtoVacia();
        AntecedenteClinicoDTO dto = new AntecedenteClinicoDTO();
        dto.setCategoria("PERSONAL");
        dto.setDescripcion("Hipertensión");
        AntecedenteClinico entidad = new AntecedenteClinico();
        entidad.setId(UUID.randomUUID());
        entidad.setDescripcion("Hipertensión");
        when(antecedenteClinicoConverter.toEntity(dto, h)).thenReturn(entidad);
        when(antecedenteClinicoRepository.save(entidad)).thenReturn(entidad);

        HistorialClinicoDTO resultado = svc.crearAntecedente(dto);

        assertNotNull(resultado);
        verify(auditoriaCambioService).registrarCambio(
                any(), any(), any(), any(), any(), any(), any(), any(), eq(AuditoriaCambio.TipoOperacion.CREATE), any());
    }

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

    @Test
    void editarAntecedente_conAntecedenteDeOtroHistorial_lanzaIllegalArgumentException() {
        mockUsuarioAutenticado();
        UUID antecedenteId = UUID.randomUUID();
        AntecedenteClinico deOtroHistorial = new AntecedenteClinico();
        deOtroHistorial.setId(antecedenteId);
        HistorialClinico otroHistorial = new HistorialClinico();
        otroHistorial.setId(UUID.randomUUID());
        deOtroHistorial.setHistorialClinico(otroHistorial);
        when(antecedenteClinicoRepository.findById(antecedenteId)).thenReturn(Optional.of(deOtroHistorial));

        assertThrows(IllegalArgumentException.class,
                () -> svc.editarAntecedente(antecedenteId, new AntecedenteClinicoDTO()));
    }

    @Test
    void editarAntecedente_conAntecedenteInexistente_lanzaIllegalArgumentException() {
        mockUsuarioAutenticado();
        UUID antecedenteId = UUID.randomUUID();
        when(antecedenteClinicoRepository.findById(antecedenteId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> svc.editarAntecedente(antecedenteId, new AntecedenteClinicoDTO()));
    }

    @Test
    void borrarAntecedente_conAntecedentePropio_loElimina() {
        mockUsuarioAutenticado();
        mockConstruccionDtoVacia();
        UUID antecedenteId = UUID.randomUUID();
        AntecedenteClinico existente = new AntecedenteClinico();
        existente.setId(antecedenteId);
        existente.setHistorialClinico(h);
        existente.setDescripcion("a borrar");
        when(antecedenteClinicoRepository.findById(antecedenteId)).thenReturn(Optional.of(existente));

        assertNotNull(svc.borrarAntecedente(antecedenteId));

        verify(antecedenteClinicoRepository).delete(existente);
        verify(auditoriaCambioService).registrarCambio(
                any(), any(), any(), any(), any(), any(), any(), any(), eq(AuditoriaCambio.TipoOperacion.DELETE), any());
    }

    @Test
    void borrarAntecedente_conAntecedenteDeOtroHistorial_lanzaIllegalArgumentException() {
        mockUsuarioAutenticado();
        UUID antecedenteId = UUID.randomUUID();
        AntecedenteClinico deOtroHistorial = new AntecedenteClinico();
        deOtroHistorial.setId(antecedenteId);
        HistorialClinico otroHistorial = new HistorialClinico();
        otroHistorial.setId(UUID.randomUUID());
        deOtroHistorial.setHistorialClinico(otroHistorial);
        when(antecedenteClinicoRepository.findById(antecedenteId)).thenReturn(Optional.of(deOtroHistorial));

        assertThrows(IllegalArgumentException.class, () -> svc.borrarAntecedente(antecedenteId));
    }

    // ---- crearAlergia / borrarAlergia ----

    @Test
    void crearAlergia_loGuardaYRegistraAuditoria() {
        mockUsuarioAutenticado();
        mockConstruccionDtoVacia();
        AlergiaDTO dto = new AlergiaDTO();
        dto.setDescripcion("Alergia a la penicilina");
        Alergia entidad = new Alergia();
        entidad.setId(UUID.randomUUID());
        entidad.setDescripcion("Alergia a la penicilina");
        when(alergiaConverter.toEntity(dto, h)).thenReturn(entidad);
        when(alergiaRepository.save(entidad)).thenReturn(entidad);

        assertNotNull(svc.crearAlergia(dto));

        verify(auditoriaCambioService).registrarCambio(
                any(), any(), any(), any(), any(), any(), any(), any(), eq(AuditoriaCambio.TipoOperacion.CREATE), any());
    }

    @Test
    void borrarAlergia_conAlergiaPropia_laElimina() {
        mockUsuarioAutenticado();
        mockConstruccionDtoVacia();
        UUID alergiaId = UUID.randomUUID();
        Alergia existente = new Alergia();
        existente.setId(alergiaId);
        existente.setHistorialClinico(h);
        when(alergiaRepository.findById(alergiaId)).thenReturn(Optional.of(existente));

        assertNotNull(svc.borrarAlergia(alergiaId));

        verify(alergiaRepository).delete(existente);
    }

    @Test
    void borrarAlergia_conAlergiaDeOtroHistorial_lanzaIllegalArgumentException() {
        mockUsuarioAutenticado();
        UUID alergiaId = UUID.randomUUID();
        Alergia deOtroHistorial = new Alergia();
        deOtroHistorial.setId(alergiaId);
        HistorialClinico otroHistorial = new HistorialClinico();
        otroHistorial.setId(UUID.randomUUID());
        deOtroHistorial.setHistorialClinico(otroHistorial);
        when(alergiaRepository.findById(alergiaId)).thenReturn(Optional.of(deOtroHistorial));

        assertThrows(IllegalArgumentException.class, () -> svc.borrarAlergia(alergiaId));
    }

    @Test
    void borrarAlergia_conAlergiaInexistente_lanzaIllegalArgumentException() {
        mockUsuarioAutenticado();
        UUID alergiaId = UUID.randomUUID();
        when(alergiaRepository.findById(alergiaId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> svc.borrarAlergia(alergiaId));
    }

    // ---- guardar mediciones cuantitativas: cubre en detalle procesarDatosClinicos ----

    private static DatoClinicoEntradaDTO entrada(String label, String key, String value, String unit, String createdAt) {
        DatoClinicoEntradaDTO d = new DatoClinicoEntradaDTO();
        d.setLabel(label);
        d.setKey(key);
        d.setValue(value);
        d.setUnit(unit);
        d.setCreatedAt(createdAt);
        return d;
    }

    private static DatoClinicoEntradaDTO conLabelYValor(String label, String value) {
        return entrada(label, null, value, null, null);
    }

    @Test
    void actualizarAnalisisSangre_conDatosValidos_creaElDatoClinicoConRangoExacto() {
        mockUsuarioAutenticado();
        when(historialClinicoConverter.toDto(h)).thenReturn(new HistorialClinicoDTO());
        Rango rango = new Rango();
        rango.setNombre("Glucosa");
        when(rangoRepository.findByNombreIgnoreCase("Glucosa")).thenReturn(Optional.of(rango));

        svc.actualizarAnalisisSangre(List.of(entrada("Glucosa", null, "95,5", "mg/dL", null)));

        ArgumentCaptor<List<DatoClinico>> captor = ArgumentCaptor.forClass(List.class);
        verify(datoRepo).saveAll(captor.capture());
        DatoClinico guardado = captor.getValue().get(0);
        assertEquals("Glucosa", guardado.getTipo());
        assertEquals("mg/dL", guardado.getUnidad());
        assertEquals("95.5", guardado.getValor());
        assertEquals(rango, guardado.getRango());
    }

    @Test
    void actualizarAnalisisSangre_conValorDecimal_loGuardaSinPerderPrecision() {
        mockUsuarioAutenticado();
        when(historialClinicoConverter.toDto(h)).thenReturn(new HistorialClinicoDTO());

        svc.actualizarAnalisisSangre(List.of(
                conLabelYValor("Glucosa", "5.1"),
                conLabelYValor("Colesterol", "180"),
                conLabelYValor("Creatinina", " 0,89 ")));

        ArgumentCaptor<List<DatoClinico>> captor = ArgumentCaptor.forClass(List.class);
        verify(datoRepo).saveAll(captor.capture());
        List<DatoClinico> guardados = captor.getValue();
        assertEquals("5.1", guardados.get(0).getValor());
        assertEquals("180", guardados.get(1).getValor());
        assertEquals("0.89", guardados.get(2).getValor());
    }

    @Test
    void actualizarAnalisisSangre_sinRangoExacto_buscaPorCoincidenciaParcial() {
        mockUsuarioAutenticado();
        when(historialClinicoConverter.toDto(h)).thenReturn(new HistorialClinicoDTO());
        Rango rango = new Rango();
        rango.setNombre("Colesterol Total");
        when(rangoRepository.findByNombreIgnoreCase("Colesterol")).thenReturn(Optional.empty());
        when(rangoRepository.findByNombreContainingIgnoreCase("Colesterol")).thenReturn(Optional.of(rango));

        svc.actualizarAnalisisSangre(List.of(conLabelYValor("Colesterol", "180")));

        ArgumentCaptor<List<DatoClinico>> captor = ArgumentCaptor.forClass(List.class);
        verify(datoRepo).saveAll(captor.capture());
        assertEquals(rango, captor.getValue().get(0).getRango());
    }

    @Test
    void actualizarAnalisisSangre_sinRangoEncontrado_dejaElRangoNulo() {
        mockUsuarioAutenticado();
        when(historialClinicoConverter.toDto(h)).thenReturn(new HistorialClinicoDTO());
        when(rangoRepository.findByNombreIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(rangoRepository.findByNombreContainingIgnoreCase(anyString())).thenReturn(Optional.empty());

        svc.actualizarAnalisisSangre(List.of(conLabelYValor("Desconocido", "1")));

        ArgumentCaptor<List<DatoClinico>> captor = ArgumentCaptor.forClass(List.class);
        verify(datoRepo).saveAll(captor.capture());
        assertNull(captor.getValue().get(0).getRango());
    }

    @Test
    void actualizarAnalisisSangre_conKeyEnVezDeLabel_usaKeyComoTipo() {
        mockUsuarioAutenticado();
        when(historialClinicoConverter.toDto(h)).thenReturn(new HistorialClinicoDTO());

        svc.actualizarAnalisisSangre(List.of(entrada(null, "Hemoglobina", "14", null, null)));

        ArgumentCaptor<List<DatoClinico>> captor = ArgumentCaptor.forClass(List.class);
        verify(datoRepo).saveAll(captor.capture());
        assertEquals("Hemoglobina", captor.getValue().get(0).getTipo());
    }

    @Test
    void actualizarAnalisisSangre_sinLabelNiKey_usaTipoPorDefecto() {
        mockUsuarioAutenticado();
        when(historialClinicoConverter.toDto(h)).thenReturn(new HistorialClinicoDTO());

        svc.actualizarAnalisisSangre(List.of(entrada(null, null, "14", null, null)));

        ArgumentCaptor<List<DatoClinico>> captor = ArgumentCaptor.forClass(List.class);
        verify(datoRepo).saveAll(captor.capture());
        assertEquals("ANALISIS", captor.getValue().get(0).getTipo());
    }

    @Test
    void actualizarAnalisisSangre_conCreatedAtFormatoOffset_parseaLaFecha() {
        mockUsuarioAutenticado();
        when(historialClinicoConverter.toDto(h)).thenReturn(new HistorialClinicoDTO());

        svc.actualizarAnalisisSangre(List.of(entrada(null, null, "1", null, "2024-01-15T10:30:00+01:00")));

        ArgumentCaptor<List<DatoClinico>> captor = ArgumentCaptor.forClass(List.class);
        verify(datoRepo).saveAll(captor.capture());
        assertEquals(2024, captor.getValue().get(0).getFechaCreacion().getYear());
    }

    @Test
    void actualizarAnalisisSangre_conCreatedAtFormatoLocalDateTime_parseaLaFecha() {
        mockUsuarioAutenticado();
        when(historialClinicoConverter.toDto(h)).thenReturn(new HistorialClinicoDTO());

        svc.actualizarAnalisisSangre(List.of(entrada(null, null, "1", null, "2024-01-15T10:30:00")));

        ArgumentCaptor<List<DatoClinico>> captor = ArgumentCaptor.forClass(List.class);
        verify(datoRepo).saveAll(captor.capture());
        assertEquals(2024, captor.getValue().get(0).getFechaCreacion().getYear());
    }

    @Test
    void actualizarAnalisisSangre_conCreatedAtInvalido_usaFechaActual() {
        mockUsuarioAutenticado();
        when(historialClinicoConverter.toDto(h)).thenReturn(new HistorialClinicoDTO());

        svc.actualizarAnalisisSangre(List.of(entrada(null, null, "1", null, "no-es-una-fecha")));

        ArgumentCaptor<List<DatoClinico>> captor = ArgumentCaptor.forClass(List.class);
        verify(datoRepo).saveAll(captor.capture());
        assertNotNull(captor.getValue().get(0).getFechaCreacion());
    }

    @Test
    void actualizarAnalisisSangre_conEliminarExistentesYDatosPrevios_losElimina() {
        mockUsuarioAutenticado();
        when(historialClinicoConverter.toDto(h)).thenReturn(new HistorialClinicoDTO());
        DatoClinico previo = new DatoClinico();
        when(datoRepo.findByHistorialClinicoAndTipoHashIn(eq(h), any())).thenReturn(List.of(previo));

        svc.actualizarAnalisisSangre(List.of());

        verify(datoRepo).deleteAll(List.of(previo));
    }

    @Test
    void anadirAnalisisSangre_noEliminaLosDatosPrevios() {
        mockUsuarioAutenticado();
        when(historialClinicoConverter.toDto(h)).thenReturn(new HistorialClinicoDTO());

        svc.añadirAnalisisSangre(List.of());

        verify(datoRepo, never()).deleteAll(any());
        verify(auditoriaCambioService).registrarCambio(
                any(), any(), any(), any(), any(), any(), any(), any(), eq(AuditoriaCambio.TipoOperacion.CREATE), any());
    }

    @Test
    void actualizarAnalisisSangre_conItemSinValue_lanzaIllegalArgumentException() {
        mockUsuarioAutenticado();

        assertThrows(IllegalArgumentException.class,
                () -> svc.actualizarAnalisisSangre(List.of(entrada("Glucosa", null, null, null, null))));
    }

    @Test
    void actualizarAnalisisSangre_conValorNoNumerico_lanzaIllegalArgumentException() {
        mockUsuarioAutenticado();

        assertThrows(IllegalArgumentException.class,
                () -> svc.actualizarAnalisisSangre(List.of(entrada(null, null, "no-es-un-numero", null, null))));
    }

    @Test
    void actualizarAnalisisSangre_conListaNulaOVacia_noProcesaNiGuardaDatos() {
        mockUsuarioAutenticado();
        when(historialClinicoConverter.toDto(h)).thenReturn(new HistorialClinicoDTO());

        svc.actualizarAnalisisSangre(null);

        verify(datoRepo, never()).saveAll(any());
    }

    // ---- variantes de signos vitales y análisis de orina (camino feliz) ----

    @Test
    void actualizarSignosVitales_conDatosValidos_guardaLosDatos() {
        mockUsuarioAutenticado();
        when(historialClinicoConverter.toDto(h)).thenReturn(new HistorialClinicoDTO());

        svc.actualizarSignosVitales(List.of(conLabelYValor("Frecuencia Cardiaca", "70")));

        verify(datoRepo).saveAll(any());
        verify(auditoriaCambioService).registrarCambio(
                any(), any(), any(), any(), any(), any(), any(), any(), eq(AuditoriaCambio.TipoOperacion.UPDATE), any());
    }

    @Test
    void anadirSignosVitales_conDatosValidos_guardaLosDatos() {
        mockUsuarioAutenticado();
        when(historialClinicoConverter.toDto(h)).thenReturn(new HistorialClinicoDTO());

        svc.añadirSignosVitales(List.of(conLabelYValor("IMC", "22.5")));

        verify(datoRepo).saveAll(any());
    }

    @Test
    void actualizarAnalisisOrina_conDatosValidos_guardaLosDatos() {
        mockUsuarioAutenticado();
        when(historialClinicoConverter.toDto(h)).thenReturn(new HistorialClinicoDTO());

        svc.actualizarAnalisisOrina(List.of(conLabelYValor("PH Orina", "6.5")));

        verify(datoRepo).saveAll(any());
    }

    @Test
    void anadirAnalisisOrina_conDatosValidos_guardaLosDatos() {
        mockUsuarioAutenticado();
        when(historialClinicoConverter.toDto(h)).thenReturn(new HistorialClinicoDTO());

        svc.añadirAnalisisOrina(List.of(conLabelYValor("PH Orina", "6.5")));

        verify(datoRepo).saveAll(any());
    }

    // ---- borrarDatoClinico ----

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

    @Test
    void borrarDatoClinico_conIdNulo_lanzaIllegalArgumentException() {
        mockUsuarioAutenticado();

        assertThrows(IllegalArgumentException.class, () -> svc.borrarDatoClinico(null));
    }

    @Test
    void borrarDatoClinico_conDatoInexistente_lanzaIllegalArgumentException() {
        mockUsuarioAutenticado();
        UUID datoId = UUID.randomUUID();
        when(datoRepo.findById(datoId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> svc.borrarDatoClinico(datoId));
    }

    @Test
    void borrarDatoClinico_conDatoDeOtroHistorial_lanzaIllegalArgumentException() {
        mockUsuarioAutenticado();
        UUID datoId = UUID.randomUUID();
        DatoClinico deOtroHistorial = new DatoClinico();
        deOtroHistorial.setId(datoId);
        HistorialClinico otroHistorial = new HistorialClinico();
        otroHistorial.setId(UUID.randomUUID());
        deOtroHistorial.setHistorialClinico(otroHistorial);
        when(datoRepo.findById(datoId)).thenReturn(Optional.of(deOtroHistorial));

        assertThrows(IllegalArgumentException.class, () -> svc.borrarDatoClinico(datoId));
    }
}
