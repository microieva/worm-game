/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wormgame.domain;

import wormgame.Direction;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author ieva
 */
public class Worm {
    
    private List<Piece> worm = new ArrayList<Piece>();
    private Direction originalDirection;
    private int originalX;
    private int originalY;
    private boolean grow;
    
    /*private Piece head;
    private Piece tail;
    private Piece body;*/
    
    public Worm (int originalX, int originalY, Direction originalDirection) {
        this.originalX = originalX;
        this.originalY = originalY;
        this.originalDirection = originalDirection;
        this.worm.add(0, new Piece(this.originalX, this.originalY));
        this.grow = false;
        
        /*this.tail = this.worm.get(0);
        this.head = this.worm.get(this.worm.size()-1);
        for (int i=0; i<this.worm.size()-1; i++) {
            if (!this.worm.get(i).equals(this.head) && !this.worm.get(i).equals(this.tail)) {
                this.body = this.worm.get(i);
            }
        }*/
    }
    
    public Direction getDirection() {
        return this.originalDirection;
    }
    
    public void setDirection(Direction dir) {
        this.originalDirection = dir;
    }
    
    public void move() { // GitHub
        if (this.originalDirection == Direction.UP) {
            worm.add(new Piece(this.originalX, this.originalY-1));
            this.originalY = this.originalY -1;

        } else if (this.originalDirection == Direction.DOWN) {
            worm.add(new Piece(this.originalX, this.originalY+1));
            this.originalY = this.originalY +1;

        } else if (this.originalDirection == Direction.LEFT) {
            worm.add(new Piece(this.originalX-1, this.originalY));
            this.originalX = this.originalX -1;

        } else if (this.originalDirection == Direction.RIGHT) {
            worm.add(new Piece(this.originalX+1, this.originalY));
            this.originalX = this.originalX +1;
        }
        if (this.getLength() > 3 && this.grow == false) {
            worm.remove(0);
        }
        if (this.grow == true) {
            this.grow = false;
        }
    }
    
    public void goBackwards() {
        if (this.originalDirection == Direction.DOWN) {
            this.originalDirection = Direction.UP;
            move();
        } else if (this.originalDirection == Direction.UP) {
            this.originalDirection = Direction.DOWN;
            move();
        } else if (this.originalDirection == Direction.RIGHT) {
            this.originalDirection = Direction.LEFT;
            move();
        } else if (this.originalDirection == Direction.LEFT) {
            this.originalDirection = Direction.RIGHT;
            move();
        }
    }
    
    public int getLength() {
        return this.worm.size();
    }
    
    public List<Piece> getPieces() {
        return this.worm;
    }
    
    public void grow() {
        this.grow = true;
    }
    
    public boolean runsInto(Piece piece) {
        for (Piece p: this.worm) {
            if (p.getX() == piece.getX() && p.getY() == piece.getY()) {
                return true;
            }
        }
        return false;
    }
    
    public boolean runsIntoItself() {
        for (int i=0; i<getLength()-1; i++) {
            if (this.worm.get(this.worm.size()-1).getX() == this.worm.get(i).getX() && 
                    this.worm.get(this.worm.size()-1).getY() == this.worm.get(i).getY()) {
                return true;
            }
        }
        return false;
    }
    
    public void shrink() {
        this.grow = false;
        this.worm.remove(worm.get(0));    
    }
}
