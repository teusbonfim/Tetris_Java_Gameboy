package com.tetris.controller;

import com.tetris.db.Database;
import com.tetris.model.Board;
import com.tetris.model.Theme;
import com.tetris.view.GameFrame;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * O Controller no padrão MVC.
 * Faz a ponte entre o Model (Board) e a View (GameFrame).
 * Contém o game loop (Timer) e gere os inputs do utilizador.
 */
public class GameController extends KeyAdapter implements ActionListener {

    private static final int INITIAL_DELAY = 400;

    private final GameFrame gameFrame;
    private final Board board;
    private final Timer timer;
    private int currentThemeIndex = 0;
    private String playerName = "";

    public GameController(GameFrame gameFrame, Board board) {
        this.gameFrame = gameFrame;
        this.board = board;
        this.timer = new Timer(getDelayForLevel(), this);
        this.gameFrame.getGamePanel().addKeyListener(this);
        this.gameFrame.getGamePanel().setFocusable(true);
        
        // Adiciona o listener de pause ao botão
        this.gameFrame.getGamePanel().getInfoPanel().setPauseActionListener(e -> {
            if (board.isStarted() && !board.isGameOver()) {
                board.togglePause();
                updateView();
                // Retorna o foco ao painel do jogo
                gameFrame.getGamePanel().requestFocusInWindow();
            }
        });
    }

    /**
     * Método público para ser chamado pela UI (botão Start) para iniciar o jogo.
     * Repete a mesma lógica que o ENTER no keyPressed.
     */
    public void startGameFromUI() {
        if ((!board.isStarted() || board.isGameOver())) {
            // exige que o nome do jogador esteja definido
            if (playerName == null || playerName.trim().isEmpty()) {
                // solicita foco ao campo de nome no overlay
                if (gameFrame.getOverlayPanel() != null) {
                    gameFrame.getOverlayPanel().requestFocusForName();
                }
                return;
            }

            board.start();
            if (!timer.isRunning()) {
                timer.start();
            }
            // garante foco para receber teclado
            gameFrame.getGamePanel().requestFocusInWindow();
            updateView();
        }
    }

    public void setPlayerName(String name) {
        this.playerName = name == null ? "" : name.trim();
    }

    public void start() {
        timer.start();
        // Garantir que o painel tem foco para receber eventos de teclado
        gameFrame.getGamePanel().requestFocusInWindow();
        updateView(); 
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (board.isStarted() && !board.isPaused() && !board.isGameOver()) {
            board.movePieceDown();
        }
        
        if (board.isGameOver()) {
            // Parar o timer e gravar a sessão no DB
            timer.stop();
            Database.createTable();
            String playerToSave = (playerName == null || playerName.trim().isEmpty()) ? "Jogador1" : playerName;
            Database.saveGame(playerToSave, board.getScore(), board.getLevel(), board.getLinesCleared());
            updateView();
            return;
        }

        timer.setDelay(getDelayForLevel());
        updateView();
    }

    private void updateView() {
        gameFrame.getGamePanel().getBoardPanel().updateBoard(board);
        gameFrame.getGamePanel().getInfoPanel().updateInfo(board);
        gameFrame.getOverlayPanel().updateBoard(board);

        Theme currentTheme = Theme.AVAILABLE_THEMES[currentThemeIndex];
        gameFrame.getGamePanel().updateTheme(currentTheme);
        gameFrame.getOverlayPanel().updateTheme(currentTheme);

        gameFrame.repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keycode = e.getKeyCode();

        if (keycode == KeyEvent.VK_T) {
            currentThemeIndex = (currentThemeIndex + 1) % Theme.AVAILABLE_THEMES.length;
            updateView();
            return;
        }
        
        // Novo: Controlo para ligar/desligar a peça fantasma
        if (keycode == KeyEvent.VK_G) {
            board.toggleGhostPiece();
            updateView();
            return;
        }

        if ((!board.isStarted() || board.isGameOver()) && keycode == KeyEvent.VK_ENTER) {
            // exige nome antes de iniciar com ENTER
            if (playerName == null || playerName.trim().isEmpty()) {
                if (gameFrame.getOverlayPanel() != null) {
                    gameFrame.getOverlayPanel().requestFocusForName();
                }
                return;
            }

            board.start();
            if (!timer.isRunning()) {
                timer.start();
            }
            updateView();
            return;
        }
        
        if (!board.isStarted() || board.isGameOver()) {
            return;
        }

        if (keycode == KeyEvent.VK_P) {
            board.togglePause();
            updateView();
            return;
        }

        if (board.isPaused()) {
            return;
        }

        switch (keycode) {
            case KeyEvent.VK_LEFT:
                board.moveLeft();
                break;
            case KeyEvent.VK_RIGHT:
                board.moveRight();
                break;
            case KeyEvent.VK_DOWN:
                board.movePieceDown();
                break;
            case KeyEvent.VK_UP:
                board.rotateRight();
                break;
            case KeyEvent.VK_Z:
                board.rotateLeft();
                break;
            case KeyEvent.VK_SPACE:
                board.dropDown();
                break;
        }
        
        updateView();
    }

    private int getDelayForLevel() {
        return Math.max(100, INITIAL_DELAY - (board.getLevel() - 1) * 30);
    }
}