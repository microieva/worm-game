#!/bin/bash

set -e

cleanup() {
    echo "Cleaning up..."
    kill $XVFB_PID 2>/dev/null || true
    kill $FLUXBOX_PID 2>/dev/null || true
    kill $VNC_PID 2>/dev/null || true
    kill $JAVA_PID 2>/dev/null || true
    rm -f /tmp/.X99-lock
    echo "Cleanup completed"
    exit 0
}

trap cleanup SIGTERM SIGINT SIGQUIT

rm -f /tmp/.X99-lock
rm -f /tmp/.X11-unix/X99

echo "Starting GUI environment..."

echo "Starting Xvfb..."
Xvfb :99 -screen 0 1024x768x24 -ac +extension GLX +render -noreset &
XVFB_PID=$!

echo "Waiting for Xvfb to be ready..."
max_attempts=30
attempt=1
while ! xdpyinfo -display :99 >/dev/null 2>&1; do
    if [ $attempt -ge $max_attempts ]; then
        echo "Xvfb failed to start after $max_attempts attempts"
        exit 1
    fi
    sleep 1
    attempt=$((attempt + 1))
done
echo "Xvfb is ready"

echo "Starting Fluxbox..."
fluxbox &
FLUXBOX_PID=$!

echo "Starting VNC server..."
# FIXED: Remove lsof command that's failing, use simpler approach
# Kill any existing process on port 5900 using native methods
if command -v ss &> /dev/null; then
    ss -tlnp | grep :5900 | awk '{print $7}' | cut -d= -f2 | cut -d, -f1 | xargs -r kill -9
elif command -v netstat &> /dev/null; then
    netstat -tlnp | grep :5900 | awk '{print $7}' | cut -d'/' -f1 | xargs -r kill -9
fi

x11vnc -display :99 -forever -shared -nopw -listen 0.0.0.0 -rfbport 5900 &
VNC_PID=$!

sleep 2

echo "Starting Worm Game..."
# CRITICAL: Add debug output and proper binding
echo "Starting Java application with 0.0.0.0 binding..."
java -jar app.jar --server.address=0.0.0.0 --server.port=8080 &
JAVA_PID=$!

echo "All services started. PID: Xvfb=$XVFB_PID, Fluxbox=$FLUXBOX_PID, VNC=$VNC_PID, Java=$JAVA_PID"

# Wait for Java to start and check if it's running
echo "Waiting for Java application to start..."
sleep 10

# Check if Java process is still running
if ! ps -p $JAVA_PID > /dev/null; then
    echo "❌ Java application failed to start!"
    echo "Checking Java process status..."
    ps aux | grep java
    exit 1
fi

echo "✅ Java application started successfully"
echo "✅ Deployment complete - All services running"

# Wait for the Java process (main application)
wait $JAVA_PID



# set -e

# cleanup() {
#     echo "Cleaning up..."
#     kill $XVFB_PID 2>/dev/null || true
#     kill $FLUXBOX_PID 2>/dev/null || true
#     kill $VNC_PID 2>/dev/null || true
#     kill $JAVA_PID 2>/dev/null || true
#     rm -f /tmp/.X99-lock
#     echo "Cleanup completed"
#     exit 0
# }

# trap cleanup SIGTERM SIGINT SIGQUIT

# rm -f /tmp/.X99-lock
# rm -f /tmp/.X11-unix/X99

# echo "Starting GUI environment..."

# echo "Starting Xvfb..."
# Xvfb :99 -screen 0 1024x768x24 -ac +extension GLX +render -noreset &
# XVFB_PID=$!

# echo "Waiting for Xvfb to be ready..."
# max_attempts=30
# attempt=1
# while ! xdpyinfo -display :99 >/dev/null 2>&1; do
#     if [ $attempt -ge $max_attempts ]; then
#         echo "Xvfb failed to start after $max_attempts attempts"
#         exit 1
#     fi
#     sleep 1
#     attempt=$((attempt + 1))
# done
# echo "Xvfb is ready"

# echo "Starting Fluxbox..."
# fluxbox &
# FLUXBOX_PID=$!

# echo "Starting VNC server..."
# lsof -ti:5900 | xargs -r kill -9
# x11vnc -display :99 -forever -shared -nopw -listen 0.0.0.0 -rfbport 5900 &
# VNC_PID=$!

# sleep 2

# echo "Starting Worm Game..."
# java -jar app.jar --server.address=0.0.0.0 --server.port=8080 &
# JAVA_PID=$!

# echo "All services started. PID: Xvfb=$XVFB_PID, Fluxbox=$FLUXBOX_PID, VNC=$VNC_PID, Java=$JAVA_PID"

# wait $JAVA_PID