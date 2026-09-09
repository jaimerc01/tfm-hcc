package com.hcc.tfm_hcc.service.impl;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.hcc.tfm_hcc.service.EmailService;
import com.hcc.tfm_hcc.util.LogMaskUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementación de {@link EmailService} sobre {@link JavaMailSender}.
 *
 * <p>El envío real solo se produce si {@code app.mail.enabled} está a {@code true}
 * y hay un {@code JavaMailSender} configurado. En cualquier otro caso (desarrollo
 * sin SMTP, tests) el enlace se registra en el log a nivel INFO y el método
 * termina con normalidad, para que el flujo de restablecimiento se pueda probar
 * de extremo a extremo sin infraestructura de correo.</p>
 *
 * <p>Un fallo de envío ({@link MailException}) se registra pero no se propaga: la
 * capa que llama (solicitud de restablecimiento) responde siempre de forma neutra
 * para no revelar si el correo existe, así que tampoco debe fallar por un problema
 * del servidor SMTP.</p>
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private static final String ASUNTO_RESET = "Restablece tu contraseña - Historia Clínica Común";

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final boolean envioHabilitado;
    private final String remitente;

    public EmailServiceImpl(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.enabled:false}") boolean envioHabilitado,
            @Value("${app.mail.from:no-reply@historiaclinicacomun.local}") String remitente) {
        this.mailSenderProvider = mailSenderProvider;
        this.envioHabilitado = envioHabilitado;
        this.remitente = remitente;
    }

    @Override
    public void enviarEnlaceRestablecimientoPassword(String destinatario, String enlace) {
        String cuerpo = construirCuerpoRestablecimiento(enlace);

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (!envioHabilitado || mailSender == null) {
            log.info("Envío de correo desactivado (app.mail.enabled={}). Enlace de restablecimiento para {}: {}",
                    envioHabilitado, LogMaskUtil.enmascarar(destinatario), enlace);
            return;
        }

        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject(ASUNTO_RESET);
            mensaje.setText(cuerpo);
            mailSender.send(mensaje);
            log.info("Correo de restablecimiento de contraseña enviado a {}", LogMaskUtil.enmascarar(destinatario));
        } catch (MailException e) {
            log.error("No se pudo enviar el correo de restablecimiento a {}: {}",
                    LogMaskUtil.enmascarar(destinatario), e.getMessage());
        }
    }

    private String construirCuerpoRestablecimiento(String enlace) {
        return """
                Hemos recibido una solicitud para restablecer la contraseña de tu cuenta.

                Abre el siguiente enlace para elegir una nueva contraseña:

                %s

                Si no has solicitado este cambio, puedes ignorar este mensaje: tu contraseña
                actual seguirá siendo válida. El enlace caduca automáticamente por seguridad.
                """.formatted(enlace);
    }
}
