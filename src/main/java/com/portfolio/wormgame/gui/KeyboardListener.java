package com.portfolio.wormgame.gui;

import com.portfolio.wormgame.domain.Worm;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import com.portfolio.wormgame.Direction;

public class KeyboardListener implements KeyListener {
    
    private Worm worm;
    
    public KeyboardListener(Worm worm) {
        this.worm = worm;
    }
    
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                this.worm.setDirection(Direction.LEFT);
                break;
            case KeyEvent.VK_RIGHT:
                this.worm.setDirection(Direction.RIGHT);
                break;
            case KeyEvent.VK_UP:
                this.worm.setDirection(Direction.UP);
                break;
            case KeyEvent.VK_DOWN:
                this.worm.setDirection(Direction.DOWN);
                break;
            default:
                break;
        }    
    }
    
    public void keyReleased(KeyEvent e) {
    }
    
    public void keyTyped(KeyEvent e) {
    }
    
}
