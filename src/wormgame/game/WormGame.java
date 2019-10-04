package wormgame.game;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.Timer;

import wormgame.Direction;
import wormgame.domain.Worm;
import wormgame.domain.Apple;
import wormgame.domain.Orange;
import wormgame.domain.Mushroom;
import wormgame.gui.Updatable;

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
    
    public WormGame(int width, int height) {
        super(1000, null);
        
        this.appleCounter = 0;
        this.width = width;
        this.height = height;
        this.continues = true;
        this.worm = new Worm(this.width/2, this.height/2, Direction.DOWN);
        this.x = new Random().nextInt(this.width);
        this.y = new Random().nextInt(this.height);
        
        this.apple = new Apple(this.x, this.y);
        this.orange = new Orange(this.x, this.y);
        this.mushroom = new Mushroom(this.x, this.y);

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
        
        if (this.worm.runsInto(this.apple)) {
            appleEffect();
        } else if (this.worm.runsInto(this.orange)) {
            orangeEffect(); 
        } else if (this.worm.runsInto(this.mushroom)) {
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
        this.worm.grow();
        this.apple = new Apple(new Random().nextInt(this.width), new Random().nextInt(this.height));         
    }
    
    public void orangeEffect() {
        if (this.worm.getLength() > 3) {
            this.worm.shrink();     
        }
        this.orange = new Orange(new Random().nextInt(this.width), new Random().nextInt(this.height));
        
    }
    
    public void mushroomEffect() {
        if (this.worm.getLength() > 3) {
            this.worm.goBackwards();    
        }
        this.mushroom = new Mushroom(new Random().nextInt(this.width), new Random().nextInt(this.height));
        
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
