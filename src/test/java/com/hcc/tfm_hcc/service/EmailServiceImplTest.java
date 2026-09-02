package com.hcc.tfm_hcc.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.hcc.tfm_hcc.service.impl.EmailServiceImpl;

class EmailServiceImplTest {

    private static final String DESTINO = "ana@example.com";
    private static final String ENLACE = "http://localhost:8080/restablecer-password?token=abc";

    @SuppressWarnings("unchecked")
    private ObjectProvider<JavaMailSender> providerDe(JavaMailSender sender) {
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(sender);
        return provider;
    }

    @Test
    void enviarEnlace_conEnvioHabilitadoYSender_enviaElCorreo() {
        JavaMailSender sender = mock(JavaMailSender.class);
        EmailServiceImpl service = new EmailServiceImpl(providerDe(sender), true, "no-reply@hcc.local");

        service.enviarEnlaceRestablecimientoPassword(DESTINO, ENLACE);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(captor.capture());
        SimpleMailMessage mensaje = captor.getValue();
        org.junit.jupiter.api.Assertions.assertArrayEquals(new String[] { DESTINO }, mensaje.getTo());
        org.junit.jupiter.api.Assertions.assertEquals("no-reply@hcc.local", mensaje.getFrom());
        org.junit.jupiter.api.Assertions.assertTrue(mensaje.getText().contains(ENLACE));
    }

    @Test
    void enviarEnlace_conEnvioDeshabilitado_noContactaConElSender() {
        JavaMailSender sender = mock(JavaMailSender.class);
        EmailServiceImpl service = new EmailServiceImpl(providerDe(sender), false, "no-reply@hcc.local");

        service.enviarEnlaceRestablecimientoPassword(DESTINO, ENLACE);

        verify(sender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void enviarEnlace_sinSenderConfigurado_noFalla() {
        EmailServiceImpl service = new EmailServiceImpl(providerDe(null), true, "no-reply@hcc.local");

        assertDoesNotThrow(() -> service.enviarEnlaceRestablecimientoPassword(DESTINO, ENLACE));
    }

    @Test
    void enviarEnlace_siElEnvioFalla_noPropagaLaExcepcion() {
        JavaMailSender sender = mock(JavaMailSender.class);
        doThrow(new MailSendException("smtp caído")).when(sender).send(any(SimpleMailMessage.class));
        EmailServiceImpl service = new EmailServiceImpl(providerDe(sender), true, "no-reply@hcc.local");

        assertDoesNotThrow(() -> service.enviarEnlaceRestablecimientoPassword(DESTINO, ENLACE));
    }
}
