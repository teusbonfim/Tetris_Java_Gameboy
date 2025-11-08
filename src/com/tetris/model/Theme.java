package com.tetris.model;

import java.awt.Color;

/**
 * Representa um tema visual para o jogo, contendo todas as cores necessárias.
 * Usamos um 'record' para uma definição concisa e imutável de um tema.
 */
public record Theme(
    String name,
    Color uiBackground,
    Color boardBackground,
    Color grid,
    Color[] pieceColors // Array com 8 cores: a primeira é para 'NoShape', as outras 7 para as peças.
) {
    // --- Temas Pré-definidos ---

    /**
     * O tema escuro original do jogo.
     */
    public static final Theme CLASSIC_DARK = new Theme(
        "Novo Tema",
        new Color(0x27, 0x2a, 0x19),  // fundo UI - novo tema escuro para barra lateral
        new Color(0xc8, 0xc7, 0xa8),  // fundo tabuleiro - tema claro para área de jogo
        new Color(0x27, 0x2a, 0x19),  // grid - cor da borda
        new Color[] {
            new Color(0x27, 0x2a, 0x19),  // NoShape - cor da borda
            new Color(0x27, 0x2a, 0x19),  // ZShape - cor clara principal
            new Color(0x27, 0x2a, 0x19),  // SShape
            new Color(0x27, 0x2a, 0x19),  // LineShape
            new Color(0x27, 0x2a, 0x19),  // TShape
            new Color(0x27, 0x2a, 0x19),  // SquareShape
            new Color(0x27, 0x2a, 0x19),  // LShape
            new Color(0x27, 0x2a, 0x19)   // MirroredLShape
        }
    );

    /**
     * Um tema claro, com cores vibrantes.
     */
  public static final Theme LIGHT = new Theme(
    "Claro",
    new Color(230, 210, 255),  // fundo principal - lilás claro suave
    new Color(235, 225, 245),  // fundo do tabuleiro - roxo suave
    new Color(205, 195, 225),  // grid - roxo médio para linhas
    new Color[] {
        new Color(90, 60, 120),    // NoShape - roxo escuro
        new Color(186, 104, 200),  // Z - roxo-rosa vibrante
        new Color(149, 102, 230),  // S - roxo médio-claro
        new Color(140, 90, 210),   // Line - roxo médio
        new Color(170, 120, 240),  // T - roxo-azulado
        new Color(190, 150, 250),  // Square - lavanda clara
        new Color(160, 100, 220),  // L - roxo médio-escuro
        new Color(200, 160, 255)   // MirroredL - lilás claro
    }
);
        
    

    // Array que contém todos os temas disponíveis para fácil acesso.
    public static final Theme[] AVAILABLE_THEMES = { CLASSIC_DARK, LIGHT, };
}
