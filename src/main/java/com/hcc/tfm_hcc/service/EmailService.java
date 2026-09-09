package com.hcc.tfm_hcc.service;

/**
 * Envío de correo transaccional de la aplicación.
 *
 * <p>Por ahora solo cubre el correo del flujo de restablecimiento de contraseña.
 * El envío efectivo depende de la propiedad {@code app.mail.enabled}: mientras
 * esté desactivada (desarrollo y tests), la implementación se limita a registrar
 * el contenido en el log y no contacta con ningún servidor SMTP.</p>
 */
public interface EmailService {

    /**
     * Envía al usuario el enlace de un solo uso para restablecer su contraseña.
     *
     * @param destinatario dirección de correo de la cuenta
     * @param enlace URL completa del frontend con el token de restablecimiento
     */
    void enviarEnlaceRestablecimientoPassword(String destinatario, String enlace);
}
