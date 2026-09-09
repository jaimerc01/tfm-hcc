package com.hcc.tfm_hcc.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.hcc.tfm_hcc.converter.NotificacionConverter;
import com.hcc.tfm_hcc.dto.NotificacionDTO;
import com.hcc.tfm_hcc.dto.NotificacionPageDTO;
import com.hcc.tfm_hcc.exception.NotificacionAccesoException;
import com.hcc.tfm_hcc.exception.NotificacionOperacionException;
import com.hcc.tfm_hcc.exception.NotificacionValidationException;
import com.hcc.tfm_hcc.facade.impl.NotificacionFacadeImpl;
import com.hcc.tfm_hcc.model.Notificacion;
import com.hcc.tfm_hcc.service.NotificacionService;

class NotificacionFacadeImplTest {

    private NotificacionService notificacionService;
    private NotificacionConverter notificacionConverter;
    private NotificacionFacadeImpl facade;

    @BeforeEach
    void setUp() {
        notificacionService = mock(NotificacionService.class);
        notificacionConverter = mock(NotificacionConverter.class);
        facade = new NotificacionFacadeImpl(notificacionService, notificacionConverter);
    }

    @Test
    void listarNotificacionesUsuarioActual_devuelveLasConvertidas() {
        Notificacion notificacion = new Notificacion();
        NotificacionDTO dto = new NotificacionDTO();
        when(notificacionService.listarNotificacionesUsuarioActual()).thenReturn(List.of(notificacion));
        when(notificacionConverter.toDtoList(List.of(notificacion))).thenReturn(List.of(dto));

        assertEquals(List.of(dto), facade.listarNotificacionesUsuarioActual());
    }

    @Test
    void listarNotificacionesUsuarioActual_conErrorInesperado_lanzaNotificacionOperacionException() {
        when(notificacionService.listarNotificacionesUsuarioActual()).thenThrow(new RuntimeException("fallo"));

        assertThrows(NotificacionOperacionException.class, () -> facade.listarNotificacionesUsuarioActual());
    }

    @Test
    void crearNotificacionParaUsuario_conDatosValidos_devuelveElDto() {
        Notificacion notificacion = new Notificacion();
        NotificacionDTO dto = new NotificacionDTO();
        when(notificacionService.crearNotificacionParaUsuario("12345678A", "mensaje")).thenReturn(notificacion);
        when(notificacionConverter.toDto(notificacion)).thenReturn(dto);

        assertEquals(dto, facade.crearNotificacionParaUsuario("12345678A", "mensaje"));
    }

    @Test
    void crearNotificacionParaUsuario_conIllegalArgumentDelServicio_lanzaNotificacionValidationException() {
        when(notificacionService.crearNotificacionParaUsuario("00000000Z", "mensaje"))
                .thenThrow(new IllegalArgumentException("usuario no encontrado"));

        assertThrows(NotificacionValidationException.class,
                () -> facade.crearNotificacionParaUsuario("00000000Z", "mensaje"));
    }

    @Test
    void crearNotificacionParaUsuario_conSecurityExceptionDelServicio_lanzaNotificacionAccesoException() {
        when(notificacionService.crearNotificacionParaUsuario("12345678A", "mensaje"))
                .thenThrow(new SecurityException("acceso denegado"));

        assertThrows(NotificacionAccesoException.class,
                () -> facade.crearNotificacionParaUsuario("12345678A", "mensaje"));
    }

    @Test
    void listarNotificacionesUsuarioActualPaginado_devuelveLosItemsYElTotalDeLaPagina() {
        Notificacion notificacion = new Notificacion();
        NotificacionDTO dto = new NotificacionDTO();
        dto.setId("id-1");
        Page<Notificacion> pagina = new PageImpl<>(List.of(notificacion));
        when(notificacionService.listarNotificacionesUsuarioActual(0, 10)).thenReturn(pagina);
        when(notificacionConverter.toDtoList(List.of(notificacion))).thenReturn(List.of(dto));

        NotificacionPageDTO resultado = facade.listarNotificacionesUsuarioActual(0, 10);

        assertEquals(List.of(dto), resultado.getItems());
        assertEquals(pagina.getTotalElements(), resultado.getTotal());
    }

    @Test
    void listarNotificacionesUsuarioActualPaginado_conPaginaNegativa_lanzaNotificacionValidationException() {
        assertThrows(NotificacionValidationException.class, () -> facade.listarNotificacionesUsuarioActual(-1, 10));
    }

