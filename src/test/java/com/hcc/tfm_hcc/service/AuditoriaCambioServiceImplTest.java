package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hcc.tfm_hcc.model.AuditoriaCambio;
import com.hcc.tfm_hcc.model.AuditoriaCambio.TipoOperacion;
import com.hcc.tfm_hcc.repository.AuditoriaCambioRepository;
import com.hcc.tfm_hcc.service.impl.AuditoriaCambioServiceImpl;

class AuditoriaCambioServiceImplTest {

    private AuditoriaCambioRepository repository;
    private FieldEncryptionService fieldEncryptionService;
    private AuditoriaCambioServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(AuditoriaCambioRepository.class);
        fieldEncryptionService = mock(FieldEncryptionService.class);
        service = new AuditoriaCambioServiceImpl(repository, fieldEncryptionService);
    }

    @Test
    void registrarCambio_conEntidadSinIdNiFecha_losGeneraAntesDeGuardar() {
        AuditoriaCambio auditoria = new AuditoriaCambio();
        when(repository.save(any(AuditoriaCambio.class))).thenAnswer(inv -> inv.getArgument(0));

        AuditoriaCambio resultado = service.registrarCambio(auditoria);

        assertNotNull(resultado.getId());
        assertNotNull(resultado.getFechaCambio());
    }

    @Test
    void registrarCambio_conEntidadNula_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.registrarCambio((AuditoriaCambio) null));
    }

    @Test
    void registrarCambio_conParametrosIndividuales_construyeLaAuditoria() {
        when(repository.save(any(AuditoriaCambio.class))).thenAnswer(inv -> inv.getArgument(0));

        AuditoriaCambio resultado = service.registrarCambio(
                "usuario-1", "paciente-1", "medico-1", "GLUCOSA", "dato_clinico",
                "recurso-1", "100", "110", TipoOperacion.UPDATE, "Corrección");

        assertEquals("usuario-1", resultado.getIdUsuario());
        assertEquals("GLUCOSA", resultado.getTipoCambio());
        assertEquals(TipoOperacion.UPDATE, resultado.getTipoOperacion());
        assertEquals("Corrección", resultado.getRazonCambio());
    }

    @Test
    void registrarCambio_guardaLosValoresCifradosYDevuelveElTextoEnClaro() {
        when(fieldEncryptionService.cifrar("100")).thenReturn("CIFRADO(100)");
        when(fieldEncryptionService.cifrar("110")).thenReturn("CIFRADO(110)");
        // La misma instancia se pasa a save() y luego se restaura a texto en claro antes de
        // devolverla, así que hay que capturar los valores en el momento exacto de guardar
        // (un ArgumentCaptor vería el estado final, ya restaurado, por ser el mismo objeto).
        List<String> valoresGuardados = new ArrayList<>();
        when(repository.save(any(AuditoriaCambio.class))).thenAnswer(inv -> {
            AuditoriaCambio guardando = inv.getArgument(0);
            valoresGuardados.add(guardando.getValorAnterior());
            valoresGuardados.add(guardando.getValorNuevo());
            return guardando;
        });

        AuditoriaCambio resultado = service.registrarCambio(
                "usuario-1", "paciente-1", null, "GLUCOSA", "dato_clinico",
                "recurso-1", "100", "110", TipoOperacion.UPDATE, "Corrección");

        assertEquals(List.of("CIFRADO(100)", "CIFRADO(110)"), valoresGuardados);
        assertEquals("100", resultado.getValorAnterior());
        assertEquals("110", resultado.getValorNuevo());
    }

    @Test
    void obtenerHistorialCambiosUsuario_conIdValido_devuelveLosCambiosDescifrados() {
        AuditoriaCambio cambio = new AuditoriaCambio();
        cambio.setValorAnterior("CIFRADO(100)");
        cambio.setValorNuevo("CIFRADO(110)");
        when(repository.findByIdUsuarioOrderByFechaCambioDesc("usuario-1")).thenReturn(List.of(cambio));
        when(fieldEncryptionService.descifrar("CIFRADO(100)")).thenReturn("100");
        when(fieldEncryptionService.descifrar("CIFRADO(110)")).thenReturn("110");

        List<AuditoriaCambio> resultado = service.obtenerHistorialCambiosUsuario("usuario-1");

        assertEquals(1, resultado.size());
        assertEquals("100", resultado.get(0).getValorAnterior());
        assertEquals("110", resultado.get(0).getValorNuevo());
    }

    @Test
    void obtenerHistorialCambiosUsuario_conIdVacio_devuelveListaVacia() {
        List<AuditoriaCambio> resultado = service.obtenerHistorialCambiosUsuario("  ");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void obtenerHistorialCambiosPaciente_conIdValido_devuelveLosCambios() {
        when(repository.findByIdPacienteOrderByFechaCambioDesc("paciente-1"))
                .thenReturn(List.of(new AuditoriaCambio(), new AuditoriaCambio()));

        List<AuditoriaCambio> resultado = service.obtenerHistorialCambiosPaciente("paciente-1");

        assertEquals(2, resultado.size());
    }

    @Test
    void obtenerHistorialCambiosPaciente_conIdNulo_devuelveListaVacia() {
        assertTrue(service.obtenerHistorialCambiosPaciente((String) null).isEmpty());
    }

    @Test
    void obtenerHistorialCambiosPacienteConRango_conFechasValidas_delegaEnElRepositorio() {
        LocalDateTime desde = LocalDateTime.now().minusDays(1);
        LocalDateTime hasta = LocalDateTime.now();
        when(repository.findByIdPacienteAndFechaCambioBetween("paciente-1", desde, hasta))
                .thenReturn(List.of(new AuditoriaCambio()));

        List<AuditoriaCambio> resultado = service.obtenerHistorialCambiosPaciente("paciente-1", desde, hasta);

        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerHistorialCambiosPacienteConRango_conFechaNula_devuelveListaVacia() {
        assertTrue(service.obtenerHistorialCambiosPaciente("paciente-1", null, LocalDateTime.now()).isEmpty());
    }

    @Test
    void obtenerHistorialCambiosRecurso_conIdValido_devuelveLosCambios() {
        when(repository.findByIdRecursoOrderByFechaCambioDesc("recurso-1"))
                .thenReturn(List.of(new AuditoriaCambio()));

        assertEquals(1, service.obtenerHistorialCambiosRecurso("recurso-1").size());
    }

    @Test
    void obtenerHistorialCambiosRecurso_conIdVacio_devuelveListaVacia() {
        assertTrue(service.obtenerHistorialCambiosRecurso("").isEmpty());
    }

    @Test
    void obtenerCambiosPorTipo_conParametrosValidos_delegaEnElRepositorio() {
        when(repository.findByIdPacienteAndTipoCambioOrderByFechaCambioDesc("paciente-1", "GLUCOSA"))
                .thenReturn(List.of(new AuditoriaCambio()));

        assertEquals(1, service.obtenerCambiosPorTipo("paciente-1", "GLUCOSA").size());
    }

    @Test
    void obtenerCambiosPorTipo_conTipoCambioVacio_devuelveListaVacia() {
        assertTrue(service.obtenerCambiosPorTipo("paciente-1", "  ").isEmpty());
    }

    @Test
    void obtenerCambiosPorMedico_conIdValido_devuelveLosCambios() {
        when(repository.findByIdMedicoOrderByFechaCambioDesc("medico-1"))
                .thenReturn(List.of(new AuditoriaCambio()));

        assertEquals(1, service.obtenerCambiosPorMedico("medico-1").size());
    }

    @Test
    void obtenerCambiosPorMedico_conIdVacio_devuelveListaVacia() {
        assertTrue(service.obtenerCambiosPorMedico(null).isEmpty());
    }

    @Test
    void obtenerCambiosPorTipoOperacion_conParametrosValidos_delegaEnElRepositorio() {
        when(repository.findByIdPacienteAndTipoOperacionOrderByFechaCambioDesc("paciente-1", TipoOperacion.CREATE))
                .thenReturn(List.of(new AuditoriaCambio()));

        assertEquals(1, service.obtenerCambiosPorTipoOperacion("paciente-1", TipoOperacion.CREATE).size());
    }

    @Test
    void obtenerCambiosPorTipoOperacion_conTipoOperacionNulo_devuelveListaVacia() {
        assertTrue(service.obtenerCambiosPorTipoOperacion("paciente-1", null).isEmpty());
    }

    private AuditoriaCambio cambioConRazon(String razon) {
        AuditoriaCambio cambio = new AuditoriaCambio();
        cambio.setRazonCambio(razon);
        cambio.setTipoOperacion(TipoOperacion.UPDATE);
        cambio.setTipoCambio("GLUCOSA");
        cambio.setTabla("dato_clinico");
        cambio.setIdUsuario("usuario-1");
        return cambio;
    }

    @Test
    void hayAuditoriaSospechosa_conMasDeUn30PorcientoSinRazon_devuelveTrue() {
        List<AuditoriaCambio> cambios = new ArrayList<>();
        cambios.add(cambioConRazon(null));
        cambios.add(cambioConRazon(null));
        cambios.add(cambioConRazon("justificado"));
        when(repository.findByIdPacienteAndFechaCambioBetween(anyString(), any(), any())).thenReturn(cambios);

        assertTrue(service.hayAuditoriaSospechosa("paciente-1", LocalDateTime.now().minusDays(1), LocalDateTime.now()));
    }

    @Test
    void hayAuditoriaSospechosa_conMenosDeUn30PorcientoSinRazon_devuelveFalse() {
        List<AuditoriaCambio> cambios = new ArrayList<>();
        cambios.add(cambioConRazon("justificado 1"));
        cambios.add(cambioConRazon("justificado 2"));
        cambios.add(cambioConRazon("justificado 3"));
        cambios.add(cambioConRazon(null));
        when(repository.findByIdPacienteAndFechaCambioBetween(anyString(), any(), any())).thenReturn(cambios);

        assertFalse(service.hayAuditoriaSospechosa("paciente-1", LocalDateTime.now().minusDays(1), LocalDateTime.now()));
    }

    @Test
    void hayAuditoriaSospechosa_conIdPacienteVacio_devuelveFalse() {
        assertFalse(service.hayAuditoriaSospechosa("  ", LocalDateTime.now().minusDays(1), LocalDateTime.now()));
    }

    @Test
    void obtenerEstadisticas_conCambiosRegistrados_calculaLosContadoresCorrectamente() {
        AuditoriaCambio creacion = cambioConRazon("razon");
        creacion.setTipoOperacion(TipoOperacion.CREATE);
        AuditoriaCambio actualizacion = cambioConRazon(null);
        actualizacion.setTipoOperacion(TipoOperacion.UPDATE);
        actualizacion.setIdMedico("medico-1");
        AuditoriaCambio eliminacion = cambioConRazon("razon");
        eliminacion.setTipoOperacion(TipoOperacion.DELETE);

        when(repository.findByIdPacienteOrderByFechaCambioDesc("paciente-1"))
                .thenReturn(List.of(creacion, actualizacion, eliminacion));
        when(repository.findByIdPacienteAndFechaCambioBetween(anyString(), any(), any())).thenReturn(List.of());

        AuditoriaCambioStats stats = service.obtenerEstadisticas("paciente-1");

        assertEquals("paciente-1", stats.getIdPaciente());
        assertEquals(3, stats.getTotalCambios());
        assertEquals(1, stats.getCreaciones());
        assertEquals(1, stats.getActualizaciones());
        assertEquals(1, stats.getEliminaciones());
        assertEquals(1, stats.getCambiosSinRazon());
        assertFalse(stats.isActividadAnomala());
    }

    @Test
    void obtenerEstadisticas_conIdPacienteVacio_devuelveEstadisticasVacias() {
        AuditoriaCambioStats stats = service.obtenerEstadisticas(" ");

        assertEquals(0, stats.getTotalCambios());
    }
}
