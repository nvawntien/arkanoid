package com.game.arkanoid.models;

/**
 * Vertical projectile fired by the paddle when the laser power-up is active.
 */
public final class Bullet extends MovableObject {

    public Bullet(double x, double y, double width, double height, double speed) {
        super(x, y, width, height);
        setVelocity(0.0, -Math.abs(speed));
    }

    @Override
    public void update(double dt) {
        move(dt);
    }

    public double bottom() {
        return y + height;
    }

    public double top() {
        return y;
    }

    public double left() {
        return x;
    }

    public double right() {
        return x + width;
    }
}
