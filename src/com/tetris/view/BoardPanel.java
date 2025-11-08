package com.tetris.view;

import com.tetris.model.Board;
import com.tetris.model.Piece;
import com.tetris.model.Shape;
import com.tetris.model.Theme;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Painel responsável por desenhar o tabuleiro de jogo e as peças.
 * Esta é uma classe puramente visual (View).
 */
public class BoardPanel extends JPanel {

    private Board board;
    private Theme currentTheme;
    // Imagens das peças (mesmo índice do Shape.Tetrominoe.ordinal())
    private BufferedImage[] pieceImages;
    // Cache de imagens já escaladas para o tamanho atual
    private BufferedImage[] scaledImages;
    // Tamanho para o qual as imagens estão escaladas atualmente
    private int currentScaledSize;

    public BoardPanel() {
        this.currentTheme = Theme.AVAILABLE_THEMES[0];
        setPreferredSize(new Dimension(getSquareSize() * Board.BOARD_WIDTH, getSquareSize() * Board.BOARD_HEIGHT));
        // Tenta carregar imagens das peças; se não existirem, cai para o desenho por cor
        loadPieceImages();
    }

    /**
     * Tenta carregar imagens para cada tetromino a partir do classpath
     * em /com/tetris/view/resources/pieces/{shapeName}.png
     * onde {shapeName} é o nome do enum (ex: ZShape.png, LineShape.png, SquareShape.png...)
     */
    private void loadPieceImages() {
        Shape.Tetrominoe[] values = Shape.Tetrominoe.values();
        pieceImages = new BufferedImage[values.length];
        for (int i = 0; i < values.length; i++) {
            String name = values[i].name();
            // pula NoShape
            if (values[i] == Shape.Tetrominoe.NoShape) {
                pieceImages[i] = null;
                continue;
            }
            String path = "/com/tetris/view/resources/pieces/" + name + ".png";
            try {
                java.net.URL url = getClass().getResource(path);
                if (url != null) {
                    pieceImages[i] = ImageIO.read(url);
                } else {
                    // tenta caminho relativo no filesystem (útil durante desenvolvimento)
                    java.io.File f = new java.io.File("src/com/tetris/view/resources/pieces/" + name + ".png");
                    if (f.exists()) {
                        pieceImages[i] = ImageIO.read(f);
                    } else {
                        pieceImages[i] = null; // nenhum arquivo disponível
                    }
                }
            } catch (IOException ex) {
                pieceImages[i] = null;
                System.out.println("BoardPanel: falha ao carregar imagem " + path + " -> " + ex.getMessage());
            }
        }
    }

    public void updateBoard(Board board) {
        this.board = board;
    }

    public void updateTheme(Theme theme) {
        this.currentTheme = theme;
        // Força reescalonamento das imagens na próxima atualização
        this.currentScaledSize = -1;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (board == null) {
            return;
        }

        drawBoardBackground(g);
        drawGrid(g);
        drawGameOverLine(g);
        drawPlacedPieces(g);
        
        // Desenha a peça fantasma antes da peça real
        drawGhostPiece(g); 
        drawCurrentPiece(g);
    }

