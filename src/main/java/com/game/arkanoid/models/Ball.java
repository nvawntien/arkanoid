package com.game.arkanoid.models;

import com.game.arkanoid.utils.Constants;

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

    /** Ball vs AABB (Paddle/Brick) – test va chạm hình tròn với hộp. */

    public boolean checkCollision(GameObject other) {
        double oL = other.getX();
        double oT = other.getY();
        double oR = oL + other.getWidth();
        double oB = oT + other.getHeight();

        double cx = getCenterX(), cy = getCenterY();
        double nearestX = Math.max(oL, Math.min(cx, oR));
        double nearestY = Math.max(oT, Math.min(cy, oB));
        double ddx = cx - nearestX, ddy = cy - nearestY;
        return ddx*ddx + ddy*ddy <= radius * radius;
    }

    /** Nảy khỏi AABB theo trục có độ xuyên nhỏ nhất (đơn giản, ổn định). */

    public void bounceOff(GameObject other) {
        double oL = other.getX();
        double oT = other.getY();
        double oR = oL + other.getWidth();
        double oB = oT + other.getHeight();

        double penLeft   = Math.abs(right() - oL);
        double penRight  = Math.abs(oR - left());
        double penTop    = Math.abs(bottom() - oT);
        double penBottom = Math.abs(oB - top());

        double minPen = Math.min(Math.min(penLeft, penRight), Math.min(penTop, penBottom));

        if (minPen == penLeft) {
            setCenter(oL - radius - Constants.BALL_NUDGE, y);
            setVelocity(-Math.abs(dx) * Constants.BALL_RESTITUTION, dy);
        } else if (minPen == penRight) {
            setCenter(oR + radius + Constants.BALL_NUDGE, y);
            setVelocity(Math.abs(dx) * Constants.BALL_RESTITUTION, dy);
        } else if (minPen == penTop) {
            setCenter(x, oT - radius - Constants.BALL_NUDGE);
            setVelocity(dx, -Math.abs(dy) * Constants.BALL_RESTITUTION);
        } else {
            setCenter(x, oB + radius + Constants.BALL_NUDGE);
            setVelocity(dx, Math.abs(dy) * Constants.BALL_RESTITUTION);
        }

        double speed = Math.hypot(dx, dy);
        if (speed < 1e-3) { // tránh “đứng chết”
            double t = Math.toRadians(Constants.BALL_LAUNCH_ANGLE);
            setVelocity(Constants.BALL_SPEED * Math.cos(t), -Constants.BALL_SPEED * Math.sin(t));
        }
    }
}