package com.tetris.view;

import com.tetris.model.Theme;

import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Font;
import javax.swing.border.Border;

/**
 * Painel principal que contém e organiza os outros componentes da view.
 * Usa um BorderLayout para posicionar o tabuleiro e o painel de informações.
 */
public class GamePanel extends JPanel {

    private BoardPanel boardPanel;
    private InfoPanel infoPanel;
    // private JButton historyButton; // REMOVIDO: Variável de instância para o botão Histórico

    public GamePanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        boardPanel = new BoardPanel();
        infoPanel = new InfoPanel();

        // Painel lateral que contém as info visual e botões
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BorderLayout());
        sidePanel.add(infoPanel, BorderLayout.CENTER);

        // Painel para botões embaixo (agora estará vazio ou pode ser removido, mas o mantenho
        // para preservar a estrutura de layout, caso algo mais seja adicionado no futuro)
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        /* // REMOVIDO: Criação e configuração do Botão "Histórico"
        historyButton = new JButton("Histórico");
        historyButton.setToolTipText("Ver histórico de partidas");

        // Estiliza o botão para combinar com o painel lateral
        styleHistoryButton(historyButton, infoPanel.getBackground());

        historyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Abre diálogo de histórico sem roubar foco. Em seguida, garante que o GamePanel recupera o foco.
                JFrame top = (JFrame) SwingUtilities.getWindowAncestor(GamePanel.this);
                HistoryDialog dlg = new HistoryDialog(top);
                dlg.setModal(false);
                dlg.setVisible(true);

                // Restaurar foco ao painel do jogo (pequeno atraso para garantir que a janela já está visível)
                SwingUtilities.invokeLater(() -> GamePanel.this.requestFocusInWindow());
            }
        });
        buttonsPanel.add(historyButton);
        */
        
        sidePanel.add(buttonsPanel, BorderLayout.SOUTH);

        add(boardPanel, BorderLayout.CENTER);
        add(sidePanel, BorderLayout.EAST);
    }

    /**
     * Atualiza o tema de todos os componentes visuais filhos.
     */
    public void updateTheme(Theme theme) {
        infoPanel.updateTheme(theme);
        boardPanel.updateTheme(theme);
        /*
        // REMOVIDO: Chamada de estilo do botão
        if (historyButton != null) {
            styleHistoryButton(historyButton, infoPanel.getBackground());
        }
        */
    }

    /**
     * Aplica estilo ao botão de histórico de acordo com a cor de fundo do painel lateral.
     * MANTIDO COMO MÉTODO PRIVADO NÃO UTILIZADO, CASO VOCÊ QUEIRA REINSERIR UM BOTÃO FUTURAMENTE.
     */
    private void styleHistoryButton(JButton btn, Color bg) {
        Color blockColor = bg.darker();
        Color borderColor = bg.brighter();
        Color textColor = (bg.getRed() < 128) ? UIConstants.BOX_BG : UIConstants.BORDER_COLOR;

        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBackground(blockColor);
        btn.setForeground(textColor);
        btn.setFont(new Font("Consolas", Font.BOLD, 13));
        javax.swing.border.Border outer = BorderFactory.createLineBorder(UIConstants.OUTER_BORDER_COLOR, UIConstants.OUTER_BORDER_WIDTH);
        javax.swing.border.Border inner = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borderColor, 1), BorderFactory.createEmptyBorder(6, 12, 6, 12));
        Border b = BorderFactory.createCompoundBorder(outer, inner);
        btn.setBorder(b);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(140, 36));
    }


    // --- Getters para o Controller ---

    public BoardPanel getBoardPanel() {
        return boardPanel;
    }

    public InfoPanel getInfoPanel() {
        return infoPanel;
    }
}