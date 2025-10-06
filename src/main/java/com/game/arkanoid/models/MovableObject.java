package com.game.arkanoid.models;

/**
 * Abstract class for movable objects, extending from GameObject.
 * Classes that extended this class must provide/override certain methods.
 * 
 * @author bmngxn
 */
public abstract class MovableObject extends GameObject {
    protected double dx, dy; 

    protected MovableObject(double x, double y, double w, double h) {
        super(x, y, w, h);
    }

    protected void move(double dt) { 
        this.x += dx * dt; 
        this.y += dy * dt; 
    }

    @Override
    public void update(double dt) { 
        move(dt); 
    }

    public double getDx() { 
        return dx;
    }
    public double getDy() { 
        return dy; 
    }
    public void setVelocity(double dx, double dy) { 
        this.dx = dx; 
        this.dy = dy; 
    }
}