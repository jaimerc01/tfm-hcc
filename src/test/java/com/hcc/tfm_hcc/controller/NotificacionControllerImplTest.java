package com.hcc.tfm_hcc.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.hcc.tfm_hcc.controller.impl.NotificacionControllerImpl;
import com.hcc.tfm_hcc.dto.NotificacionDTO;
import com.hcc.tfm_hcc.dto.NotificacionPageDTO;
import com.hcc.tfm_hcc.exception.NotificacionAccesoException;
import com.hcc.tfm_hcc.exception.NotificacionOperacionException;
import com.hcc.tfm_hcc.exception.NotificacionValidationException;
import com.hcc.tfm_hcc.facade.NotificacionFacade;

class NotificacionControllerImplTest {

    private MockMvc mvc;
    private NotificacionFacade notificacionFacade;

    @BeforeEach
    void setUp() {
        notificacionFacade = mock(NotificacionFacade.class);
        mvc = MockMvcBuilders.standaloneSetup(new NotificacionControllerImpl(notificacionFacade)).build();
    }

    @Test
    void listarMisNotificaciones_delegaEnElFacadeYDevuelveItemsYTotal() throws Exception {
        NotificacionDTO dto = new NotificacionDTO();
        dto.setId("id-1");
        when(notificacionFacade.listarNotificacionesUsuarioActual(0, 10))
                .thenReturn(new NotificacionPageDTO(List.of(dto), 7L));

        mvc.perform(get("/notificaciones").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value("id-1"))
                .andExpect(jsonPath("$.total").value(7));

        verify(notificacionFacade).listarNotificacionesUsuarioActual(0, 10);
    }

    @Test
    void listarMisNotificaciones_conParametrosInvalidos_propagaEl400DelFacade() throws Exception {
        when(notificacionFacade.listarNotificacionesUsuarioActual(anyInt(), anyInt()))
                .thenThrow(new NotificacionValidationException("página negativa"));

        mvc.perform(get("/notificaciones").param("page", "-1").param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void marcarTodasNotificacionesLeidas_devuelveOk() throws Exception {
        mvc.perform(post("/notificaciones/marcar-leidas")).andExpect(status().isOk());

        verify(notificacionFacade).marcarTodasComoLeidasUsuarioActual();
    }

    @Test
    void marcarTodasNotificacionesLeidas_conErrorInterno_propagaEl500DelFacade() throws Exception {
        doThrow(new NotificacionOperacionException("fallo"))
                .when(notificacionFacade).marcarTodasComoLeidasUsuarioActual();

        mvc.perform(post("/notificaciones/marcar-leidas")).andExpect(status().isInternalServerError());
    }

    @Test
    void contarNotificacionesNoLeidas_devuelveElConteo() throws Exception {
        when(notificacionFacade.contarNoLeidasUsuarioActual()).thenReturn(3L);

        mvc.perform(get("/notificaciones/no-leidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noLeidas").value(3));
    }

    @Test
    void marcarNotificacionLeida_devuelveOk() throws Exception {
        mvc.perform(put("/notificaciones/id-1/leida")).andExpect(status().isOk());

        verify(notificacionFacade).marcarNotificacionComoLeida("id-1");
    }

    @Test
    void marcarNotificacionLeida_deOtroUsuario_propagaEl403DelFacade() throws Exception {
        when(notificacionFacade.marcarNotificacionComoLeida("id-1"))
                .thenThrow(new NotificacionAccesoException("no es tuya"));

        mvc.perform(put("/notificaciones/id-1/leida")).andExpect(status().isForbidden());
    }

    @Test
    void eliminarNotificacion_devuelveNoContent() throws Exception {
        mvc.perform(delete("/notificaciones/id-1")).andExpect(status().isNoContent());

        verify(notificacionFacade).eliminarNotificacionUsuarioActual("id-1");
    }

    @Test
    void eliminarNotificacion_conIdInvalido_propagaEl400DelFacade() throws Exception {
        when(notificacionFacade.eliminarNotificacionUsuarioActual("id-1"))
                .thenThrow(new NotificacionValidationException("id vacío"));

        mvc.perform(delete("/notificaciones/id-1")).andExpect(status().isBadRequest());
    }
}
