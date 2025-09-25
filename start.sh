#!/bin/bash

check_xvfb() {
    if ! ps aux | grep -v grep | grep -q "Xvfb :99"; then
        echo "Starting Xvfb..."
        Xvfb :99 -screen 0 1024x768x24 -ac +extension GLX +render -noreset &
        export DISPLAY=:99
    fi
}

start_fluxbox() {
    if ! ps aux | grep -v grep | grep -q "fluxbox"; then
        echo "Starting Fluxbox..."
        fluxbox &
    fi
}

start_vnc() {
    if ! ps aux | grep -v grep | grep -q "x11vnc"; then
        echo "Starting VNC server..."
        x11vnc -display :99 -forever -shared -nopw -listen 0.0.0.0 &
    fi
}

wait_for_xvfb() {
    echo "Waiting for Xvfb to be ready..."
    while ! xdpyinfo -display :99 >/dev/null 2>&1; do
        sleep 1
    done
    echo "Xvfb is ready"
}

echo "Starting GUI environment..."

check_xvfb
wait_for_xvfb

start_fluxbox

start_vnc

sleep 3

echo "Starting Worm Game..."
java -jar app.jar &

echo "Worm Game application started"