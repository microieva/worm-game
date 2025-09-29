let streamInterval = null;
let scoreInterval = null;
let lastMoveTime = 0;
const MOVE_COOLDOWN = 100; 
let isGameRunning = false;
let isDisabled = false;
const info = document.getElementById('info');

function startStream() {
    const img = document.getElementById('gameStream');
    if (!img) return;
    
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
        displayInfo('Starting game..');
        updateStatus('Starting game..');
        updateGameStatus('Starting..');
        
        const response = await fetch('/api/control?action=start', {
            method: 'POST'
        });
        
        const result = await response.json();
        
        if (result.status === 'success') {
            updateStatus('Game started successfully!');
            updateGameStatus('Running');
            startStream();
            startScoreUpdates();
            isGameRunning = true;
            setTimeout(() => {info.style.display = 'none'}, 2000);
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

function startScoreUpdates() {
    fetchScore();
    scoreInterval = setInterval(fetchScore, 1000);
}

async function fetchScore() {
    try {
        const response = await fetch('/api/score');
        const data = await response.json();
        const score = data.score;
        const status = data.status;
        if (status === 'game_over') {
            gameOver();
        } 
        updateGameScore((score).toString());
    } catch (error) {
        console.error('Failed to fetch score:', error);
    }
}

function gameOver() {
    isGameRunning = false;
    updateButtonStates(true);
    updateStatus('Game Over!');
    updateGameStatus('Game Over');
    displayInfo('On no! Game over..');    
    stopScoreUpdates();
}

function displayInfo(message) {
    if (info) {
        const h3Element = info.querySelector('h3');
        if (h3Element) {
            h3Element.textContent = message;
        } else {
            info.innerHTML = '<h3>' + message + '</h3>';
        }
        info.style.display = 'block';
    }
}

function stopScoreUpdates() {
    if (scoreInterval) {
        clearInterval(scoreInterval);
        scoreInterval = null;
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
            stopScoreUpdates();
            displayInfo('Game paused');
        } else {
            updateStatus('Error: ' + result.message);
        }
    } catch (error) {
        updateStatus('Failed to pause game: ' + error.message);
    }
}

async function stopGame() {
    stopScoreUpdates();
    updateGameScore('0');
    updateButtonStates(false);
    
    try {
        updateStatus('Stopping & restarting game...');    
        const response = await fetch('/api/control?action=restart', {
            method: 'POST'
        });
        
        const result = await response.json();
        
        if (result.status === 'success') {
            isGameRunning = true;
            updateStatus('Game stopped & restarted');
            updateGameStatus('Ready');
            startStream();
            if (info) {
                info.style.display = 'none';
            }
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
    if (!isGameRunning) {
        updateStatus('Game is not running');
        return;
    }
    
    const now = Date.now();
    if (now - lastMoveTime < MOVE_COOLDOWN) {
        return; 
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

function updateGameScore(message) {
    const gameScoreElement = document.getElementById('gameScore');
    if (gameScoreElement) {
        gameScoreElement.textContent = 'Game score: ' + message;
    }
}

function updateButtonStates(isDisabled) {
    const buttons = document.querySelectorAll('.start, .pause');
    
    if (isDisabled === true) {
        buttons.forEach(button => {
            button.disabled = isDisabled;
            button.style.opacity = 0.6;
            button.style.cursor = 'default';
        });
    } else {
        buttons.forEach(button => {
            button.disabled = isDisabled;
            button.style.opacity = 1;
            button.style.cursor = 'pointer';
        });
    }
}

document.addEventListener('keydown', (event) => {
    if (!isGameRunning) return;
    
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
