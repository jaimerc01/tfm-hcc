package com.hcc.tfm_hcc.service.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.hcc.tfm_hcc.model.ArchivoClinico;
import com.hcc.tfm_hcc.model.Usuario;
import com.hcc.tfm_hcc.repository.ArchivoClinicoRepository;
import com.hcc.tfm_hcc.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArchivoClinicoServiceImpl implements com.hcc.tfm_hcc.service.ArchivoClinicoService {

    private final ArchivoClinicoRepository archivoClinicoRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.uploads.base-dir:uploads}")
    private String baseDir;

    @Value("${app.uploads.max-size-bytes:10485760}") // 10 MB por defecto
    private long maxSizeBytes;

    @Value("${app.uploads.allowed-types:}") // vacío = sin restricción
    private String allowedTypes;

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) {
            throw new IllegalStateException("Usuario no autenticado");
        }
        return ((Usuario) auth.getPrincipal()).getId();
    }

    private Path userDir(UUID userId) {
        return Paths.get(baseDir, userId.toString());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArchivoClinico> listMine() {
        UUID userId = getCurrentUserId();
        return archivoClinicoRepository.findByUsuarioIdOrderByFechaCreacionDesc(userId);
    }

    @Override
    @Transactional
    public ArchivoClinico uploadMine(MultipartFile file) throws IOException {
        if (file == null) throw new IllegalArgumentException("No se ha enviado ningún archivo");
        if (file.isEmpty()) throw new IllegalArgumentException("El archivo está vacío");
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("El archivo supera el tamaño máximo permitido");
        }
        UUID userId = getCurrentUserId();
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        String rawName = file.getOriginalFilename();
    if (rawName == null || rawName.isBlank()) rawName = "archivo";
        String originalFilename = StringUtils.cleanPath(rawName);
        if (originalFilename.contains("..") || originalFilename.startsWith("/") || originalFilename.startsWith("\\")) {
            throw new IllegalArgumentException("Nombre de archivo no válido");
        }
        // Validación de content-type si hay lista permitida
        String ctype = file.getContentType();
        if (allowedTypes != null && !allowedTypes.isBlank()) {
            boolean ok = false;
            for (String t : allowedTypes.split(",")) {
                String tt = t.trim();
                if (tt.isEmpty()) continue;
                if ("*/*".equals(tt)) { ok = true; break; }
                if (ctype != null && (ctype.equalsIgnoreCase(tt) || ctype.toLowerCase().startsWith(tt.toLowerCase().replace("*","")))) {
                    ok = true; break;
                }
            }
            if (!ok) {
                throw new IllegalArgumentException("Tipo de archivo no permitido");
            }
        }
        String ext = "";
        int dot = originalFilename.lastIndexOf('.');
        if (dot > 0 && dot < originalFilename.length() - 1) {
            ext = originalFilename.substring(dot);
        }
        String storedName = UUID.randomUUID().toString() + ext;
        Path dir = userDir(userId);
        Files.createDirectories(dir);
        Path dest = dir.resolve(storedName);
        try {
            Files.copy(file.getInputStream(), dest);
        } catch (IOException ex) {
            throw new IOException("No se pudo guardar el archivo", ex);
        }

        ArchivoClinico ac = new ArchivoClinico();
        ac.setUsuario(usuario);
        ac.setNombreOriginal(originalFilename);
        ac.setContentType(ctype);
        ac.setSizeBytes(file.getSize());
        ac.setRutaAlmacenada(dest.toString());
    ac.setFechaCreacion(LocalDateTime.now());
    ac.setFechaUltimaModificacion(LocalDateTime.now());
        return archivoClinicoRepository.save(ac);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getMineResource(UUID id) {
        UUID userId = getCurrentUserId();
        ArchivoClinico ac = archivoClinicoRepository.findByIdAndUsuarioId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el archivo"));
        try {
            Path p = Paths.get(ac.getRutaAlmacenada());
            Resource res = new UrlResource(p.toUri());
            if (!res.exists() || !res.isReadable()) {
                throw new IllegalStateException("Archivo no accesible");
            }
            return res;
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Ruta inválida", e);
        }
    }

    @Override
    @Transactional
    public void deleteMine(UUID id) throws IOException {
        UUID userId = getCurrentUserId();
        ArchivoClinico ac = archivoClinicoRepository.findByIdAndUsuarioId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el archivo"));
        Path p = Paths.get(ac.getRutaAlmacenada());
        archivoClinicoRepository.delete(ac);
        if (Files.exists(p)) {
            Files.delete(p);
        }
    }
}
