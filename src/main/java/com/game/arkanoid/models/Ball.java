package com.game.arkanoid.models;

public class Ball extends MovableObject {
    private double radius;
    private boolean isMoving;
    private boolean isStuck; 
    private double stuckOffsetX;
    
    public Ball(double centerX, double centerY, double radius) {
        super(centerX, centerY, radius * 2, radius * 2);
        if (radius <= 0) throw new IllegalArgumentException("radius must be > 0");
        this.radius = radius;
        this.isMoving = false;
        this.isStuck = false;
        this.stuckOffsetX = 0.0;
    }

    //getters and setters

    public double getCenterX() {
        return x;
    }

    public double getCenterY() {
        return y;
    }

    public void setCenter(double cx, double cy) {
        this.x = cx;
        this.y = cy;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean moving) {
        isMoving = moving;
    }

    public boolean isStuck() {
        return isStuck;
    }

    public void setStuck(boolean isStuck) {
        this.isStuck = isStuck;
    }

    public double getStuckOffsetX() {
        return stuckOffsetX;
    }

    public void setStuckOffsetX(double stuckOffsetX) {
        this.stuckOffsetX = stuckOffsetX;
    }

    /**
     * Updates the ball's position based on its velocity and the time delta.
     */
    @Override
    public void update(double dt) {
        if (isMoving) move(dt);
    }

    public double left() {
        return x - radius;
    }

    public double right() {
        return x + radius;
    }

    public double top() {
        return y - radius;
    }

    public double bottom() {
        return y + radius;
    }
}
