# Worm Game - Java Implementation

A classic Snake/Worm game built with Java Swing, featuring a web interface for browser-based control and streaming.

## 🎮 Features

- **Classic Gameplay**: Control a worm that grows when eating apples
- **Multiple Food Types**: Apples, oranges, and mushrooms with different effects
- **Web Interface**: Control the game via browser with real-time streaming
- **Responsive Design**: Works on desktop and mobile devices
- **Docker Support**: Easy containerized deployment

## 🏗️ Project Structure

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
│           ├── vnc.html
│           ├── icons/ 
│           └── static/            # Web assets (HTML, CSS, JS)
├── target/                        # Build outputs
├── Dockerfile                     # Container configuration
└── pom.xml                        # Maven configuration
```

## 🚀 Quick Start

### Prerequisites

- Java 11 or higher
- Maven 3.6+
- (Optional) Docker

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
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

## 🎯 How to Play

### Basic Controls

- **Arrow Keys**: Control the worm's direction
- **Objective**: Eat apples to grow, avoid walls and self-collision

### Food Types

- **🍎 Apple**: Grow longer (+1 segment)
- **🍊 Orange**: Shrink (-1 segment, min length 3)
- **🍄 Mushroom**: Reverse direction

### Web Interface Features

- Start/Pause/Restart game from browser
- Real-time game streaming
- Mobile-friendly touch controls
- Keyboard support for desktop

## 🌐 Web Interface

The web interface provides multiple ways to interact with the game:

### Access Points

- **Main Control Panel**: http://localhost:8080
- **API Status**: http://localhost:8080/api/status
- **Screen Stream**: http://localhost:8080/screen

### Browser Controls

- **▶️ Start Game**: Initialize and start the game
- **🛑 Stop & New Game**: Reset with a fresh game instance
- **🔄 Restart**: Restart the current game
- **⬆️⬇️⬅️➡️ Arrow Controls**: Directional movement (keyboard)

## 🐳 Docker Support

### Build the Docker Image

```bash
docker build -t worm-game .
```

### Run Locally with Docker

```bash
docker run -p 8080:8080 worm-game
```

### Docker Compose (Alternative)

```bash
docker-compose up
```

## 🔧 Development

### Project Architecture

- **WormGame**: Main game controller extending Timer
- **UserInterface**: Swing-based GUI wrapper
- **DrawingBoard**: Custom JPanel for rendering game state
- **VncStreamServer**: Web server for browser streaming
- **GameControlServlet**: HTTP endpoints for game control

### Key Classes

- `Main.java` - Application entry point
- `WormGame.java` - Core game logic
- `Worm.java` - Worm behavior and movement
- `DrawingBoard.java` - Game rendering component
- `VncStreamServer.java` - Web streaming server

## 🧪 Testing

Run the test suite:

```bash
./mvnw test
```

## 📁 Configuration

### JVM Options

Common JVM options for different environments:

```bash
# Development
java -Xmx512m -jar target/wormgame-1.0-SNAPSHOT-jar-with-dependencies.jar

# Production
java -Djava.awt.headless=true -Xmx256m -jar app.jar
```

## 🔄 Build Options

### Create Executable JAR

```bash
./mvnw clean package
```

### Create JAR with Dependencies

```bash
./mvnw clean compile assembly:single
```

### Skip Tests

```bash
./mvnw clean package -DskipTests
```

## 🐛 Troubleshooting

### Logs and Debugging

Enable debug logging:

```bash
java -Ddebug=true -jar target/wormgame-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## 📚 Dependencies

- **Java Swing**: GUI framework
- **Jetty**: Embedded web server
- **JUnit**: Testing framework
- **Maven**: Build automation



