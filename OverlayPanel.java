package com.tetris.view;

import com.tetris.controller.GameController;
import com.tetris.model.Board;
import com.tetris.model.Theme;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

/**
 * Um painel transparente que desenha os 'overlays' (telas por cima do jogo),
 * como a tela de início, pausa e game over.
 */
public class OverlayPanel extends JPanel {

    private Board board;
    private GameController controller;
    // Fonte padrão usada para instruções e controles (mesma do "Pressione ENTER para Jogar")
    private Font uiFont = new Font("Consolas", Font.BOLD, 15);
    private JButton startButton;
    private Timer blinkTimer;
    private boolean showPress = true;
    private BufferedImage backgroundImage;
    // Configurações do bloco do texto "Pressione ENTER para Jogar" — ajuste estes valores para mudar tamanho/posicionamento
    private int enterRectPaddingX = 06; // espaço horizontal interno
    private int enterRectPaddingY = 10; // espaço vertical interno
    private int enterRectCornerArc = 0; // arredondamento do retângulo
    private int enterRectOffsetFromCenter = -50; // deslocamento vertical relativo ao centro (positivo desce, negativo sobe)
    private javax.swing.JTextField nameField;
    private int nameFieldYOffset = 5;

    public OverlayPanel() {
        setOpaque(false); // Torna o painel transparente
        loadBackgroundImage();
        setLayout(null);
        initNameField();
        initStartButton();
        initBlinker();
        
        // Atualiza o layout quando o painel for redimensionado
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutComponents();
            }
        });
    }

    private void initStartButton() {
        startButton = new JButton("START");
        startButton.setFont(new Font("Consolas", Font.BOLD, 22));
        startButton.setFocusable(false);
        startButton.setBackground(new Color(0xc8, 0xc7, 0xa8)); // #c8c7a8
    javax.swing.border.Border innerStartBorder = javax.swing.BorderFactory.createLineBorder(new Color(0x27, 0x2a, 0x19), 5);
    javax.swing.border.Border outerStartBorder = javax.swing.BorderFactory.createLineBorder(UIConstants.OUTER_BORDER_COLOR, UIConstants.OUTER_BORDER_WIDTH);
    startButton.setBorder(javax.swing.BorderFactory.createCompoundBorder(outerStartBorder, innerStartBorder));
        startButton.setForeground(new Color(0xA9, 0xA9, 0xA9)); // Cinza inicial
        
        // Dimensões e posicionamento do botão
        int w = 180;
        int h = 48;
        int x = (getWidth() - w) / 2;
        int y = getHeight() / 2 + 50; // 50 pixels abaixo do centro
        startButton.setBounds(x, y, w, h);
        
        // Adiciona ação ao botão
        startButton.addActionListener(e -> {
            if (controller != null && nameField != null) {
                String name = nameField.getText();
                if (name != null && !name.trim().isEmpty() && !name.equals("NOME")) {
                    controller.setPlayerName(name);
                    controller.startGameFromUI();
                }
            }
        });
        
        // Listener para atualizar a cor do texto baseado no conteúdo do campo nome
        nameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void updateButtonState() {
                String text = nameField.getText();
                boolean hasValidText = text != null && !text.trim().isEmpty() && !text.equals("NOME");
                startButton.setForeground(hasValidText ? new Color(0x27, 0x2a, 0x19) : new Color(0xA9, 0xA9, 0xA9));
                startButton.setEnabled(hasValidText);
            }
            
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateButtonState(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateButtonState(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateButtonState(); }
        });
        
        add(startButton);
    }

    private void loadBackgroundImage() {
        try {
            // Tenta localizar a imagem no classpath em vários locais
            java.net.URL url = null;
            // 1) caminho absoluto padrão: /com/tetris/view/resources/menu_bg.png
            url = getClass().getResource("/com/tetris/view/resources/menu_bg.png");
            // 2) antigo caminho alternativo: /resources/menu_bg.png
            if (url == null) url = getClass().getResource("/resources/menu_bg.png");

            if (url != null) {
                backgroundImage = ImageIO.read(url);
                System.out.println("OverlayPanel: loaded background image from classpath: " + url);
                return;
            }

            // 3) fallback: tenta carregar diretamente do sistema de arquivos (projeto)
            java.io.File f = new java.io.File("src/com/tetris/view/resources/menu_bg.png");
            if (f.exists()) {
                backgroundImage = ImageIO.read(f);
                System.out.println("OverlayPanel: loaded background image from filesystem: " + f.getAbsolutePath());
                return;
            }

            // Nenhuma imagem encontrada
            backgroundImage = null;
            System.out.println("OverlayPanel: no background image found (looked in classpath and src/ path)");
        } catch (IOException e) {
            backgroundImage = null;
            System.out.println("OverlayPanel: error loading background image: " + e.getMessage());
        }
    }

    private void initBlinker() {
        blinkTimer = new Timer(600, e -> {
            showPress = !showPress;
            repaint();
        });
        blinkTimer.setInitialDelay(0);
        blinkTimer.start();
    }

    private void initNameField() {
        // Campo de nome do jogador (apenas o campo de texto; label removido)
        nameField = new javax.swing.JTextField();
        nameField.setFont(uiFont);
        nameField.setVisible(false);
        nameField.setBackground(new Color(0xc8, 0xc7, 0xa8)); // Cor de fundo #c8c7a8
        // Borda principal alterada para #272a19 com padding interno à esquerda de 2px
        javax.swing.border.Border innerNameBorder = javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new Color(0x27, 0x2a, 0x19), 5),
            javax.swing.BorderFactory.createEmptyBorder(5, 10, 0, 0)
        );
        javax.swing.border.Border outerNameBorder = javax.swing.BorderFactory.createLineBorder(UIConstants.OUTER_BORDER_COLOR, UIConstants.OUTER_BORDER_WIDTH);
        nameField.setBorder(javax.swing.BorderFactory.createCompoundBorder(outerNameBorder, innerNameBorder));
        // Texto do campo também na cor solicitada
        Color uiTextColor = new Color(0x27, 0x2a, 0x19);
        nameField.setForeground(uiTextColor);
        add(nameField);

        // Placeholder "Nome" que desaparece ao focar/começar a digitar
        final String placeholder = "NOME";
        nameField.setText(placeholder);
        nameField.setForeground(uiTextColor);
        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (nameField.getText() != null && nameField.getText().equals(placeholder)) {
                    nameField.setText("");
                    nameField.setForeground(uiTextColor);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                    nameField.setText(placeholder);
                    nameField.setForeground(uiTextColor);
                }
            }
        });

        // Adiciona um listener para acionar o jogo quando o usuário pressionar ENTER
        nameField.addActionListener(e -> {
            String name = nameField.getText();
            if (name != null && !name.trim().isEmpty() && !name.equals(placeholder)) {
                if (controller != null) {
                    controller.setPlayerName(name);
                    controller.startGameFromUI();
                }
            }
        });
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    /**
     * Solicita foco para o campo de nome (usado quando o controller precisa que o usuário digite o nome).
     */
    public void requestFocusForName() {
        if (nameField != null) {
            nameField.requestFocusInWindow();
        }
    }

    /**
     * Atualiza o estilo do overlay com base no tema atual.
     */
    public void updateTheme(Theme theme) {
        if (theme == null) return;
        repaint();
    }

    private void layoutComponents() {
        if (nameField == null || startButton == null) return;
        
        // Dimensões para o campo de nome
        int nameW = 220;
        int nameH = 40;

        // Dimensões para o botão START
        int buttonW = 180;
        int buttonH = 48;

        // Centraliza os componentes
        int centerY = getHeight() / 2;
        int nameY = centerY - 30; // Campo de nome um pouco acima do centro
        int buttonY = centerY + 30; // Botão um pouco abaixo do centro
        
        int nameX = (getWidth() - nameW) / 2;
        int buttonX = (getWidth() - buttonW) / 2;
        
        nameField.setBounds(nameX, nameY, nameW, nameH);
        startButton.setBounds(buttonX, buttonY, buttonW, buttonH);
    }

    public void updateBoard(Board board) {
        this.board = board;
        boolean showMenu = (board != null && !board.isStarted());
        
        if (nameField != null) {
            nameField.setVisible(showMenu);
        }
        if (startButton != null) {
            startButton.setVisible(showMenu);
        }
        if (showMenu) {
            layoutComponents();
            // Atualiza o estado inicial do botão
            if (nameField != null && startButton != null) {
                String text = nameField.getText();
                boolean hasValidText = text != null && !text.trim().isEmpty() && !text.equals("NOME");
                startButton.setForeground(hasValidText ? new Color(0x27, 0x2a, 0x19) : new Color(0xA9, 0xA9, 0xA9));
                startButton.setEnabled(hasValidText);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (board == null) {
            return;
        }

        // Decide qual overlay desenhar com base no estado do jogo
        if (!board.isStarted()) {
            drawStartScreen(g);
        } else if (board.isGameOver()) {
            drawGameOver(g);
        } else if (board.isPaused()) {
            drawPaused(g);
        }
    }

    private void drawStartScreen(Graphics g) {
        // Desenha imagem de fundo se existir, caso contrário usa um overlay escuro
        if (backgroundImage != null) {
            // escala a imagem para preencher o painel com um fator de escala de 1.2 (20% maior)
            int scaledWidth = (int)(getWidth() * 1.0);
            int scaledHeight = (int)(getHeight() * 1.0);
            // centraliza a imagem maior
            int x = (getWidth() - scaledWidth) / 2;
            int y = (getHeight() - scaledHeight) / 2;
            g.drawImage(backgroundImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH), x, y, null);
            // aplica uma camada semi-transparente para aumentar contraste do texto
            g.setColor(new Color(0, 0, 0, 0));
            g.fillRect(0, 0, getWidth(), getHeight());
        } else {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        
        // Desenha o bloco do texto de instrução (fixo) e faz apenas o texto piscar
        // usa a fonte UI definida
        Font f = uiFont;
        java.awt.FontMetrics fm = g.getFontMetrics(f);
        String text = "PRESSIONE START PARA JOGAR";
        int stringWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        int paddingX = enterRectPaddingX;
        int paddingY = enterRectPaddingY;
        int rectW = stringWidth + paddingX * 2;
        int rectH = textHeight + paddingY * 2;
        int rectX = (getWidth() - rectW) / 2;
        // posiciona o bloco relativo ao centro e aplicar offset configurável
        int centerY = getHeight() / 2;
        int rectY = centerY - rectH + enterRectOffsetFromCenter;

        // desenha o bloco de fundo com cor #c8c7a8 (sempre exibido)
        g.setColor(new Color(0xc8, 0xc7, 0xa8));
        g.fillRoundRect(rectX, rectY, rectW, rectH, enterRectCornerArc, enterRectCornerArc);

        // desenha o texto apenas quando showPress == true (texto pisca)
        if (showPress) {
            g.setFont(f);
            g.setColor(new Color(0x27, 0x2a, 0x19));
            int textX = rectX + paddingX;
            int textBaseline = rectY + paddingY + fm.getAscent();
            g.drawString(text, textX, textBaseline);
        }
    }

    private void drawGameOver(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(new Color(0xc8, 0xc7, 0xa8));
        g.setFont(new Font("Consolas", Font.BOLD, 36));
        g.drawString("GAME OVER", getWidth()/2 - 80, getHeight() / 2);
        g.setFont(new Font("Consolas", Font.PLAIN, 18));
        g.drawString("  ENTER para reiniciar", getWidth()/2 - 110, getHeight() / 2 + 40);
    }
    
    private void drawPaused(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Título PAUSADO
        g.setColor(new Color(0xc8, 0xc7, 0xa8));
        g.setFont(new Font("Consolas", Font.BOLD, 36));
        g.drawString("PAUSADO", getWidth() / 2 - 70, getHeight() / 2 - 150);

        // Painel de records com borda
        int recordPanelWidth = 400;
        int recordPanelHeight = 200;
        int x = (getWidth() - recordPanelWidth) / 2;
        int y = (getHeight() - recordPanelHeight) / 2 - 20;

        // Desativa anti-aliasing para efeito pixelado
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        // Fundo usando constantes
        g.setColor(UIConstants.BOX_BG);
        g.fillRect(x + 2, y + 2, recordPanelWidth - 4, recordPanelHeight - 4);
        
        // Borda usando constantes
        g.setColor(UIConstants.BORDER_COLOR);
        // Bordas horizontais
        g.fillRect(x + 2, y, recordPanelWidth - 4, 2); // Superior
        g.fillRect(x + 2, y + recordPanelHeight - 2, recordPanelWidth - 4, 2); // Inferior
        // Bordas verticais
        g.fillRect(x, y + 2, 2, recordPanelHeight - 4); // Esquerda
        g.fillRect(x + recordPanelWidth - 2, y + 2, 2, recordPanelHeight - 4); // Direita
        // Cantos pixelados
        g.fillRect(x + 2, y + 2, 2, 2);// Superior esquerdo
        g.fillRect(x + recordPanelWidth - 4, y + 2, 2, 2); // Superior direito
        g.fillRect(x + 2, y + recordPanelHeight - 4, 2, 2); // Inferior esquerdo
        g.fillRect(x + recordPanelWidth - 4, y + recordPanelHeight - 4, 2, 2); // Inferior direito

        // Borda externa adicional (fora da caixa)
        g.setColor(UIConstants.OUTER_BORDER_COLOR);
        int ob = UIConstants.OUTER_BORDER_WIDTH;
        // superior
        g.fillRect(x - ob, y - ob, recordPanelWidth + ob * 2, ob);
        // inferior
        g.fillRect(x - ob, y + recordPanelHeight, recordPanelWidth + ob * 2, ob);
        // esquerda
        g.fillRect(x - ob, y, ob, recordPanelHeight);
        // direita
        g.fillRect(x + recordPanelWidth, y, ob, recordPanelHeight);

        // Lista os records
        java.util.List<String> history = com.tetris.db.Database.getGameHistory();
        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        int lineHeight = g.getFontMetrics().getHeight();
        
        // Espaço horizontal para o texto (garante que fique dentro do painel)
        int textX = x + 15; // Reduzido para 15px de margem
        int textY = y + 30;
        
        // *** MUDANÇA: Define a cor do texto para #272a19 ***
        g.setColor(new Color(0x27, 0x2a, 0x19));
        
        // Título "MELHORES PONTUAÇÕES"
        g.setFont(new Font("Consolas", Font.BOLD, 16));
        g.drawString("MELHORES PONTUAÇÕES", textX, textY);
        textY += lineHeight + 5; // Ajuste para melhor espaçamento
        
        // Lista as pontuações
        g.setFont(new Font("Consolas", Font.PLAIN, 14));
        int maxLines = 6; // Reduzido para 6, para caber com título e margens
        int count = 0;
        for (String score : history) {
            if (count >= maxLines) break;
            g.drawString(score, textX, textY);
            textY += lineHeight;
            count++;
        }

        // Instrução para continuar (voltando para a cor do PAUSADO para contraste)
        g.setColor(new Color(0xc8, 0xc7, 0xa8)); 
        g.setFont(new Font("Consolas", Font.BOLD, 18));
        int bottomTextY = y + recordPanelHeight + 40;
        g.drawString("Pressione P para continuar", getWidth() / 2 - 125, bottomTextY);
    }


}

