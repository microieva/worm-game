let streamInterval = null;
let lastMoveTime = 0;
const MOVE_COOLDOWN = 100; 
let isGameRunning = false;

function startStream() {
    const img = document.getElementById('gameStream');
    if (!img) return;
    
    img.style.display = 'block';
    updateStatus('Streaming game...');

    if (streamInterval) {
        clearInterval(streamInterval);
        streamInterval = null;
    }
    
    streamInterval = setInterval(() => {
        if (isGameRunning) {
            img.src = '/screen?t=' + new Date().getTime();
        }
    }, 100); 
}

function stopStream() {
    if (streamInterval) {
        clearInterval(streamInterval);
        streamInterval = null;
    }
    const img = document.getElementById('gameStream');
    if (img) {
        img.style.display = 'none';
    }
    updateStatus('Streaming stopped');
}

async function startGame() {
    try {
        updateStatus('Starting game...');
        updateGameStatus('Starting...');
        
        const response = await fetch('/api/control?action=start', {
            method: 'POST'
        });
        
        const result = await response.json();
        
        if (result.status === 'success') {
            isGameRunning = true;
            updateStatus('Game started successfully!');
            updateGameStatus('Running');
            startStream();
        } else {
            updateStatus('Error: ' + result.message);
            updateGameStatus('Error');
            isGameRunning = false;
        }
    } catch (error) {
        updateStatus('Failed to start game: ' + error.message);
        updateGameStatus('Error');
        isGameRunning = false;
    }
}

async function pauseGame() {
    try {
        updateStatus('Pausing game...');
        
        const response = await fetch('/api/control?action=pause', {
            method: 'POST'
        });
        
        const result = await response.json();
        
        if (result.status === 'success') {
            isGameRunning = false;
            updateStatus('Game paused');
            updateGameStatus('Paused');
        } else {
            updateStatus('Error: ' + result.message);
        }
    } catch (error) {
        updateStatus('Failed to pause game: ' + error.message);
    }
}

async function stopGame() {
    try {
        updateStatus('Stopping & restarting game...');
        
        const response = await fetch('/api/control?action=restart', {
            method: 'POST'
        });
        
        const result = await response.json();
        
        if (result.status === 'success') {
            isGameRunning = true;
            updateStatus('Game stopped & restarted');
            updateGameStatus('Restarted');
            startStream();
        } else {
            updateStatus('Error: ' + result.message);
            isGameRunning = false;
        }
    } catch (error) {
        updateStatus('Failed to stop & restart game: ' + error.message);
        isGameRunning = false;
    }
}

async function move(direction) {
    const now = Date.now();
    if (now - lastMoveTime < MOVE_COOLDOWN) {
        return; // Too soon since last move
    }
    lastMoveTime = now;
    
    try {
        updateStatus('Moving ' + direction + '...');
        
        const response = await fetch('/api/control?action=' + direction, {
            method: 'POST'
        });
        
        const result = await response.json();
        
        if (result.status === 'success') {
            updateStatus('Moved ' + direction);
        } else {
            updateStatus('Move error: ' + result.message);
        }
    } catch (error) {
        updateStatus('Move failed: ' + error.message);
    }
}

function updateStatus(message) {
    const statusElement = document.getElementById('status');
    if (statusElement) {
        statusElement.textContent = message;
    }
}

function updateGameStatus(message) {
    const gameStatusElement = document.getElementById('gameStatus');
    if (gameStatusElement) {
        gameStatusElement.textContent = 'Game status: ' + message;
    }
}

document.addEventListener('keydown', (event) => {
    if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(event.key)) {
        event.preventDefault();
        
        const directionMap = {
            'ArrowUp': 'up',
            'ArrowDown': 'down', 
            'ArrowLeft': 'left',
            'ArrowRight': 'right'
        };
        
        move(directionMap[event.key]);
    }
});

window.addEventListener('load', function() {
    updateStatus('Ready to start game');
    updateGameStatus('Not started');
    
    const img = document.getElementById('gameStream');
    if (img) {
        img.style.display = 'block';
        img.src = '/screen?t=' + new Date().getTime(); 
    }
});
