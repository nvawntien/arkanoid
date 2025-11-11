package com.game.arkanoid.models;

import com.game.arkanoid.utils.Constants;
/**
 * Enemy moving in zig-zag while falling.
 */
public final class Enemy extends MovableObject {

    private final EnemyType type;
    private double zigzagTimer;  
    
    public double getZigzagTimer() {
        return zigzagTimer;
    }

    public void setZigzagTimer(double zigzagTimer) {
        this.zigzagTimer = zigzagTimer;
    }

    public EnemyType getType() {
        return type;
    }

    public Enemy(EnemyType type, double x, double y, double width, double height, double speedX, double speedY) {
        super(x, y, width, height);
        setVelocity(speedX, speedY);
        this.zigzagTimer = 0;
        this.type = type;
    }

    @Override
    public void update(double dt) {
        // Zig-zag logic: đổi hướng theo dx sau mỗi khoảng thời gian
        zigzagTimer += dt;
        if (zigzagTimer >= Constants.ZIGZAG_INTERVAL) {
            dx = -dx;
            zigzagTimer = 0;
        }

        // Di chuyển theo dx, dy
        move(dt);
    }

    public double getVx() {
        return dx;
    }

    public void setVx(double dx) {
        this.dx = dx;
    }

    public void setVy(double dy) {
        this.dy = dy;
    }
    
    public double getVy() {
        return dy;
    }
}
