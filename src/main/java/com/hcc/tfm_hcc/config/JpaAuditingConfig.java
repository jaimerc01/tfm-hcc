package com.hcc.tfm_hcc.config;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Activa la auditoría automática de JPA ({@code @LastModifiedDate}) para que
 * {@link com.hcc.tfm_hcc.model.BaseEntity#getFechaUltimaModificacion()} se rellene
 * de forma consistente al crear o actualizar cualquier entidad, sin depender de que
 * cada servicio la establezca manualmente (y pueda olvidarse o usar una zona horaria
 * distinta al resto del proyecto).
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaAuditingConfig {

    private static final String ZONE_ID_EUROPA_MADRID = "Europe/Madrid";

    /**
     * Fuerza la zona horaria "Europe/Madrid" para las fechas de auditoría, en vez de
     * depender de la zona por defecto de la JVM (que puede variar entre el entorno de
     * desarrollo y el servidor de producción).
     */
    @Bean
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of((TemporalAccessor) LocalDateTime.now(ZoneId.of(ZONE_ID_EUROPA_MADRID)));
    }
}
