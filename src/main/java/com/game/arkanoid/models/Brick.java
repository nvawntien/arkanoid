package com.game.arkanoid.models;

public class Brick extends GameObject {
    
    private int health; // health of the brick

    public Brick(double x, double y, double width, double height, int initialHealth) {
        super(x, y, width, height);
        this.health = initialHealth;
    }

    @Override
    public void update(double dt) {
        // brick not moving, do nothing
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
      // brick silver is indestructible
        if (!isIndestructible()) {
            this.health = health;
        }
    }

    /**
     * Check if the brick is destroyed.
     * @return
     */
    public boolean isDestroyed() {
        return !isIndestructible() && health <= 0;
    }

    /**
     * Check if the brick is indestructible.
     * @return
     */
    public boolean isIndestructible() {
        return health == 9;
    }
}
