package com.game.arkanoid.models;

/**
 * Abstract class representing objects in game, 
 * characterized by x, y, width, height xơii.
 *
 * @author bmngxn
 */
public abstract class GameObject {
    protected double x, y;          // top left (ph chuẩn hóa sau dkm)
    protected double width, height; // Ball dùng 2 * radius để có AABB xới

    /**
     * Game Object constructor.
     * @param x object x coordinate
     * @param y object y coordinate
     * @param w object width
     * @param h object height
     */
    protected GameObject(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    /**
     * Abstract method for update object coordinate.
     * @param dt Time delta
     */
    public abstract void update(double dt);

    // getters and setters
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(double w, double h) {
        this.width = w;
        this.height = h;
    }
}