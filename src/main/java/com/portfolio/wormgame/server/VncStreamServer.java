package com.portfolio.wormgame.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.DefaultServlet;
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
import java.net.URL; 

public class VncStreamServer {
    private Server webServer;
    private UserInterface ui;
    private int webPort;
    private WormGame game;

    public VncStreamServer(UserInterface ui, int webPort, WormGame game) {
        this.ui = ui;
        this.webPort = webPort;
        this.game = game;
    }

    public void start() throws Exception {
        startWebServer();    
        System.out.println("✅ Game Streaming Server Started");
    }

    private void startWebServer() throws Exception {
        webServer = new Server(webPort);
        
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL staticResource = classLoader.getResource("static");
        
        if (staticResource != null) {
            context.setResourceBase(staticResource.toExternalForm());
        } else {
            context.setResourceBase("./");
        }
        
        context.setWelcomeFiles(new String[]{"vnc.html"});
        
        // DefaultServlet 
        ServletHolder defaultHolder = new ServletHolder("default", DefaultServlet.class);
        defaultHolder.setInitParameter("dirAllowed", "false");
        defaultHolder.setInitParameter("gzip", "true");
        defaultHolder.setInitParameter("etags", "true");
        defaultHolder.setInitParameter("resourceBase", staticResource != null ? staticResource.toExternalForm() : "./");
        context.addServlet(defaultHolder, "/");
        
        // API servlets
        context.addServlet(new ServletHolder(new ScreenCaptureServlet(ui)), "/screen");
        context.addServlet(new ServletHolder(new GameInfoServlet()), "/api/game-info");
        context.addServlet(new ServletHolder(new GameControlServlet(ui)), "/api/control");
        context.addServlet(new ServletHolder(new GameScoreServlet(game)), "/api/score");
        
        webServer.setHandler(context);
        webServer.start();
        System.out.println("✅ Web server started on port " + webPort);
    }

    public void stop() throws Exception {
        if (webServer != null) {
            webServer.stop();
        }
    }

    // Servlet for score (currently worm length)
    public class GameScoreServlet extends HttpServlet {
        private final WormGame game;

        public GameScoreServlet(WormGame game) {
            this.game = game;
        }
        
        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) 
                throws IOException {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            int score = 0;
            
            if (game != null && game.getWorm() != null && game.getWorm().getLength() > 3) {
                score = game.getWorm().getLength() - 3;
            }
            
            String jsonResponse = String.format("{\"score\": %d}", score);
            response.getWriter().write(jsonResponse);
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
                      break;
                      
                  case "pause":
                      if (ui.getWormGame() instanceof Timer) {
                          ((Timer) ui.getWormGame()).stop();
                      }
                      resp.getWriter().println("{\"status\":\"success\", \"message\":\"Game paused\"}");
                      break;
                      
                  case "restart":
                      ui.stopAndCreateNewGame();
                      resp.getWriter().println("{\"status\":\"success\", \"message\":\"Game stopped and reset\"}");
                      break;

                  case "up":
                    if (ui.getWormGame() != null) {
                        ui.getWormGame().getWorm().setDirection(Direction.UP);
                        resp.getWriter().println("{\"status\":\"success\", \"message\":\"Going up\"}");
                    }
                    break;
                    
                  case "down":
                      if (ui.getWormGame() != null) {
                          ui.getWormGame().getWorm().setDirection(Direction.DOWN);
                          resp.getWriter().println("{\"status\":\"success\", \"message\":\"Going down\"}");
                      }
                      break;
                      
                  case "left":
                      if (ui.getWormGame() != null) {
                          ui.getWormGame().getWorm().setDirection(Direction.LEFT);
                          resp.getWriter().println("{\"status\":\"success\", \"message\":\"Going left\"}");
                      }
                      break;
                      
                  case "right":
                    if (ui.getWormGame() != null) {
                        ui.getWormGame().getWorm().setDirection(Direction.RIGHT);
                        resp.getWriter().println("{\"status\":\"success\", \"message\":\"Going right\"}");
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
