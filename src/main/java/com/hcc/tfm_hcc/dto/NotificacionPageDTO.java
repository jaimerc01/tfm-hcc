package com.hcc.tfm_hcc.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Página de notificaciones del usuario autenticado.
 *
 * <p>{@code total} es el número total de notificaciones vigentes (no eliminadas)
 * del usuario, no el tamaño de {@code items}: el frontend lo usa para decidir si
 * mostrar el botón de "cargar más" ({@code items.size() < total}).</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionPageDTO {
    private List<NotificacionDTO> items;
    private long total;
}
