package com.game.arkanoid.models;

/**
 * Falling collectible spawned from destroyed bricks.
 */
public final class PowerUp extends MovableObject {

    private final PowerUpType type;
    private boolean collected;

    public PowerUp(PowerUpType type, double x, double y, double width, double height, double fallSpeed) {
        super(x, y, width, height);
        this.type = type;
        setVelocity(0, fallSpeed);
    }

    public PowerUpType getType() {
        return type;
    }

    public boolean isCollected() {
        return collected;
    }

    public void markCollected() {
        this.collected = true;
    }

    @Override
    public void update(double dt) {
        move(dt);
    }
}