    private void drawBoardBackground(Graphics g) {
        g.setColor(currentTheme.boardBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        // Borda externa ao redor do tabuleiro
        g.setColor(UIConstants.OUTER_BORDER_COLOR);
        int ob = UIConstants.OUTER_BORDER_WIDTH;
        // topo
        g.fillRect(0, 0, getWidth(), ob);
        // base
        g.fillRect(0, getHeight() - ob, getWidth(), ob);
        // esquerda
        g.fillRect(0, 0, ob, getHeight());
        // direita
        g.fillRect(getWidth() - ob, 0, ob, getHeight());
    }

    /**
     * Desenha uma linha vermelha semi-transparente indicando a altura onde
     * game over acontece (quando uma nova peça não consegue ser posicionada).
     */
    private void drawGameOverLine(Graphics g) {
        int squareSize = getSquareSize();
        // Uma nova peça começa no topo + minY(); se não couber ali = game over
        int gameOverY = Board.BOARD_HEIGHT - 2; // 2 blocos mais abaixo
        int y = (Board.BOARD_HEIGHT - 1 - gameOverY) * squareSize;

        // Cria uma linha vermelha semi-transparente
        Color redLine = new Color(255, 0, 0, 128);
        g.setColor(redLine);
        g.fillRect(0, y - 2, getWidth(), 4);
    }

    private void drawGrid(Graphics g) {
        g.setColor(currentTheme.grid());
        int squareSize = getSquareSize();
        for (int i = 0; i <= Board.BOARD_WIDTH; i++) {
            g.drawLine(i * squareSize, 0, i * squareSize, getHeight());
        }
        for (int i = 0; i <= Board.BOARD_HEIGHT; i++) {
            g.drawLine(0, i * squareSize, getWidth(), i * squareSize);
        }
    }

    private void drawPlacedPieces(Graphics g) {
        int squareSize = getSquareSize();
        for (int i = 0; i < Board.BOARD_HEIGHT; i++) {
            for (int j = 0; j < Board.BOARD_WIDTH; j++) {
                Shape.Tetrominoe shape = board.shapeAt(j, i);
                if (shape != Shape.Tetrominoe.NoShape) {
                    drawSquare(g, j * squareSize, (Board.BOARD_HEIGHT - 1 - i) * squareSize, shape, false);
                }
            }
        }
    }

    private void drawCurrentPiece(Graphics g) {
        Piece currentPiece = board.getCurrentPiece();
        if (board.isStarted() && currentPiece.getShape() != Shape.Tetrominoe.NoShape) {
            int squareSize = getSquareSize();
            for (int i = 0; i < 4; i++) {
                int x = currentPiece.getX() + currentPiece.x(i);
                int y = currentPiece.getY() - currentPiece.y(i);
                if (y < Board.BOARD_HEIGHT) {
                    drawSquare(g, x * squareSize, (Board.BOARD_HEIGHT - 1 - y) * squareSize, currentPiece.getShape(), false);
                }
            }
        }
    }

    /**
     * Novo: Desenha a peça fantasma na sua posição final.
     */
    private void drawGhostPiece(Graphics g) {
        if (!board.isGhostPieceEnabled() || !board.isStarted()) {
            return;
        }
        
        Piece currentPiece = board.getCurrentPiece();
        if (currentPiece.getShape() == Shape.Tetrominoe.NoShape) {
            return;
        }

        int ghostY = board.getGhostPieceY();
        int squareSize = getSquareSize();

        for (int i = 0; i < 4; i++) {
            int x = currentPiece.getX() + currentPiece.x(i);
            int y = ghostY - currentPiece.y(i);
             if (y < Board.BOARD_HEIGHT) {
                drawSquare(g, x * squareSize, (Board.BOARD_HEIGHT - 1 - y) * squareSize, currentPiece.getShape(), true);
            }
        }
    }

    /**
     * Garante que temos versões escaladas das imagens no tamanho correto
     */
    private void ensureScaledImages(int size) {
        // Se já temos as imagens escaladas no tamanho correto, não faz nada
        if (scaledImages != null && currentScaledSize == size) {
            return;
        }

        // Inicializa o cache se necessário
        if (scaledImages == null) {
            scaledImages = new BufferedImage[Shape.Tetrominoe.values().length];
        }

        // Escala todas as imagens para o novo tamanho
        currentScaledSize = size;
        for (int i = 0; i < pieceImages.length; i++) {
            BufferedImage original = pieceImages[i];
            if (original != null) {
                BufferedImage scaled = new BufferedImage(size - 2, size - 2, BufferedImage.TYPE_INT_ARGB);
                Graphics g = scaled.createGraphics();
                g.drawImage(original, 0, 0, size - 2, size - 2, null);
                g.dispose();
                scaledImages[i] = scaled;
            } else {
                scaledImages[i] = null;
            }
        }
    }

    private void drawSquare(Graphics g, int x, int y, Shape.Tetrominoe shape, boolean isGhost) {
        int size = getSquareSize();
        Color[] colors = currentTheme.pieceColors();
        Color color = colors[shape.ordinal()];

        // Garante que temos as imagens escaladas no tamanho correto
        ensureScaledImages(size);

        // Usa a imagem escalada do cache, se disponível
        BufferedImage img = (scaledImages != null) ? scaledImages[shape.ordinal()] : null;
        if (img != null) {
            if (isGhost) {
                // Desenha a imagem com um contorno mais claro/escuro para efeito de ghost
                g.drawImage(img, x + 1, y + 1, null);
                g.setColor(new Color(0, 0, 0, 100));
                g.drawRect(x + 1, y + 1, size - 2, size - 2);
            } else {
                g.drawImage(img, x + 1, y + 1, null);
                // opcional: desenha bordas sutis para destacar a peça
                g.setColor(color.brighter());
                g.drawLine(x, y + size - 1, x, y);
                g.drawLine(x, y, x + size - 1, y);
                g.setColor(color.darker());
                g.drawLine(x + 1, y + size - 1, x + size - 1, y + size - 1);
                g.drawLine(x + size - 1, y + size - 1, x + size - 1, y + 1);
            }
        } else {
            // Fallback para o desenho por cor (comportamento antigo)
            if (isGhost) {
                g.setColor(color.darker()); // Cor para a peça fantasma
                g.drawRect(x + 1, y + 1, size - 2, size - 2); // Desenha apenas o contorno
            } else {
                g.setColor(color);
                g.fillRect(x + 1, y + 1, size - 2, size - 2);

                g.setColor(color.brighter());
                g.drawLine(x, y + size - 1, x, y);
                g.drawLine(x, y, x + size - 1, y);

                g.setColor(color.darker());
                g.drawLine(x + 1, y + size - 1, x + size - 1, y + size - 1);
                g.drawLine(x + size - 1, y + size - 1, x + size - 1, y + 1);
            }
        }
    }
    
    private int getSquareSize() {
        return 40;
    }
}

