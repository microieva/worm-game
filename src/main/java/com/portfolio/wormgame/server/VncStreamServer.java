package com.portfolio.wormgame.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import com.portfolio.wormgame.gui.UserInterface;
import com.portfolio.wormgame.game.WormGame;
import com.portfolio.wormgame.Direction;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import javax.swing.Timer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class VncStreamServer {
    private Server webServer;
    private UserInterface ui;
    private int webPort;

    public VncStreamServer(UserInterface ui, int webPort) {
        this.ui = ui;
        this.webPort = webPort;
    }

    public void start() throws Exception {
        startWebServer();
        
        System.out.println("✅ Game Streaming Server Started");
        System.out.println("🌐 Open: http://localhost:" + webPort + "/vnc.html");
        System.out.println("📺 Game streaming available on port: " + webPort);
    }

    private void startWebServer() throws Exception {
        webServer = new Server(webPort);
        
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        
        // Add screen capture servlet
        context.addServlet(new ServletHolder(new ScreenCaptureServlet(ui)), "/screen");
        
        // Add API endpoints
        context.addServlet(new ServletHolder(new GameInfoServlet()), "/api/game-info");
        
        // Add HTML servlet to serve vnc.html
        context.addServlet(new ServletHolder(new HtmlServlet()), "/vnc.html");
        context.addServlet(new ServletHolder(new StaticResourceServlet()), "/static/*");
        
        // Add root redirect to vnc.html
        context.addServlet(new ServletHolder(new RootRedirectServlet()), "/");

        context.addServlet(new ServletHolder(new GameControlServlet(ui)), "/api/control");

        
        webServer.setHandler(context);
        webServer.start();
    }

    public void stop() throws Exception {
        if (webServer != null) {
            webServer.stop();
        }
    }

    // Servlet to serve vnc.html from resources
    public static class HtmlServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
                throws ServletException, IOException {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("vnc.html")) {
                if (is != null) {
                    String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    resp.setContentType("text/html; charset=utf-8");
                    resp.getWriter().write(html);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("vnc.html not found in resources");
                }
            }
        }
    }

    public static class StaticResourceServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
                throws ServletException, IOException {
            String path = req.getPathInfo();
            if (path == null) path = "";
            
            String contentType = "text/plain";
            if (path.endsWith(".css")) contentType = "text/css";
            if (path.endsWith(".js")) contentType = "application/javascript";
            if (path.endsWith(".png")) contentType = "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) contentType = "image/jpeg";
            
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("static" + path)) {
                if (is != null) {
                    resp.setContentType(contentType);
                    resp.getOutputStream().write(is.readAllBytes());
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("Resource not found: " + path);
                }
            }
        }
    }

    // Servlet to redirect root to vnc.html
    public static class RootRedirectServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
                throws ServletException, IOException {
            resp.sendRedirect("/vnc.html");
        }
    }

    // Servlet to provide game connection info
    public static class GameInfoServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
                throws ServletException, IOException {
            resp.setContentType("application/json");
            resp.getWriter().println("{" +
                "\"status\": \"running\"," +
                "\"type\": \"screen_stream\"," +
                "\"screen_endpoint\": \"/screen\"," +
                "\"message\": \"Game streaming via screen capture\"" +
            "}");
        }
    }

    // Servlet for screen capture
    public static class ScreenCaptureServlet extends HttpServlet {
        private final UserInterface ui;
        
        public ScreenCaptureServlet(UserInterface ui) {
            this.ui = ui;
        }
        
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
                throws ServletException, IOException {
            try {
                if (ui != null && ui.getDrawingBoard() != null) {
                    BufferedImage screenshot = ui.getDrawingBoard().captureScreenshot();
                    if (screenshot != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(screenshot, "png", baos);
                        
                        resp.setContentType("image/png");
                        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                        resp.setHeader("Pragma", "no-cache");
                        resp.setHeader("Expires", "0");
                        
                        resp.getOutputStream().write(baos.toByteArray());
                        return;
                    }
                }
                resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                resp.getWriter().write("Screen not available");
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("Error capturing screen: " + e.getMessage());
            }
        }
    }

    // Servlet for game control
    public static class GameControlServlet extends HttpServlet {
      private final UserInterface ui;
      
      public GameControlServlet(UserInterface ui) {
          this.ui = ui;
      }
      
      @Override
      protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
              throws ServletException, IOException {
          try {
              String action = req.getParameter("action");
              resp.setContentType("application/json");
              
              if (ui == null || ui.getWormGame() == null) {
                  resp.getWriter().println("{\"status\":\"error\", \"message\":\"Game not available\"}");
                  return;
              }
              
              switch (action) {
                  case "start":
                      ui.getWormGame().start();
                      resp.getWriter().println("{\"status\":\"success\", \"message\":\"Game started\"}");
                      System.out.println("🎮 Game started from browser");
                      break;
                      
                  case "pause":
                      if (ui.getWormGame() instanceof Timer) {
                          ((Timer) ui.getWormGame()).stop();
                      }
                      resp.getWriter().println("{\"status\":\"success\", \"message\":\"Game paused\"}");
                      System.out.println("🎮 Game paused from browser");
                      break;
                      
                  case "restart":
                      ui.stopAndCreateNewGame();
                      resp.getWriter().println("{\"status\":\"success\", \"message\":\"Game stopped and reset\"}");
                      System.out.println("🛑 Game stopped and reset from browser");
                      break;

                  case "up":
                    if (ui.getWormGame() != null) {
                        ui.getWormGame().getWorm().setDirection(Direction.UP);
                        resp.getWriter().println("{\"status\":\"success\", \"message\":\"Going up\"}");
                        System.out.println("⬆️ Going up");
                    }
                    break;
                    
                  case "down":
                      if (ui.getWormGame() != null) {
                          ui.getWormGame().getWorm().setDirection(Direction.DOWN);
                          resp.getWriter().println("{\"status\":\"success\", \"message\":\"Going down\"}");
                          System.out.println("⬇️ Going down");
                      }
                      break;
                      
                  case "left":
                      if (ui.getWormGame() != null) {
                          ui.getWormGame().getWorm().setDirection(Direction.LEFT);
                          resp.getWriter().println("{\"status\":\"success\", \"message\":\"Going left\"}");
                          System.out.println("⬅️ Going left");
                      }
                      break;
                      
                  case "right":
                    if (ui.getWormGame() != null) {
                        ui.getWormGame().getWorm().setDirection(Direction.RIGHT);
                        resp.getWriter().println("{\"status\":\"success\", \"message\":\"Going right\"}");
                        System.out.println("➡️ Going right");
                    }
                    break;
                      
                  default:
                      resp.getWriter().println("{\"status\":\"error\", \"message\":\"Unknown action: \" + action}");
              }
              
          } catch (Exception e) {
              resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
              resp.getWriter().println("{\"status\":\"error\", \"message\":\"Server error: \" + e.getMessage()}");
              System.err.println("❌ Error in GameControlServlet: " + e.getMessage());
              e.printStackTrace();
          }
      }
    }
}
