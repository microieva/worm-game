package com.portfolio.wormgame.gui;

import java.awt.Color;
import com.portfolio.wormgame.game.WormGame;
import com.portfolio.wormgame.domain.Piece;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.Timer;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.net.URL;
import java.util.List;


public class DrawingBoard extends JPanel implements Updatable {
    
    private WormGame game;
    private int pieceLength;
    private Image scaledAppleIcon;
    private Image scaledOrangeIcon;
    private Image scaledMushroomIcon;
    private Image scaledHeadIcon;
    private Image scaledTailIcon;
    private Image scaledBodyIcon; 
    
    private static final Color WORM_COLOR = Color.decode("#1dbf44");
    private boolean isRunning = false;

    
    public DrawingBoard(WormGame game, int pieceLength) {
        super.setBackground(Color.GRAY);
        this.game = game;
        this.pieceLength = pieceLength;
        loadIcons();
        checkIsRunning();
    }
    
    private void loadIcons() {
        scaledAppleIcon = loadFruitImage("apple");
        scaledOrangeIcon = loadFruitImage("orange"); 
        scaledMushroomIcon = loadFruitImage("mushroom");

        scaledHeadIcon = loadWormSegment("snake-head");
        scaledTailIcon = loadWormSegment("snake-tail");    
    }

    private void checkIsRunning() {
        if (this.game instanceof Timer) {
            Timer timer = (Timer) this.game;
            this.isRunning = timer.isRunning();
        }
    }
    
    private Image loadFruitImage(String fruitName) {
        String imageName = fruitName.toLowerCase() + ".jpg";
        String resourcePath = "static/icons/" + imageName;
        return loadImage(resourcePath, fruitName);
    }
    
    private Image loadWormSegment(String segmentName) {
        String imageName = segmentName.toLowerCase() + ".png";
        String resourcePath = "static/icons/" + imageName;
        return loadImage(resourcePath, segmentName);
    }
    
    private Image loadImage(String resourcePath, String imageType) {    
        try {
            URL imageUrl = getClass().getClassLoader().getResource(resourcePath);
            if (imageUrl != null) {
                ImageIcon icon = new ImageIcon(imageUrl);
                return scaleImage(icon.getImage(), pieceLength, pieceLength);
            } else {
                System.out.println("✗ Not found in classpath: " + resourcePath);
            }
            
            // (for development)
            String filePath = "src/main/resources/" + resourcePath;
            java.io.File file = new java.io.File(filePath);
            if (file.exists()) {
                System.out.println("✓ Successfully loaded " + imageType + " from filesystem: " + file.getAbsolutePath());
                ImageIcon icon = new ImageIcon(filePath);
                return scaleImage(icon.getImage(), pieceLength, pieceLength);
            } else {
                System.out.println("✗ Not found in filesystem: " + filePath);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading " + imageType + " icon: " + e.getMessage());
        }
        
        return null;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        drawFruit(g, scaledAppleIcon, game.getApple());
        drawFruit(g, scaledMushroomIcon, game.getMushroom());
        drawFruit(g, scaledOrangeIcon, game.getOrange());
        if (this.isRunning) {
            drawWormWithDirection(g);
        }
    }
    
    private void drawFruit(Graphics g, Image fruitIcon, Piece fruit) {
        if (fruitIcon != null && fruit != null) {
            g.drawImage(fruitIcon, fruit.getX() * pieceLength, 
                       fruit.getY() * pieceLength, this);
        }
    }

    private void drawWormWithDirection(Graphics g) {
        if (game.getWorm() == null || game.getWorm().getPieces().isEmpty()) {
            return;
        }
        
        List<Piece> pieces = game.getWorm().getPieces();
        int size = pieces.size();
        
        for (int i = size - 1; i >= 0; i--) {
            Piece piece = pieces.get(i);
            int x = piece.getX() * pieceLength;
            int y = piece.getY() * pieceLength;
            Image imageToDraw = null;
            double rotation = 0;
            

            if (i == 0  && scaledTailIcon != null) {
                imageToDraw = scaledTailIcon;
                rotation = calculateStartRotation(pieces, i) + Math.PI; 
            } else if (i == size - 1 && scaledHeadIcon != null) {
                imageToDraw = scaledHeadIcon;
                rotation = calculateEndRotation(pieces, i) + Math.PI; 
            } else if (scaledBodyIcon != null) {
                imageToDraw = scaledBodyIcon;
            }
            
            if (imageToDraw != null) {
                if (rotation != 0) {
                    drawRotatedImage((Graphics2D) g, imageToDraw, x, y, rotation);
                } else {
                    g.drawImage(imageToDraw, x, y, this);
                }
            } else {
                g.setColor(WORM_COLOR);
                g.fillRect(x, y, pieceLength, pieceLength);
            }
        }
    }

    private double calculateStartRotation(List<Piece> pieces, int headIndex) {
        if (pieces.size() > 1) {
            Piece head = pieces.get(headIndex);
            Piece next = pieces.get(headIndex + 1);
            
            int dx = head.getX() - next.getX();
            int dy = head.getY() - next.getY();
            
            if (dx == 1) return Math.PI;       // Facing left
            if (dx == -1) return 0;            // Facing right  
            if (dy == 1) return -Math.PI / 2;  // Facing up
            if (dy == -1) return Math.PI / 2;  // Facing down
        }
        return 0; 
    }
    
    private double calculateEndRotation(List<Piece> pieces, int tailIndex) {
        if (tailIndex > 0) {
            Piece tail = pieces.get(tailIndex);
            Piece prev = pieces.get(tailIndex - 1);
            
            int dx = tail.getX() - prev.getX();
            int dy = tail.getY() - prev.getY();
            
            if (dx == 1) return 0;             // Coming from left
            if (dx == -1) return Math.PI;      // Coming from right
            if (dy == 1) return Math.PI / 2;   // Coming from above
            if (dy == -1) return -Math.PI / 2; // Coming from below
        }
        return 0;
    }

    
    private void drawRotatedImage(Graphics2D g2d, Image image, int x, int y, double radians) {
        g2d.translate(x + pieceLength / 2, y + pieceLength / 2);
        g2d.rotate(radians);
        g2d.drawImage(image, -pieceLength / 2, -pieceLength / 2, pieceLength, pieceLength, null);
        g2d.rotate(-radians);
        g2d.translate(-x - pieceLength / 2, -y - pieceLength / 2);
    }
    
    private Image scaleImage(Image image, int width, int height) {
        return image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }
    
    public void update() {
        checkIsRunning(); 
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

    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
}