    @Test
    void listarNotificacionesUsuarioActualPaginado_conTamanoCero_lanzaNotificacionValidationException() {
        assertThrows(NotificacionValidationException.class, () -> facade.listarNotificacionesUsuarioActual(0, 0));
    }

    @Test
    void listarNotificacionesUsuarioActualPaginado_conTamanoMayorA100_lanzaNotificacionValidationException() {
        assertThrows(NotificacionValidationException.class, () -> facade.listarNotificacionesUsuarioActual(0, 101));
    }

    @Test
    void marcarTodasComoLeidasUsuarioActual_devuelveLasConvertidas() {
        Notificacion notificacion = new Notificacion();
        NotificacionDTO dto = new NotificacionDTO();
        when(notificacionService.marcarTodasComoLeidasUsuarioActual()).thenReturn(List.of(notificacion));
        when(notificacionConverter.toDtoList(List.of(notificacion))).thenReturn(List.of(dto));

        assertEquals(List.of(dto), facade.marcarTodasComoLeidasUsuarioActual());
    }

    @Test
    void marcarTodasComoLeidasUsuarioActual_conErrorInesperado_lanzaNotificacionOperacionException() {
        when(notificacionService.marcarTodasComoLeidasUsuarioActual()).thenThrow(new RuntimeException("fallo"));

        assertThrows(NotificacionOperacionException.class, () -> facade.marcarTodasComoLeidasUsuarioActual());
    }

    @Test
    void marcarNotificacionComoLeida_conIdValido_devuelveElDto() {
        Notificacion notificacion = new Notificacion();
        NotificacionDTO dto = new NotificacionDTO();
        when(notificacionService.marcarNotificacionComoLeida("id-1")).thenReturn(notificacion);
        when(notificacionConverter.toDto(notificacion)).thenReturn(dto);

        assertEquals(dto, facade.marcarNotificacionComoLeida("id-1"));
    }

    @Test
    void marcarNotificacionComoLeida_conIdVacio_lanzaNotificacionValidationException() {
        assertThrows(NotificacionValidationException.class, () -> facade.marcarNotificacionComoLeida("  "));
    }

    @Test
    void marcarNotificacionComoLeida_conAccesoDenegado_lanzaNotificacionAccesoException() {
        when(notificacionService.marcarNotificacionComoLeida("id-1"))
                .thenThrow(new SecurityException("no es tuya"));

        assertThrows(NotificacionAccesoException.class, () -> facade.marcarNotificacionComoLeida("id-1"));
    }

    @Test
    void eliminarNotificacionUsuarioActual_conIdValido_devuelveElDto() {
        Notificacion notificacion = new Notificacion();
        NotificacionDTO dto = new NotificacionDTO();
        when(notificacionService.eliminarNotificacionUsuarioActual("id-1")).thenReturn(notificacion);
        when(notificacionConverter.toDto(notificacion)).thenReturn(dto);

        assertEquals(dto, facade.eliminarNotificacionUsuarioActual("id-1"));
    }

    @Test
    void eliminarNotificacionUsuarioActual_conIdVacio_lanzaNotificacionValidationException() {
        assertThrows(NotificacionValidationException.class, () -> facade.eliminarNotificacionUsuarioActual(null));
    }

    @Test
    void eliminarNotificacionUsuarioActual_conErrorInesperado_lanzaNotificacionOperacionException() {
        when(notificacionService.eliminarNotificacionUsuarioActual("id-1")).thenThrow(new RuntimeException("fallo"));

        assertThrows(NotificacionOperacionException.class, () -> facade.eliminarNotificacionUsuarioActual("id-1"));
    }

    @Test
    void contarNoLeidasUsuarioActual_devuelveElConteoDelServicio() {
        when(notificacionService.contarNoLeidasUsuarioActual()).thenReturn(5L);

        assertEquals(5L, facade.contarNoLeidasUsuarioActual());
    }

    @Test
    void contarNoLeidasUsuarioActual_conErrorInesperado_lanzaNotificacionOperacionException() {
        when(notificacionService.contarNoLeidasUsuarioActual()).thenThrow(new RuntimeException("fallo"));

        assertThrows(NotificacionOperacionException.class, () -> facade.contarNoLeidasUsuarioActual());
    }
}
