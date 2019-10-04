/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wormgame.gui;

import java.awt.Color;
import wormgame.game.WormGame;
import wormgame.domain.Piece;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Image;

/**
 *
 * @author ieva
 */
public class DrawingBoard extends JPanel implements Updatable {
    
    private WormGame game;
    private int pieceLength;
    
    public DrawingBoard(WormGame game, int pieceLength) {
        super.setBackground(Color.GRAY);
        this.game = game;
        this.pieceLength = pieceLength;
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // APPLE:
        ImageIcon appleIcon = new ImageIcon("src/wormgame/icons/apple.jpg");
        Image scaledAppleIcon = scaleImage(appleIcon.getImage(), pieceLength, pieceLength);
        ImageIcon apple = new ImageIcon(scaledAppleIcon);
        apple.paintIcon(this, g, game.getApple().getX()*pieceLength, game.getApple().getY()*pieceLength);
        // ORANGE:
        ImageIcon orangeIcon = new ImageIcon("src/wormgame/icons/orange.jpg");
        Image scaledOrangeIcon = scaleImage(orangeIcon.getImage(), pieceLength, pieceLength);
        ImageIcon orange = new ImageIcon(scaledOrangeIcon);
        orange.paintIcon(this, g, game.getOrange().getX()*pieceLength, game.getOrange().getY()*pieceLength);
        // MUSHROOM:
        ImageIcon mushroomIcon = new ImageIcon("src/wormgame/icons/mushroom.jpg");
        Image scaledMushroomIcon = scaleImage(mushroomIcon.getImage(), pieceLength, pieceLength);
        ImageIcon mushroom = new ImageIcon(scaledMushroomIcon);
        mushroom.paintIcon(this, g, game.getMushroom().getX()*pieceLength, game.getMushroom().getY()*pieceLength);
        
         /*g.setColor(Color.orange);
        g.fillOval(game.getOrange().getX()*pieceLength, game.getOrange().getY()*pieceLength, pieceLength, pieceLength);
      
        g.setColor(Color.red);
        g.fillOval(game.getApple().getX()*pieceLength, game.getApple().getY()*pieceLength, pieceLength, pieceLength);
        
        g.setColor(Color.pink);
        g.fillOval(game.getMushroom().getX()*pieceLength, game.getMushroom().getY()*pieceLength, pieceLength, pieceLength);
        */
         
        // WORM :
        g.setColor(Color.BLACK);
        for (Piece piece: game.getWorm().getPieces()) {
            g.fill3DRect(piece.getX()*pieceLength, piece.getY()*pieceLength, pieceLength, pieceLength, true);    
        }     
    }
    
    private Image scaleImage(Image image, int width, int height) {
        return image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }
    
    public void update() {
        repaint();
    }
}
