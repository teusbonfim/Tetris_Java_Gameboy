package com.tetris.view;

import java.awt.Color;

/**
 * Constantes de UI reutilizáveis (cores, tamanhos)
 */
public final class UIConstants {
    // Fundo das caixas (substitui Color.WHITE)
    public static final Color BOX_BG = new Color(0xC8, 0xC7, 0xA8);
    // Cor da borda / texto (substitui Color.BLACK)
    public static final Color BORDER_COLOR = new Color(0x27, 0x2A, 0x19);
    // Espessura de borda padrão (pixels)
    public static final int BORDER_WIDTH = 30;
    // Borda externa adicional
    public static final Color OUTER_BORDER_COLOR = new Color(0xC8, 0xC7, 0xA8);
    public static final int OUTER_BORDER_WIDTH = 5;

    private UIConstants() {}
}
