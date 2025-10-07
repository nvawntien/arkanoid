package com.game.arkanoid.models;

import com.game.arkanoid.utils.Constants;

/** Paddle là AABB: x,y = top-left; width,height = kích thước. */
public class Paddle extends MovableObject {
    private double speed;            // px/s
    private double minWidth;         // để clamp power-up
    private double maxWidth;

    public Paddle(double x, double y, double width, double height, double speed) {
        super(x, y, width, height);
        this.speed = speed;
        this.minWidth = Constants.MIN_PADDLE_WIDTH;
        this.maxWidth = Constants.MAX_PADDLE_WIDTH;
        this.dx = 0; 
        this.dy = 0;                                // ONLY MOVES HORIZONTALLY --> dy = 0 
    }

    @Override
    public void update(double dt) {
        move(dt); 
    }

    // getters and setters

    public double getSpeed() { 
        return speed; 
    }
    public void setSpeed(double speed) { 
        this.speed = speed; 
    }

    public double getMinWidth() { 
        return minWidth; 
    }
    public double getMaxWidth() { 
        return maxWidth; 
    }
    public void setWidthBounds(double minW, double maxW) { 
        this.minWidth = minW; this.maxWidth = maxW; 
    }

    /** Thay đổi width có clamp theo min/max. */
    public void setWidthClamped(double newWidth) {
        this.width = Math.max(minWidth, Math.min(maxWidth, newWidth));
    }
}
