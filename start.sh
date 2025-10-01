#!/bin/bash

set -e

LOCKFILE="/tmp/wormgame.lock"
if [ -f "$LOCKFILE" ]; then
    echo "❌ Application is already running!"
    exit 1
fi
touch "$LOCKFILE"

cleanup() {
    echo "Cleaning up..."
    kill $XVFB_PID 2>/dev/null || true
    kill $FLUXBOX_PID 2>/dev/null || true
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

sleep 2

echo "Starting Worm Game..."
java -jar app.jar --server.address=0.0.0.0 --server.port=8080 &
JAVA_PID=$!

echo "All services started. PID: Xvfb=$XVFB_PID, Fluxbox=$FLUXBOX_PID, Java=$JAVA_PID"

echo "Waiting for Java application to start..."
sleep 20

if ! ps -p $JAVA_PID > /dev/null; then
    echo "❌ Java application failed to start!"
    echo "Checking Java process status..."
    ps aux | grep java
    exit 1
fi

echo "✅ Java application started successfully"
echo "✅ Deployment complete - All services running"

wait $JAVA_PID

