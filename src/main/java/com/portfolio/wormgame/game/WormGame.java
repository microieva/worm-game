package com.portfolio.wormgame.game;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import javax.swing.Timer;

import com.portfolio.wormgame.Direction;
import com.portfolio.wormgame.domain.Worm;
import com.portfolio.wormgame.domain.Apple;
import com.portfolio.wormgame.domain.Orange;
import com.portfolio.wormgame.domain.Mushroom;
import com.portfolio.wormgame.gui.Updatable;
import com.portfolio.wormgame.domain.FruitType; 
import com.portfolio.wormgame.domain.Piece;

public class WormGame extends Timer implements ActionListener {

    private int width;
    private int height;
    private int x;
    private int y;
    private int appleCounter;

    private boolean continues;
    private Updatable updatable;
    private Worm worm;
    private Apple apple;
    private Orange orange;
    private Mushroom mushroom;
    private boolean isMovingBackwards = false;
    private Random random = new Random();
    private boolean hasSpawnedFirstFruit = false;
    private int spawnInterval = 1000;
    private long lastSpawnTime = 0;
    private FruitType fruitType;
    
    public WormGame(int width, int height) {
        super(1000, null);
        
        this.appleCounter = 0;
        this.width = width;
        this.height = height;
        this.continues = true;
        this.worm = new Worm(this.width/2, this.height/2, Direction.DOWN);

        spawnFruit();
        // this.x = new Random().nextInt(this.width);
        // this.y = new Random().nextInt(this.height);
        
        // this.apple = new Apple(this.x, this.y);
        // this.orange = new Orange(this.x, this.y);
        // this.mushroom = new Mushroom(this.x, this.y);

        addActionListener(this);
        setInitialDelay(2000);
    }

    public boolean continues() {
        return continues;
    }

    public void setUpdatable(Updatable updatable) {
        this.updatable = updatable;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (!continues) {
            return;
        }
        this.worm.move();
        
        if (this.apple != null && this.worm.runsInto(this.apple)) {
            appleEffect();
        } else if (this.orange != null && this.worm.runsInto(this.orange)) {
            orangeEffect(); 
        } else if (this.mushroom != null && this.worm.runsInto(this.mushroom)) {
            mushroomEffect();
        } else if (this.worm.runsIntoItself()) {
            this.continues = false;
        // GAME WALLS:
        } else if ((this.worm.getPieces().get(this.worm.getLength()-1).getX() == this.width+1 || 
                this.worm.getPieces().get(this.worm.getLength()-1).getX() == -1)) {
            this.continues = false;
        } else if (this.worm.getPieces().get(this.worm.getLength()-1).getY() == this.height+1 || 
                this.worm.getPieces().get(this.worm.getLength()-1).getY() == -1) {
            this.continues = false;
        }
        
        this.updatable.update();
        setDelay(1000 / this.worm.getLength());
    }
    
    public void appleEffect() {
        this.appleCounter++;
        this.hasSpawnedFirstFruit = true;
        this.worm.grow();
        spawnFruit();        
    }
    
    public void orangeEffect() {
        if (this.worm.getLength() > 3) {
            this.worm.shrink();     
        }
        spawnFruit();     
    }
    public boolean isMovingBackwards() {
        return this.isMovingBackwards;
    }
    
    public void mushroomEffect() {
        if (this.worm.getLength() > 3) {
            this.worm.goBackwards(); 
            this.isMovingBackwards = true;   
        }
        spawnFruit(); 
    }

    private void spawnFruit() {
        this.fruitType = determineFruitType();
        spawnFruitByType();
    }


    private FruitType determineFruitType() {
        if (!this.hasSpawnedFirstFruit) {
            this.hasSpawnedFirstFruit = true;
            return FruitType.APPLE;
        }
        return getRandomFruitType();
    }

    private FruitType getRandomFruitType() {
        List<FruitType> availableFruits = new ArrayList<>();
        
        availableFruits.add(FruitType.APPLE);
        availableFruits.add(FruitType.MUSHROOM);
        if (this.worm.getLength() > 3) {
            availableFruits.add(FruitType.ORANGE);
        }
        
        return availableFruits.get(random.nextInt(availableFruits.size()));
    }

    private void spawnFruitByType() {
        int x, y;
        do {
            x = random.nextInt(this.width);
            y = random.nextInt(this.height);
        } while (isPositionOccupied(x, y));
        
        switch (this.fruitType) {
            case APPLE:
                this.apple = new Apple(x, y);
                this.orange = null;
                this.mushroom = null;
                break;
            case ORANGE:
                this.orange = new Orange(x, y);
                this.apple = null;
                this.mushroom = null;
                break;
            case MUSHROOM:
                this.mushroom = new Mushroom(x, y);
                this.apple = null;
                this.orange = null; 
                break;
        }
    }

    // private boolean isPositionOccupiedByWorm(int x, int y) {
    //     if (this.worm == null) return false;
    //     return this.worm.runsInto(new Piece(x, y));
    // }

    private boolean isPositionOccupied(int x, int y) {
        if (this.worm == null) return false;
        
        if (this.worm.runsInto(new Piece(x, y))) {
            return true;
        }
        
        if (this.apple != null && this.apple.getX() == x && this.apple.getY() == y) {
            return true;
        }
        if (this.orange != null && this.orange.getX() == x && this.orange.getY() == y) {
            return true;
        }
        if (this.mushroom != null && this.mushroom.getX() == x && this.mushroom.getY() == y) {
            return true;
        }
        
        return false;
    }
    
    public Worm getWorm() {
        return this.worm;
    }
    
    public void setWorm(Worm worm) {
        this.worm = worm;
    }
    
    public Apple getApple() {
        return this.apple;
    }
    
    public void setApple(Apple apple) {
        this.apple = apple;
    }
    
    public Orange getOrange() {
        return this.orange;
       
    }
    
    public void setOrange(Orange orange) {
        this.orange = orange;
    }
    
    public void setMushroom(Mushroom mushroom) {
        this.mushroom = mushroom;
    }
    
    public Mushroom getMushroom() {
        return this.mushroom;
    }
}
