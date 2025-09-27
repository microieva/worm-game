package com.portfolio.wormgame.gui;

import java.awt.Container;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.Timer;
import java.awt.event.KeyListener; 
import javax.swing.WindowConstants;
import com.portfolio.wormgame.game.WormGame;

public class UserInterface implements Runnable {

    private JFrame frame;
    private WormGame game;
    private int sideLength;
    private DrawingBoard db;

    public UserInterface(WormGame game, int sideLength) {
        this.game = game;
        this.sideLength = sideLength;
    }

    @Override
    public void run() {
        frame = new JFrame("Worm Game");
        int width = (game.getWidth() + 1) * sideLength + 10;
        int height = (game.getHeight() + 2) * sideLength + 10;

        frame.setPreferredSize(new Dimension(width, height));

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        createComponents(frame.getContentPane());

        frame.pack();
        frame.setVisible(true);
    }

    public void createComponents(Container container) {
        
        this.db = new DrawingBoard(game, sideLength);
        container.add(db);
        
        KeyboardListener kl = new KeyboardListener(game.getWorm());
        frame.addKeyListener(kl);
    }
    
    public Updatable getUpdatable() {
        return this.db;
    }

    public JFrame getFrame() {
        return frame;
    }

    public DrawingBoard getDrawingBoard() {
        return this.db;
    }

    public WormGame getWormGame() {
        return this.game;
    }

    public void stopGame() {
        if (this.game != null) {
            if (this.game instanceof Timer) {
                ((Timer) this.game).stop();
            }
            
            WormGame newGame = new WormGame(game.getWidth(), game.getHeight());
            
            if (this.db != null) {
                this.db.setGame(newGame); 
            }
            
            if (frame != null) {
                for (KeyListener listener : frame.getKeyListeners()) {
                    frame.removeKeyListener(listener);
                }
                KeyboardListener newKl = new KeyboardListener(newGame.getWorm());
                frame.addKeyListener(newKl);
            }
            
            newGame.setUpdatable(this.db);
            this.game = newGame;
            this.db.setIsRunning(false);
        }
    }

    public void stopAndCreateNewGame() {
        stopGame();
    }
}
