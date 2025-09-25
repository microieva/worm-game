#!/bin/bash

set -e # Exit immediately if any command fails

echo "🚀 Starting Automated Deployment for Java WormGame"
echo "Target: $(lsb_release -ds || cat /etc/*release || uname -om) 2>/dev/null"

echo ""
echo "🔧 Phase 1: Updating System and Installing Prerequisites..."
sudo apt-get update -y
sudo apt-get install -y \
    curl \
    wget \
    git \
    software-properties-common \
    apt-transport-https \
    ca-certificates \
    gnupg \
    lsb-release

echo ""
echo "☕ Phase 2: Ensuring Java 11 is installed..."

if ! command -v java &> /dev/null || ! java -version 2>&1 | grep -q "11"; then
    echo "Java 11 not found. Installing..."
    
    sudo apt-get install -y openjdk-11-jdk
    
    # Set JAVA_HOME
    export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
    echo "export JAVA_HOME=$JAVA_HOME" >> ~/.bashrc
    
    echo "✅ Java 11 installed."
    java -version
else
    echo "✅ Java 11 is already installed."
    java -version
fi

echo ""
echo "📦 Phase 3: Ensuring Maven is installed..."

if ! command -v mvn &> /dev/null; then
    echo "Maven not found. Installing..."
    sudo apt-get install -y maven
    echo "✅ Maven installed."
    mvn --version
else
    echo "✅ Maven is already installed."
    mvn --version
fi

echo ""
echo "🐳 Phase 4: Ensuring Docker and Docker Compose are installed..."

if ! command -v docker &> /dev/null; then
    echo "Docker not found. Installing..."
    
    sudo install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    sudo chmod a+r /etc/apt/keyrings/docker.gpg

    echo \
      "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
      $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    
    sudo apt-get update -y
    sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin
    
    sudo usermod -aG docker $USER
    echo "✅ Docker installed. Note: You may need to logout and back in for group changes to apply."
else
    echo "✅ Docker is already installed."
fi

if ! docker compose version &> /dev/null; then
    echo "Docker Compose plugin not found. Installing..."
    sudo apt-get update -y
    sudo apt-get install -y docker-compose-plugin
    echo "✅ Docker Compose plugin installed."
else
    echo "✅ Docker Compose plugin is already installed."
fi

echo ""
echo "🔄 Phase 5: Setting up GUI dependencies for headless server..."

echo "Installing Xvfb and virtual display dependencies..."
sudo apt-get install -y \
    xvfb \
    x11vnc \
    fluxbox \
    xorg

echo "✅ GUI dependencies installed."

echo ""
echo "🔄 Refreshing group membership..."

if ! docker ps &> /dev/null; then
    echo "Attempting to refresh Docker group permissions..."
    sudo su - $USER -c "docker ps" || true
    # Use sudo for the rest of the script if still needed
    DOCKER_CMD="sudo docker"
else
    DOCKER_CMD="docker"
fi

echo ""
echo "🏗️ Phase 6: Building the Java Application..."

echo "Building the project with Maven..."
./mvnw clean package -DskipTests

if [ -f "target/wormgame-1.0-SNAPSHOT-jar-with-dependencies.jar" ]; then
    echo "✅ Java application built successfully."
    echo "JAR size: $(ls -lh target/wormgame-1.0-SNAPSHOT-jar-with-dependencies.jar | awk '{print $5}')"
else
    echo "❌ Failed to build Java application"
    exit 1
fi

echo ""
echo "🐳 Phase 7: Building and Launching Docker Container..."

echo "Stopping any existing containers..."
$DOCKER_CMD compose down || true
$DOCKER_CMD rm -f worm-game || true

echo "Building Docker image..."
$DOCKER_CMD build -t worm-game .

echo "Starting container..."
$DOCKER_CMD run -d \
    --name worm-game \
    -p 8080:8080 \
    -p 5900:5900 \
    --restart unless-stopped \
    worm-game

echo "Waiting for container to start up..."
sleep 15

echo ""
echo "🔍 Phase 8: Verifying Deployment..."

echo "Container status:"
$DOCKER_CMD ps

if $DOCKER_CMD ps | grep -q "worm-game"; then
    echo "✅ WormGame container is running"
else
    echo "❌ WormGame container failed to start"
    echo "Checking logs..."
    $DOCKER_CMD logs worm-game
    exit 1
fi

echo "Testing application health..."
if curl -f http://localhost:8080/ > /dev/null 2>&1; then
    echo "✅ Application is responding on port 8080"
    
else
    echo "⚠️ Application might still be starting up..."
    echo "Current logs:"
    $DOCKER_CMD logs worm-game --tail=20
    echo "Waiting additional time..."
    sleep 10
    
    # Retry check
    if curl -f http://localhost:8080/ > /dev/null 2>&1; then
        echo "✅ Application is now responding"
    else
        echo "❌ Application failed to start properly"
        $DOCKER_CMD logs worm-game
        exit 1
    fi
fi

echo ""
echo "🎉 Deployment Complete!"
echo ""
echo "📊 Services Status:"
$DOCKER_CMD ps
echo ""
echo "🌐 Application URLs:"
EXTERNAL_IP=$(curl -s ifconfig.me || hostname -I | awk '{print $1}' || echo "localhost")
echo "   - Web Interface: http://$EXTERNAL_IP:8080"
echo "   - VNC Server (optional): $EXTERNAL_IP:5900"
echo ""
echo "🎮 How to Play:"
echo "   1. Open http://$EXTERNAL_IP:8080 in your browser"
echo "   2. Click 'START GAME' to begin"
echo "   3. Use arrow keys or on-screen controls to play"
echo "   4. Eat apples to grow, avoid walls and self-collision"
echo ""
echo "📋 Useful Commands:"
echo "   View application logs:    $DOCKER_CMD logs worm-game -f"
echo "   Stop application:         $DOCKER_CMD stop worm-game"
echo "   Restart application:      $DOCKER_CMD restart worm-game"
echo "   View container stats:     $DOCKER_CMD stats worm-game"
echo "   Shell into container:     $DOCKER_CMD exec -it worm-game bash"
echo ""
echo "⏰ Game is ready to play! The web interface should be accessible immediately."
echo "   If you encounter issues, check logs with: $DOCKER_CMD logs worm-game"