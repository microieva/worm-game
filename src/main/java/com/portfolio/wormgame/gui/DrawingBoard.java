package com.portfolio.wormgame.gui;

import java.awt.Color;
import com.portfolio.wormgame.game.WormGame;
import com.portfolio.wormgame.domain.Piece;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

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
        ImageIcon appleIcon = new ImageIcon("src/main/resources/static/icons/apple.jpg");
        Image scaledAppleIcon = scaleImage(appleIcon.getImage(), pieceLength, pieceLength);
        ImageIcon apple = new ImageIcon(scaledAppleIcon);
        apple.paintIcon(this, g, game.getApple().getX()*pieceLength, game.getApple().getY()*pieceLength);
        // ORANGE:
        ImageIcon orangeIcon = new ImageIcon("src/main/resources/static/icons/orange.jpg");
        Image scaledOrangeIcon = scaleImage(orangeIcon.getImage(), pieceLength, pieceLength);
        ImageIcon orange = new ImageIcon(scaledOrangeIcon);
        orange.paintIcon(this, g, game.getOrange().getX()*pieceLength, game.getOrange().getY()*pieceLength);
        // MUSHROOM:
        ImageIcon mushroomIcon = new ImageIcon("src/main/resources/static/icons/mushroom.jpg");
        Image scaledMushroomIcon = scaleImage(mushroomIcon.getImage(), pieceLength, pieceLength);
        ImageIcon mushroom = new ImageIcon(scaledMushroomIcon);
        mushroom.paintIcon(this, g, game.getMushroom().getX()*pieceLength, game.getMushroom().getY()*pieceLength);
         
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

   public BufferedImage captureScreenshot() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return null;
        }
        
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        paintAll(g2d);
        g2d.dispose();
        return image;
    }

    public void setGame(WormGame newGame) {
        this.game = newGame;
    }
}
