package com.game.arkanoid.models;

public class Ball extends MovableObject {
    private double radius;
    private boolean isMoving;

    /**
     * Ball constructor 1.
     * @param centerX center x axis
     * @param y center y axis
     * @param radius ball radius
     */
    public Ball(double centerX, double centerY, double radius) {
        super(centerX, centerY, radius * 2, radius * 2);            // MovableObject width = height = 2r
        if (radius <= 0) throw new IllegalArgumentException("radius must be > 0");
        this.radius = radius;
        this.isMoving = false;
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

    @Override
    public void update(double dt) {
        if (isMoving) move(dt);
    }


    //Collision handling

    // --- Hình hộp bao (AABB) để kiểm va chạm đơn giản ---

    public double left()   {
        return x - radius;
    }
    public double right()  {
        return x + radius;
    }
    public double top()    {
        return y - radius;
    }
    public double bottom() {
        return y + radius;
    }


}
