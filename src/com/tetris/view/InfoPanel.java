package com.tetris.view;

import com.tetris.model.Board;
import com.tetris.model.Piece;
import com.tetris.model.Shape;
import com.tetris.model.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.Cursor;

/**
 * Painel responsável por exibir as informações do jogo (pontuação, nível, etc.).
 * Esta é uma classe puramente visual (View).
 */
public class InfoPanel extends JPanel {

    private static final int PANEL_WIDTH = 250;
    private static final int SQUARE_PREVIEW_SIZE = 20;

    private Board board;
    private Theme currentTheme;
    private JButton pauseButton;
    private BufferedImage pauseIcon;

    // Array com os comandos do jogo
    private static final String[] GAME_COMMANDS = {
        "← MOVER ESQUERDA",
        "→ MOVER DIREITA",
        "↓ MOVER BAIXO",
        "↑ ROTACIONAR",
        "Z ROTACIONAR",
        "ESPAÇO SOLTAR",
        "P PAUSAR",
        "G SOMBRA",
        "T TEMA"
    };

    public InfoPanel() {
        this.currentTheme = Theme.AVAILABLE_THEMES[0];
        setPreferredSize(new Dimension(PANEL_WIDTH, 1)); // A altura será definida pelo layout
        setBackground(currentTheme.uiBackground());
        setLayout(null); // Para posicionamento absoluto do botão de pause
        
        // Inicializa e configura o botão de pause
        initPauseButton();
    }

