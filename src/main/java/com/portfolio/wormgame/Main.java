package com.portfolio.wormgame;

import javax.swing.SwingUtilities;
import com.portfolio.wormgame.gui.UserInterface;
import com.portfolio.wormgame.game.WormGame;
import com.portfolio.wormgame.server.VncStreamServer;
import com.portfolio.wormgame.server.SimpleWebServer;

public class Main {
    private static VncStreamServer vncServer;
    private static UserInterface ui;

    public static void main(String[] args) {
        System.out.println("🚀 Starting Worm Game with VNC Streaming...");
        
        WormGame game = new WormGame(20, 20);
        
        if (!java.awt.GraphicsEnvironment.isHeadless()) {
            startGUI(game);
        }
        
        startVncServer(game);
        
        keepApplicationAlive();
    }
    
    private static void startGUI(WormGame game) {
        try {
            ui = new UserInterface(game, 20);
            SwingUtilities.invokeLater(ui);
            
            int attempts = 0;
            while (ui.getUpdatable() == null && attempts < 50) {
                try {
                    Thread.sleep(100);
                    attempts++;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            if (ui.getUpdatable() != null) {
                game.setUpdatable(ui.getUpdatable());
                System.out.println("🎮 GUI ready - VNC streaming enabled");
            }
        } catch (Exception e) {
            System.err.println("❌ GUI initialization failed: " + e.getMessage());
        }
    }
    
    private static void startVncServer(WormGame game) {
        try {
            if (ui != null) {
                vncServer = new VncStreamServer(ui, 8080, game);
                vncServer.start();
            } else {
                System.out.println("⚠️  GUI not available - VNC server not started");
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to start VNC server: " + e.getMessage());
            System.out.println("📋 Starting simple web server instead...");
            startFallbackWebServer();
        }
    }
    
    private static void startFallbackWebServer() {
        try {
            SimpleWebServer webServer = new SimpleWebServer(new WormGame(20, 20), 8080);
            webServer.start();
        } catch (Exception e) {
            System.err.println("❌ Fallback web server also failed: " + e.getMessage());
        }
    }
    
    private static void keepApplicationAlive() {
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
