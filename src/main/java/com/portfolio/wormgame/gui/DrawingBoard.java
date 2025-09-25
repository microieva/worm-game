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
import java.net.URL;

public class DrawingBoard extends JPanel implements Updatable {
    
    private WormGame game;
    private int pieceLength;
    private Image scaledAppleIcon;
    private Image scaledOrangeIcon;
    private Image scaledMushroomIcon;
    
    public DrawingBoard(WormGame game, int pieceLength) {
        super.setBackground(Color.GRAY);
        this.game = game;
        this.pieceLength = pieceLength;
        
        loadIcons();
    }
    
    private void loadIcons() {
        scaledAppleIcon = loadFruitImage("apple");
        scaledOrangeIcon = loadFruitImage("orange"); 
        scaledMushroomIcon = loadFruitImage("mushroom");
        
        if (scaledAppleIcon == null) {
            System.err.println("Failed to load apple icon");
        }
        if (scaledOrangeIcon == null) {
            System.err.println("Failed to load orange icon");
        }
        if (scaledMushroomIcon == null) {
            System.err.println("Failed to load mushroom icon");
        }
    }
    
    private Image loadFruitImage(String fruitName) {
        String imageName = fruitName.toLowerCase() + ".jpg";
        String resourcePath = "static/icons/" + imageName;
        
        try {
            URL imageUrl = getClass().getClassLoader().getResource(resourcePath);
            if (imageUrl != null) {
                System.out.println("✓ Successfully loaded from classpath: " + imageUrl);
                ImageIcon icon = new ImageIcon(imageUrl);
                return scaleImage(icon.getImage(), pieceLength, pieceLength);
            } else {
                System.out.println("✗ Not found in classpath: " + resourcePath);
            }
            
            // (for development)
            String filePath = "src/main/resources/" + resourcePath;
            java.io.File file = new java.io.File(filePath);
            if (file.exists()) {
                System.out.println("✓ Successfully loaded from filesystem: " + file.getAbsolutePath());
                ImageIcon icon = new ImageIcon(filePath);
                return scaleImage(icon.getImage(), pieceLength, pieceLength);
            } else {
                System.out.println("✗ Not found in filesystem: " + filePath);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading icon " + fruitName + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("⚠️ Creating placeholder for: " + fruitName);
        return createPlaceholderIcon(fruitName);
    }
    
    private Image createPlaceholderIcon(String fruitName) {
        BufferedImage placeholder = new BufferedImage(pieceLength, pieceLength, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = placeholder.createGraphics();
        
        switch (fruitName.toLowerCase()) {
            case "apple":
                g2d.setColor(Color.RED);
                break;
            case "orange":
                g2d.setColor(Color.ORANGE);
                break;
            case "mushroom":
                g2d.setColor(Color.WHITE);
                break;
            default:
                g2d.setColor(Color.MAGENTA);
        }
        
        g2d.fillRect(0, 0, pieceLength, pieceLength);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, pieceLength - 1, pieceLength - 1);
        g2d.dispose();
        
        return placeholder;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (scaledAppleIcon != null && game.getApple() != null) {
            g.drawImage(scaledAppleIcon, game.getApple().getX() * pieceLength, 
                       game.getApple().getY() * pieceLength, this);
        }
        
        if (scaledOrangeIcon != null && game.getOrange() != null) {
            g.drawImage(scaledOrangeIcon, game.getOrange().getX() * pieceLength, 
                       game.getOrange().getY() * pieceLength, this);
        }
        
        if (scaledMushroomIcon != null && game.getMushroom() != null) {
            g.drawImage(scaledMushroomIcon, game.getMushroom().getX() * pieceLength, 
                       game.getMushroom().getY() * pieceLength, this);
        }
         
        g.setColor(Color.BLACK);
        if (game.getWorm() != null) {
            for (Piece piece : game.getWorm().getPieces()) {
                g.fill3DRect(piece.getX() * pieceLength, piece.getY() * pieceLength, 
                           pieceLength, pieceLength, true);    
            }
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


// package com.portfolio.wormgame.gui;

// import java.awt.Color;
// import com.portfolio.wormgame.game.WormGame;
// import com.portfolio.wormgame.domain.Piece;
// import javax.swing.JPanel;
// import javax.swing.ImageIcon;
// import java.awt.Graphics;
// import java.awt.Image;
// import java.awt.image.BufferedImage;
// import java.awt.Graphics2D;

// public class DrawingBoard extends JPanel implements Updatable {
    
//     private WormGame game;
//     private int pieceLength;
    
//     public DrawingBoard(WormGame game, int pieceLength) {
//         super.setBackground(Color.GRAY);
//         this.game = game;
//         this.pieceLength = pieceLength;
//     }
    
//     protected void paintComponent(Graphics g) {
//         super.paintComponent(g);
//         // APPLE:
//         ImageIcon appleIcon = new ImageIcon("src/main/resources/static/icons/apple.jpg");
//         Image scaledAppleIcon = scaleImage(appleIcon.getImage(), pieceLength, pieceLength);
//         ImageIcon apple = new ImageIcon(scaledAppleIcon);
//         apple.paintIcon(this, g, game.getApple().getX()*pieceLength, game.getApple().getY()*pieceLength);
//         // ORANGE:
//         ImageIcon orangeIcon = new ImageIcon("src/main/resources/static/icons/orange.jpg");
//         Image scaledOrangeIcon = scaleImage(orangeIcon.getImage(), pieceLength, pieceLength);
//         ImageIcon orange = new ImageIcon(scaledOrangeIcon);
//         orange.paintIcon(this, g, game.getOrange().getX()*pieceLength, game.getOrange().getY()*pieceLength);
//         // MUSHROOM:
//         ImageIcon mushroomIcon = new ImageIcon("src/main/resources/static/icons/mushroom.jpg");
//         Image scaledMushroomIcon = scaleImage(mushroomIcon.getImage(), pieceLength, pieceLength);
//         ImageIcon mushroom = new ImageIcon(scaledMushroomIcon);
//         mushroom.paintIcon(this, g, game.getMushroom().getX()*pieceLength, game.getMushroom().getY()*pieceLength);
         
//         // WORM :
//         g.setColor(Color.BLACK);
//         for (Piece piece: game.getWorm().getPieces()) {
//             g.fill3DRect(piece.getX()*pieceLength, piece.getY()*pieceLength, pieceLength, pieceLength, true);    
//         }     
//     }
    
//     private Image scaleImage(Image image, int width, int height) {
//         return image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
//     }
    
//     public void update() {
//         repaint();
//     }

//    public BufferedImage captureScreenshot() {
//         if (getWidth() <= 0 || getHeight() <= 0) {
//             return null;
//         }
        
//         BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
//         Graphics2D g2d = image.createGraphics();
//         paintAll(g2d);
//         g2d.dispose();
//         return image;
//     }

//     public void setGame(WormGame newGame) {
//         this.game = newGame;
//     }
// }
