package com.game.arkanoid.models;

public class Brick extends GameObject {

    private int health; // số máu còn lại của brick

    public Brick(double x, double y, double width, double height, int initialHealth) {
        super(x, y, width, height);
        this.health = initialHealth;
    }

    @Override
    public void update(double dt) {
        // brick không update theo thời gian
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        // brick silver (health = 9) không giảm
        if (!isIndestructible()) {
            this.health = health;
        }
    }

    /** 
     * Brick bị phá khi health <= 0, trừ brick silver 
     */
    public boolean isDestroyed() {
        return !isIndestructible() && health <= 0;
    }

    /** 
     * Brick silver (bất tử) là health = 9
     */
    public boolean isIndestructible() {
        return health == 9;
    }
}
