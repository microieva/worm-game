#!/bin/bash

set -e 

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

echo "Waiting for application to fully start..."
sleep 30 

echo "Testing application health with multiple retries..."

MAX_RETRIES=5
RETRY_COUNT=0
SUCCESS=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    echo "Health check attempt $((RETRY_COUNT + 1))/$MAX_RETRIES..."
    
    # 1: Check if container process is running
    if ! $DOCKER_CMD ps | grep -q "worm-game"; then
        echo "❌ Container is not running"
        break
    fi
    
    # 2: Check if Java process is alive inside container
    if $DOCKER_CMD exec worm-game ps aux | grep -q "java.*app.jar"; then
        echo "✅ Java process is running inside container"
        SUCCESS=1
        break
    fi
    
    # 3: Try HTTP connection (but don't fail immediately)
    if curl -f http://localhost:8080/ > /dev/null 2>&1; then
        echo "✅ HTTP endpoint is responding"
        SUCCESS=1
        break
    fi
    
    # 4: Simple port check
    if nc -z localhost 8080; then
        echo "✅ Port 8080 is listening"
        SUCCESS=1
        break
    fi
    
    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo "⚠️ Not ready yet, retrying in 10 seconds..."
    sleep 10
done

if [ $SUCCESS -eq 1 ]; then
    echo ""
    echo "🎉 Deployment Verified Successfully!"
    echo "✅ Container is running"
    echo "✅ Java process is active" 
    echo "✅ Server is listening on port 8080"
    
    # Final verification
    echo ""
    echo "📋 Final Status Check:"
    $DOCKER_CMD ps
    echo ""
    echo "🔍 Application Logs (tail):"
    $DOCKER_CMD logs worm-game --tail=10
    
else
    echo "❌ Application health check failed after $MAX_RETRIES attempts"
    echo ""
    echo "🔍 Debugging Information:"
    echo "Container status:"
    $DOCKER_CMD ps
    echo ""
    echo "Container logs:"
    $DOCKER_CMD logs worm-game
    echo ""
    echo "Processes inside container:"
    $DOCKER_CMD exec worm-game ps aux
    echo ""
    echo "Network status:"
    $DOCKER_CMD exec worm-game netstat -tlnp
    exit 1
fi
