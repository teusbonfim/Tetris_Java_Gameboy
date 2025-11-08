package com.tetris.view;

import com.tetris.db.Database;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.List;

/**
 * Diálogo simples que exibe o histórico de partidas retornado por Database.getGameHistory().
 */
public class HistoryDialog extends JDialog {

    public HistoryDialog(Frame owner) {
        super(owner, "Histórico de Partidas", false);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        List<String> history = Database.getGameHistory();

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        if (history.isEmpty()) {
            textArea.setText("Nenhuma partida registrada ainda.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String line : history) {
                sb.append(line).append('\n');
            }
            textArea.setText(sb.toString());
        }

        JScrollPane scroll = new JScrollPane(textArea);
        add(scroll, BorderLayout.CENTER);

        // Não roubar foco da janela principal — apenas visualização
        setFocusableWindowState(false);
        setAlwaysOnTop(true);

        setSize(420, 300);
        setLocationRelativeTo(getOwner());
    }
}
// EXCLUA ESTE ARQUIVO (HistoryDialog.java) DO SEU PROJETO.