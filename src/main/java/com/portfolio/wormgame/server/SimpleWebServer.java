package com.portfolio.wormgame.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.portfolio.wormgame.game.WormGame;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class SimpleWebServer {
    private HttpServer server;
    private WormGame game;

    public SimpleWebServer(WormGame game, int port) throws IOException {
        this.game = game;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
    }

    public void start() {
        // Basic status endpoint
        server.createContext("/api/status", exchange -> {
            String response = "{\"status\":\"ready\", \"message\":\"Worm Game Server is running\"}";
            sendResponse(exchange, response, "application/json");
        });

        // Start game endpoint
        server.createContext("/api/start", exchange -> {
            if (game != null) {
                game.start();
                String response = "{\"status\":\"started\", \"message\":\"Game started successfully\"}";
                sendResponse(exchange, response, "application/json");
                System.out.println("🎮 Game started via HTTP request");
            } else {
                sendResponse(exchange, "{\"status\":\"error\", \"message\":\"Game not initialized\"}", "application/json", 500);
            }
        });

        // Serve HTML page
        server.createContext("/", exchange -> {
            String html = getSimpleHtml();
            sendResponse(exchange, html, "text/html; charset=utf-8");
        });

        server.setExecutor(null); 
        server.start();
        System.out.println("✅ Simple web server started on http://localhost:8080");
        System.out.println("👉 Open http://localhost:8080 in your browser");
    }

    private void sendResponse(HttpExchange exchange, String content, String contentType) throws IOException {
        sendResponse(exchange, content, contentType, 200);
    }

    private void sendResponse(HttpExchange exchange, String content, String contentType, int statusCode) throws IOException {
        byte[] responseBytes = content.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private String getSimpleHtml() {
        return "<!DOCTYPE html>" +
               "<html><head><title>Worm Game Control</title>" +
               "<meta charset='UTF-8'>" +
               "<style>" +
               "body { font-family: Arial, sans-serif; margin: 40px; padding: 20px; background: #f5f5f5; }" +
               "h1 { color: #2c3e50; }" +
               ".container { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
               "button { padding: 15px 30px; font-size: 18px; margin: 10px; background: #3498db; color: white; border: none; border-radius: 5px; cursor: pointer; }" +
               "button:hover { background: #2980b9; }" +
               ".status { padding: 20px; background: #ecf0f1; border-radius: 5px; margin: 20px 0; font-weight: bold; }" +
               ".success { color: #27ae60; }" +
               ".error { color: #e74c3c; }" +
               "</style>" +
               "</head><body>" +
               "<div class='container'>" +
               "<h1>🐛 Worm Game Control Panel</h1>" +
               "<div class='status' id='status'>Server is ready. Click Start to begin the game.</div>" +
               "<button onclick='startGame()'>▶️ START GAME</button>" +
               "<script>" +
               "async function startGame() {" +
               "    const statusElement = document.getElementById('status');" +
               "    statusElement.innerHTML = 'Starting game...';" +
               "    statusElement.className = 'status';" +
               "    " +
               "    try {" +
               "        const response = await fetch('/api/start');" +
               "        if (response.ok) {" +
               "            const data = await response.json();" +
               "            statusElement.innerHTML = '✅ ' + data.message;" +
               "            statusElement.className = 'status success';" +
               "        } else {" +
               "            const error = await response.text();" +
               "            statusElement.innerHTML = '❌ Error: ' + error;" +
               "            statusElement.className = 'status error';" +
               "        }" +
               "    } catch (error) {" +
               "        statusElement.innerHTML = '❌ Network error: ' + error.message;" +
               "        statusElement.className = 'status error';" +
               "    }" +
               "}" +
               "</script>" +
               "</div>" +
               "</body></html>";
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Web server stopped");
        }
    }
}