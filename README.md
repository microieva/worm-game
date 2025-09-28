# Worm Game 🐍

A Java-based worm/snake game with real-time VNC streaming and web interface, deployed via Docker with HTTPS support.

## Features

- **Classic Snake Gameplay** - Control a worm to eat apples and grow longer
- **Real-time VNC Streaming** - Stream game visuals to web browsers
- **Web Interface** - Play directly from any modern browser
- **RESTful API** - Game controls and score tracking via HTTP endpoints
- **Docker Containerized** - Easy deployment and scaling
- **HTTPS Secure Access** - SSL/TLS encrypted connections
- **Cross-platform** - Runs on any system with Java and Docker

## How to Play

1. **Access the Game**: Visit `https://wormgame.mooo.com/`
2. **Start Playing**: Click start and use arrow keys or on-screen controls to move the worm
3. **Objective**: Eat apples to grow longer without hitting walls or yourself
4. **Controls**:
   - **Arrow Keys** or **WASD** - Change direction
   - **Pause** - Temporarily stop the game
   - **Stop** - Restart a new game


## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌────────────────┐
│   Web Browser   │ ←→ │   Nginx Proxy    │ ←→ │  Java App      │
│                 │    │  (HTTPS Term.)   │    │  (Jetty Server)│
└─────────────────┘    └──────────────────┘    └────────────────┘
                              ↑                         ↑
                       SSL Certificate           Game Logic & 
                       (Let's Encrypt)           VNC Streaming
```

## Prerequisites
- Java 11+
- Maven 3.6+
- Docker & Docker Compose

## Dependencies

- **Java Swing**: GUI framework
- **Jetty**: Embedded web server
- **JUnit**: Testing framework
- **Maven**: Build automation

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | Web interface (VNC streaming) |
| `/screen` | GET | Real-time screen capture |
| `/api/control` | POST | Game controls (start, pause, direction) |
| `/api/score` | GET | Current game score |
| `/api/game-info` | GET | Game status information |

### Control API Example
```bash
curl -X POST http://localhost:8080/api/control \
  -d "action=up" \
  -H "Content-Type: application/x-www-form-urlencoded"
```

Available actions: `start`, `pause`, `restart`, `up`, `down`, `left`, `right`

## Production Deployment

### 1. Server Setup
Currently this server runs on a [DataCrunch](www.datacrunch.io/) cloud instance with the following production-grade specifications:

**Compute Resources:**
- **GPU**: 1x NVIDIA RTX A6000 with 48GB VRAM (CUDA 12.6)
- **CPU**: 10 vCPUs with 60GB system RAM
- **Storage**: 50GB high-performance storage

**Software Stack:**
- **OS**: Ubuntu 24.04 LTS with Docker containerization
- **Environment**: Fully containerized deployment using Docker Compose
- **Runtime**: Optimized for GPU-accelerated inference with CUDA support

### 2. Domain & SSL Setup
1. Registered subdomain points to cloud server IP
2. Configured Nginx reverse proxy
3. Encryption certificates by Let's Encrypt SSL certificates
4. Enabled HTTP to HTTPS redirects

### 3. Security Features

- HTTPS encryption with Let's Encrypt
- Secure headers via Nginx
- Container isolation
- Input validation on API endpoints
- No direct VNC port exposure

### 4. Nginx Configuration
The production setup includes:
- HTTPS termination
- WebSocket proxy for VNC streaming
- Static file caching
- Security headers

## Development

### Project Structure

```
worm-game/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/portfolio/wormgame/
│       │       ├── Direction.java 
│       │       ├── Main.java
│       │       ├── game/          # Game logic
│       │       ├── gui/           # Swing GUI components
│       │       ├── domain/        # Game entities
│       │       └── server/        # Web server and streaming
│       └── resources/
│           ├── application.properties
│           ├── vnc.html
│           ├── icons/ 
│           └── static/            # Web assets (CSS, JS)
├── target/                        # Build outputs
├── deploy.sh                      # Deployment script (Dependancy installations)
├── start.sh                       # Script for application start & health checks
├── Dockerfile                     # Container configuration
├── docker-compose.yml             # Container environment and ports
└── pom.xml                        # Maven configuration
```


### Key Components

- **WormGame**: Main game controller extending Timer
- **UserInterface**: Swing-based GUI wrapper
- **DrawingBoard**: Custom JPanel for rendering game state
- **VncStreamServer**: Web server for browser streaming including servlets:
  - **GameControlServlet**: HTTP endpoints for game control
  - **ScreenCaptureServlet**: handles screen capture for streaming
  - **GameInfoServlet**: providing game connection information
  - **GameScoreServlet**: handles response withe game score (currently worm length)

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/microieva/worm-game.git
   cd worm-game
   ```

2. **Build the project**
   ```bash
   ./mvnw clean package
   ```

3. **Run the application**
   ```bash
   java -jar target/wormgame-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

4. **Access the game**
   - **GUI**: The Java Swing interface will open automatically
   - **Web Interface**: Open http://localhost:8080 in your browser

### Using Maven Wrapper

If you don't have Maven installed, use the included wrapper:

```bash
# Build
./mvnw clean package

# Run tests
./mvnw test

# Run specific goals
./mvnw compile
```
---

**Live Demo**: [https://wormgame.mooo.com/](https://wormgame.mooo.com/)

*Built with Java, Jetty, Docker, and Nginx*

## Acknowledgments
- [DataCrunch](www.datacrunch.io/) team for the access to GPU instance & infrastructure!