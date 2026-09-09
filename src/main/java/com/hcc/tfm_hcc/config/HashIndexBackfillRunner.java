package com.hcc.tfm_hcc.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.hcc.tfm_hcc.model.DatoClinico;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.DatoClinicoRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;
import com.hcc.tfm_hcc.service.HmacSearchIndexService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Rellena, al arrancar la aplicación, los índices de búsqueda (HMAC) de las filas
 * que todavía no los tengan calculados: {@code Usuario.nifHash}/{@code emailHash} y
 * {@code DatoClinico.tipoHash}.
 *
 * <p>Es necesario porque el cifrado de {@code nif}, {@code email} y {@code tipo} pasó de
 * ser determinista a AES/GCM no determinista (ver {@link com.hcc.tfm_hcc.converter.AESEncryptionConverter}),
 * y las búsquedas por igualdad ahora dependen de estas columnas adicionales
 * (ver {@link HmacSearchIndexService}). Las filas creadas antes de ese cambio no tienen
 * el hash calculado todavía; este runner las recorre una única vez y las actualiza.</p>
 *
 * <p>Es idempotente: en cada arranque solo procesa las filas cuyo hash esté a
 * {@code null}, así que una vez migrados todos los datos existentes no vuelve a hacer
 * nada. No falla el arranque de la aplicación si algo va mal: solo lo deja registrado
 * en el log, para no bloquear el resto de la aplicación por un problema de migración.</p>
 *
 * <p><b>Requiere que las columnas {@code nif_hash}, {@code email_hash} y {@code tipo_hash}
 * ya existan en la base de datos</b> (este proyecto no usa Flyway/Liquibase ni
 * {@code ddl-auto}, así que el esquema se gestiona a mano; ver el script SQL de
 * referencia en {@code src/main/resources/db/manual-migrations/}).</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HashIndexBackfillRunner implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final DatoClinicoRepository datoClinicoRepository;
    private final HmacSearchIndexService hmacSearchIndexService;

    @Override
    public void run(String... args) {
        try {
            backfillUsuarios();
        } catch (Exception e) {
            log.error("No se pudo completar el backfill de nifHash/emailHash de Usuario. "
                    + "¿Existen las columnas nif_hash y email_hash en la tabla usuario?", e);
        }

        try {
            backfillDatosClinicos();
        } catch (Exception e) {
            log.error("No se pudo completar el backfill de tipoHash de DatoClinico. "
                    + "¿Existe la columna tipo_hash en la tabla dato_clinico?", e);
        }
    }

    private void backfillUsuarios() {
        int actualizados = 0;
        for (Usuario usuario : usuarioRepository.findAll()) {
            boolean necesitaGuardar = false;

            if (usuario.getNifHash() == null && usuario.getNif() != null) {
                usuario.setNifHash(hmacSearchIndexService.indexar(usuario.getNif()));
                necesitaGuardar = true;
            }
            if (usuario.getEmailHash() == null && usuario.getEmail() != null) {
                usuario.setEmailHash(hmacSearchIndexService.indexar(usuario.getEmail()));
                necesitaGuardar = true;
            }

            if (necesitaGuardar) {
                usuarioRepository.save(usuario);
                actualizados++;
            }
        }
        if (actualizados > 0) {
            log.info("Backfill de índices de búsqueda: {} usuario(s) actualizados con nifHash/emailHash.", actualizados);
        }
    }

    private void backfillDatosClinicos() {
        int actualizados = 0;
        for (DatoClinico datoClinico : datoClinicoRepository.findAll()) {
            if (datoClinico.getTipoHash() == null && datoClinico.getTipo() != null) {
                datoClinico.setTipoHash(hmacSearchIndexService.indexar(datoClinico.getTipo()));
                datoClinicoRepository.save(datoClinico);
                actualizados++;
            }
        }
        if (actualizados > 0) {
            log.info("Backfill de índices de búsqueda: {} dato(s) clínico(s) actualizados con tipoHash.", actualizados);
        }
    }
}