    private void initPauseButton() {
        pauseButton = new JButton();
        loadPauseIcon();

        // Configuração do botão
        pauseButton.setContentAreaFilled(false);
        pauseButton.setBorderPainted(false);
        pauseButton.setFocusPainted(false);
        pauseButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pauseButton.setToolTipText("Pausar jogo (P)");
        
        if (pauseIcon != null) {
            // Dimensiona o ícone para 40x40 pixels
            Image scaledIcon = pauseIcon.getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            pauseButton.setIcon(new ImageIcon(scaledIcon));
        }

        // Adiciona o botão ao painel
        add(pauseButton);

        // Adiciona listener de redimensionamento para manter o botão posicionado corretamente
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updatePauseButtonPosition();
            }
        });
    }

    private void updatePauseButtonPosition() {
        if (pauseButton != null) {
            // Posiciona o botão na parte inferior do painel
            int buttonSize = 60;
            int x = (getWidth() - buttonSize) / 2;
            int y = getHeight() - buttonSize -35; // 25 pixels de margem inferior
            pauseButton.setBounds(x, y, buttonSize, buttonSize);
        }
    }

    public void updateInfo(Board board) {
        this.board = board;
        if (pauseButton != null) {
            pauseButton.setVisible(board != null && board.isStarted());
        }
        repaint();
    }
    
    public void updateTheme(Theme theme) {
        this.currentTheme = theme;
        setBackground(theme.uiBackground());
    }

    private void loadPauseIcon() {
        try {
            java.net.URL url = getClass().getResource("/com/tetris/view/resources/button/pause.png");
            if (url != null) {
                pauseIcon = ImageIO.read(url);
            } else {
                // tenta caminho relativo no filesystem (útil durante desenvolvimento)
                java.io.File f = new java.io.File("src/com/tetris/view/resources/button/pause.png");
                if (f.exists()) {
                    pauseIcon = ImageIO.read(f);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar ícone de pausa: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (board == null || !board.isStarted()) {
            return;
        }

        drawGameInfo((Graphics2D) g);
    }
    
    /**
     * Desenha a interface de informações do jogo com um design aprimorado.
     * @param g2d O contexto gráfico 2D para um melhor desenho.
     */
    private void drawGameInfo(Graphics2D g2d) {
        // Ativa o anti-aliasing para bordas mais suaves
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color textColor = (currentTheme.uiBackground().getRed() < 128) ? UIConstants.BOX_BG : UIConstants.BORDER_COLOR;
        
        int padding = 20;
        int blockWidth = PANEL_WIDTH - (2 * padding);
        int blockHeight = 60;
        int spacing = 15;
        
        int currentY = 40;

        // Desenha blocos de informação
        currentY = drawInfoBlock(g2d, "PONTUAÇÃO", String.format("%06d", board.getScore()), padding, currentY, blockWidth, blockHeight, textColor);
        currentY += spacing;
        
        // Nível em bloco completo
        currentY = drawInfoBlock(g2d, "NÍVEL", String.format("%02d", board.getLevel()), padding, currentY, blockWidth, blockHeight, textColor);
        currentY += spacing;
        
        // Linhas em bloco completo
        currentY = drawInfoBlock(g2d, "LINHAS", String.format("%03d", board.getLinesCleared()), padding, currentY, blockWidth, blockHeight, textColor);
        currentY += spacing;

        // Bloco da Próxima Peça com altura aumentada
        currentY = drawNextPiecePanel(g2d, "PRÓXIMA PEÇA", padding, currentY, blockWidth, 135, textColor);
        currentY += spacing;

        // Seção de Comandos
        drawCommandsSection(g2d, padding, currentY, blockWidth);
    }

    private void drawCommandsSection(Graphics2D g, int x, int y, int width) {
        // Configuração da fonte
        Font titleFont = new Font("Consolas", Font.BOLD, 18);
        Font commandFont = new Font("Consolas", Font.BOLD,16);
        
        // Desenha o título com a cor da borda
        g.setColor(UIConstants.BOX_BG);
        g.setFont(titleFont);
        FontMetrics fmTitle = g.getFontMetrics();
        String title = "COMANDOS";
        int titleX = x + (width - fmTitle.stringWidth(title)) / 2;
        int currentY = y + 20;
        g.drawString(title, titleX, currentY);
        
        // Configuração do espaçamento entre linhas
        g.setFont(commandFont);
        FontMetrics fmCommand = g.getFontMetrics();
        int lineHeight = fmCommand.getHeight() + 2; // +2 para um pequeno espaço adicional
        currentY += lineHeight + 5; // Espaço extra após o título

        // Desenha cada comando centralizado com a cor bege
        g.setColor(UIConstants.BOX_BG);
        for (String command : GAME_COMMANDS) {
            int commandX = x + (width - fmCommand.stringWidth(command)) / 2;
            g.drawString(command, commandX, currentY);
            currentY += lineHeight;
        }
    }

    /**
     * Helper para desenhar um bloco de informação estilizado.
     */
    private int drawInfoBlock(Graphics2D g, String title, String value, int x, int y, int width, int height, Color textColor) {
        // Cores via constantes centralizadas
        Color blockColor = UIConstants.BOX_BG;
        Color borderColor = UIConstants.BORDER_COLOR;

        // Desativa anti-aliasing para efeito pixelado
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        // Fundo branco
        g.setColor(blockColor);
        g.fillRect(x + 2, y + 2, width - 4, height - 4);
        
        // Borda preta de 2px com cantos pixelados
        g.setColor(borderColor);
        // Bordas horizontais
        g.fillRect(x + 2, y, width - 4, 2);           // Superior
        g.fillRect(x + 2, y + height - 2, width - 4, 2); // Inferior
        // Bordas verticais
        g.fillRect(x, y + 2, 2, height - 4);           // Esquerda
        g.fillRect(x + width - 2, y + 2, 2, height - 4); // Direita
        // Cantos pixelados
        g.fillRect(x + 2, y + 2, 2, 2);           // Superior esquerdo
        g.fillRect(x + width - 4, y + 2, 2, 2);   // Superior direito
        g.fillRect(x + 2, y + height - 4, 2, 2);  // Inferior esquerdo
        g.fillRect(x + width - 4, y + height - 4, 2, 2); // Inferior direito
        
        // Borda externa adicional (fora da caixa)
        g.setColor(UIConstants.OUTER_BORDER_COLOR);
        int ob = UIConstants.OUTER_BORDER_WIDTH;
        // superior
        g.fillRect(x - ob, y - ob, width + ob * 2, ob);
        // inferior
        g.fillRect(x - ob, y + height, width + ob * 2, ob);
        // esquerda
        g.fillRect(x - ob, y, ob, height);
        // direita
        g.fillRect(x + width, y, ob, height);
        
        // Centraliza e desenha o texto
        g.setColor(UIConstants.BORDER_COLOR);
        
        // Título
        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        FontMetrics fmTitle = g.getFontMetrics();
        int titleX = x + (width - fmTitle.stringWidth(title)) / 2;
        int titleY = y + 22;
        g.drawString(title, titleX, titleY);
        
        // Valor
        g.setFont(new Font("Consolas", Font.BOLD, 22));
        FontMetrics fmValue = g.getFontMetrics();
        int valueX = x + (width - fmValue.stringWidth(value)) / 2;
        int valueY = y + 48;
        g.drawString(value, valueX, valueY);

        return y + height;
    }

    /**
     * Helper para desenhar o painel da próxima peça.
     */
    private int drawNextPiecePanel(Graphics2D g, String title, int x, int y, int width, int height, Color textColor) {
        // Cores via constantes centralizadas
        Color blockColor = UIConstants.BOX_BG;
        Color borderColor = UIConstants.BORDER_COLOR;

        // Desativa anti-aliasing para efeito pixelado
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        // Fundo branco
        g.setColor(blockColor);
        g.fillRect(x + 2, y + 2, width - 4, height - 4);
        
        // Borda preta de 2px com cantos pixelados
        g.setColor(borderColor);
        // Bordas horizontais
        g.fillRect(x + 2, y, width - 4, 2);           // Superior
        g.fillRect(x + 2, y + height - 2, width - 4, 2); // Inferior
        // Bordas verticais
        g.fillRect(x, y + 2, 2, height - 4);           // Esquerda
        g.fillRect(x + width - 2, y + 2, 2, height - 4); // Direita
        // Cantos pixelados
        g.fillRect(x + 2, y + 2, 2, 2);           // Superior esquerdo
        g.fillRect(x + width - 4, y + 2, 2, 2);   // Superior direito
        g.fillRect(x + 2, y + height - 4, 2, 2);  // Inferior esquerdo
        g.fillRect(x + width - 4, y + height - 4, 2, 2); // Inferior direito
        
        // Borda externa adicional (fora da caixa)
        g.setColor(UIConstants.OUTER_BORDER_COLOR);
        int ob = UIConstants.OUTER_BORDER_WIDTH;
        // superior
        g.fillRect(x - ob, y - ob, width + ob * 2, ob);
        // inferior
        g.fillRect(x - ob, y + height, width + ob * 2, ob);
        // esquerda
        g.fillRect(x - ob, y, ob, height);
        // direita
        g.fillRect(x + width, y, ob, height);

        // Título centralizado
        g.setColor(UIConstants.BORDER_COLOR);
        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        FontMetrics fm = g.getFontMetrics();
        int titleX = x + (width - fm.stringWidth(title)) / 2;
        int titleY = y + 22;
        g.drawString(title, titleX, titleY);
        
        Piece nextPiece = board.getNextPiece();
        if (nextPiece != null) {
            // Centraliza a peça dentro do novo espaço maior
            int previewX = x + (width / 2) - (2 * SQUARE_PREVIEW_SIZE) + -2;
            int previewY = y + 45; // Corrigido para criar um vão em baixo
            for (int i = 0; i < 4; i++) {
                int px = previewX + (nextPiece.x(i) + 1) * SQUARE_PREVIEW_SIZE;
                int py = previewY + (1 - nextPiece.y(i)) * SQUARE_PREVIEW_SIZE;
                drawSquare(g, px, py, nextPiece.getShape(), SQUARE_PREVIEW_SIZE);
            }
        }
        return y + height;
    }

    private void drawSquare(Graphics g, int x, int y, Shape.Tetrominoe shape, int size) {
        Color[] colors = currentTheme.pieceColors();
        Color color = colors[shape.ordinal()];

        g.setColor(color);
        g.fillRect(x + 1, y + 1, size - 2, size - 2);

        g.setColor(color.brighter());
        g.drawLine(x, y + size - 1, x, y);
        g.drawLine(x, y, x + size - 1, y);

        g.setColor(color.darker());
        g.drawLine(x + 1, y + size - 1, x + size - 1, y + size - 1);
        g.drawLine(x + size - 1, y + size - 1, x + size - 1, y + 1);
    }

    public void setPauseActionListener(ActionListener listener) {
        if (pauseButton != null) {
            // Remove listeners anteriores para evitar duplicação
            for (ActionListener l : pauseButton.getActionListeners()) {
                pauseButton.removeActionListener(l);
            }
            // Adiciona o novo listener
            pauseButton.addActionListener(listener);
        }
    }
}