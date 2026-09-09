package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.hcc.tfm_hcc.dto.UsuarioDTO;
import com.hcc.tfm_hcc.facade.UsuarioFacade;
import com.hcc.tfm_hcc.model.Notificacion;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.NotificacionRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.impl.NotificacionServiceImpl;

class NotificacionServiceImplTest {

    private NotificacionRepository notificacionRepository;
    private UsuarioRepository usuarioRepository;
    private UsuarioFacade usuarioFacade;
    private HmacSearchIndexService hmacSearchIndexService;
    private NotificacionServiceImpl service;

    private final UUID usuarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        notificacionRepository = mock(NotificacionRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        usuarioFacade = mock(UsuarioFacade.class);
        hmacSearchIndexService = mock(HmacSearchIndexService.class);
        when(hmacSearchIndexService.indexar("12345678A")).thenReturn("hash-12345678A");
        when(hmacSearchIndexService.indexar("00000000Z")).thenReturn("hash-00000000Z");
        service = new NotificacionServiceImpl(notificacionRepository, usuarioRepository, usuarioFacade, hmacSearchIndexService);
    }

    private Usuario usuarioActualMockeado() {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuarioId.toString());
        when(usuarioFacade.getUsuarioActual()).thenReturn(dto);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        return usuario;
    }

    @Test
    void crearNotificacionParaUsuario_conDatosValidos_creaLaNotificacion() {
        Usuario usuario = new Usuario();
        usuario.setNif("12345678A");
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Notificacion resultado = service.crearNotificacionParaUsuario("12345678A", "Tienes una nueva solicitud");

        assertEquals("Tienes una nueva solicitud", resultado.getMensaje());
        assertFalse(resultado.isLeida());
    }

    @Test
    void crearNotificacionParaUsuario_conMensajeNulo_usaCadenaVacia() {
        Usuario usuario = new Usuario();
        when(usuarioRepository.findByNifHash("hash-12345678A")).thenReturn(Optional.of(usuario));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Notificacion resultado = service.crearNotificacionParaUsuario("12345678A", null);

        assertEquals("", resultado.getMensaje());
    }

    @Test
    void crearNotificacionParaUsuario_conNifNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.crearNotificacionParaUsuario(null, "msg"));
    }

    @Test
    void crearNotificacionParaUsuario_conUsuarioInexistente_lanzaIllegalArgumentException() {
        when(usuarioRepository.findByNifHash("hash-00000000Z")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.crearNotificacionParaUsuario("00000000Z", "msg"));
    }

    /**
     * Regresión: el mensaje de esta excepción se loguea tal cual en varios llamadores
     * (p. ej. SolicitudAsignacionServiceImpl), así que debe llevar el NIF enmascarado y
     * no el valor en claro -- igual que el resto de logs de la aplicación.
     */
    @Test
    void crearNotificacionParaUsuario_conUsuarioInexistente_elMensajeDeLaExcepcionEnmascaraElNif() {
        when(usuarioRepository.findByNifHash("hash-00000000Z")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.crearNotificacionParaUsuario("00000000Z", "msg"));

        assertFalse(ex.getMessage().contains("00000000Z"));
        assertTrue(ex.getMessage().contains("***00Z"));
    }

    @Test
    void listarNotificacionesUsuarioActual_devuelveLasDelUsuario() {
        Usuario usuario = usuarioActualMockeado();
        when(notificacionRepository.findByUsuarioAndEliminadaFalseOrderByFechaCreacionDesc(usuario)).thenReturn(List.of(new Notificacion()));

        List<Notificacion> resultado = service.listarNotificacionesUsuarioActual();

        assertEquals(1, resultado.size());
    }

    @Test
    void listarNotificacionesUsuarioActual_conUsuarioNoAutenticado_lanzaIllegalStateException() {
        when(usuarioFacade.getUsuarioActual()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> service.listarNotificacionesUsuarioActual());
    }

    @Test
    void listarNotificacionesUsuarioActual_conIdConFormatoInvalido_lanzaIllegalArgumentException() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId("no-es-un-uuid");
        when(usuarioFacade.getUsuarioActual()).thenReturn(dto);

        assertThrows(IllegalArgumentException.class, () -> service.listarNotificacionesUsuarioActual());
    }

    @Test
    void listarNotificacionesUsuarioActual_conUsuarioNoEncontradoEnBd_lanzaIllegalStateException() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuarioId.toString());
        when(usuarioFacade.getUsuarioActual()).thenReturn(dto);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> service.listarNotificacionesUsuarioActual());
    }

    @Test
    void listarNotificacionesUsuarioActualPaginado_devuelveLaPagina() {
        Usuario usuario = usuarioActualMockeado();
        Page<Notificacion> pagina = new PageImpl<>(List.of(new Notificacion()));
        when(notificacionRepository.findByUsuarioAndEliminadaFalse(org.mockito.ArgumentMatchers.eq(usuario), any(Pageable.class)))
                .thenReturn(pagina);

        Page<Notificacion> resultado = service.listarNotificacionesUsuarioActual(0, 10);

        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    void marcarTodasComoLeidasUsuarioActual_marcaSoloLasNoLeidas() {
        Usuario usuario = usuarioActualMockeado();
        Notificacion leida = new Notificacion();
        leida.setLeida(true);
        Notificacion noLeida = new Notificacion();
        noLeida.setLeida(false);
        when(notificacionRepository.findByUsuarioAndEliminadaFalseOrderByFechaCreacionDesc(usuario))
                .thenReturn(List.of(leida, noLeida));

        List<Notificacion> resultado = service.marcarTodasComoLeidasUsuarioActual();

        assertTrue(resultado.stream().allMatch(Notificacion::isLeida));
        verify(notificacionRepository, times(1)).saveAll(anyList());
    }

    @Test
    void marcarTodasComoLeidasUsuarioActual_conListaVacia_noLlamaSaveAll() {
        Usuario usuario = usuarioActualMockeado();
        when(notificacionRepository.findByUsuarioAndEliminadaFalseOrderByFechaCreacionDesc(usuario)).thenReturn(List.of());

        service.marcarTodasComoLeidasUsuarioActual();

        verify(notificacionRepository, never()).saveAll(anyList());
    }

    @Test
    void marcarNotificacionComoLeida_conNotificacionPropiaNoLeida_laMarcaYGuarda() {
        Usuario usuario = usuarioActualMockeado();
        UUID notifId = UUID.randomUUID();
        Notificacion notificacion = new Notificacion();
        notificacion.setId(notifId);
        notificacion.setUsuario(usuario);
        notificacion.setLeida(false);
        when(notificacionRepository.findById(notifId)).thenReturn(Optional.of(notificacion));

        Notificacion resultado = service.marcarNotificacionComoLeida(notifId.toString());

        assertTrue(resultado.isLeida());
        verify(notificacionRepository, times(1)).save(notificacion);
    }

    @Test
    void marcarNotificacionComoLeida_yaLeida_noVuelveAGuardar() {
        Usuario usuario = usuarioActualMockeado();
        UUID notifId = UUID.randomUUID();
        Notificacion notificacion = new Notificacion();
        notificacion.setId(notifId);
        notificacion.setUsuario(usuario);
        notificacion.setLeida(true);
        when(notificacionRepository.findById(notifId)).thenReturn(Optional.of(notificacion));

        service.marcarNotificacionComoLeida(notifId.toString());

        verify(notificacionRepository, never()).save(any());
    }

    @Test
    void marcarNotificacionComoLeida_deOtroUsuario_lanzaIllegalStateException() {
        usuarioActualMockeado();
        UUID notifId = UUID.randomUUID();
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(UUID.randomUUID());
        Notificacion notificacion = new Notificacion();
        notificacion.setId(notifId);
        notificacion.setUsuario(otroUsuario);
        when(notificacionRepository.findById(notifId)).thenReturn(Optional.of(notificacion));

        assertThrows(IllegalStateException.class, () -> service.marcarNotificacionComoLeida(notifId.toString()));
    }

    @Test
    void marcarNotificacionComoLeida_conIdConFormatoInvalido_lanzaIllegalArgumentException() {
        usuarioActualMockeado();

        assertThrows(IllegalArgumentException.class, () -> service.marcarNotificacionComoLeida("no-es-un-uuid"));
    }

    @Test
    void marcarNotificacionComoLeida_conNotificacionInexistente_lanzaIllegalArgumentException() {
        usuarioActualMockeado();
        UUID notifId = UUID.randomUUID();
        when(notificacionRepository.findById(notifId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.marcarNotificacionComoLeida(notifId.toString()));
    }

    @Test
    void eliminarNotificacionUsuarioActual_laMarcaComoEliminada() {
        Usuario usuario = usuarioActualMockeado();
        UUID notifId = UUID.randomUUID();
        Notificacion notificacion = new Notificacion();
        notificacion.setId(notifId);
        notificacion.setUsuario(usuario);
        when(notificacionRepository.findById(notifId)).thenReturn(Optional.of(notificacion));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Notificacion resultado = service.eliminarNotificacionUsuarioActual(notifId.toString());

        assertTrue(resultado.isEliminada());
        assertEquals(notificacion, resultado);
    }

    @Test
    void contarNoLeidasUsuarioActual_devuelveElConteoDelRepositorio() {
        Usuario usuario = usuarioActualMockeado();
        when(notificacionRepository.countByUsuarioAndLeidaFalseAndEliminadaFalse(usuario)).thenReturn(3L);

        assertEquals(3L, service.contarNoLeidasUsuarioActual());
    }
}
