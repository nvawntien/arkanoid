// src/main/java/com/game/arkanoid/services/PaddleService.java
package com.game.arkanoid.services;

import com.game.arkanoid.models.Paddle;


/**
 * Service class for handling paddle-related logic in the game.
 * 
 * @author bmgnxn
 */
public class PaddleService {

    public PaddleService() {}

    /**
     * Moves the paddle to the left for a single frame.
     * 
     * @param paddle paddle
     * @param dt time delta for this frame (s)
     * @param worldW world width used to clamp the paddle within bounds
     */
    public void moveLeft(Paddle paddle, double dt, double worldW) {
        paddle.setVelocity(-paddle.getSpeed(), 0);
        paddle.update(dt);
        clampWithin(paddle, worldW);
        stop(paddle); // tránh drift sang frame sau
    }

    /**
     * Moves the paddle to the right for a single frame.
     * 
     * @param paddle Paddle
     * @param dt time delta for this frame (s)
     * @param worldW world width used to clamp the paddle within bounds.
     */
    public void moveRight(Paddle paddle, double dt, double worldW) {
        paddle.setVelocity(paddle.getSpeed(), 0);
        paddle.update(dt);
        clampWithin(paddle, worldW);
        stop(paddle);
    }

    /**
     * Clamps the paddle's horizontal position so it stays within the playfield.
     *
     * @param paddle      paddle to clamp
     * @param worldW world width used as the right boundary (left boundary is 0)
     */
    public void clampWithin(Paddle paddle, double worldW) {
        if (paddle.getX() < 22) {
            paddle.setX(22);
        }
        double maxX = worldW - paddle.getWidth()-22;
        if (paddle.getX() > maxX) {
            paddle.setX(maxX);
        }
    }

    /**
     * Stop paddle.
     * @param paddle paddle to stop
     */
    public void stop(Paddle paddle) {
        paddle.setVelocity(0, 0);
    }

    /** 
     * Scale the width (power-ups and shi).
     * @param paddle paddle to scale
     * @param factor scale
     * @param worldW world width used to clamp the paddle after scaling
     */

    public void scaleWidth(Paddle paddle, double factor, double worldW) {
        paddle.setWidthClamped(paddle.getWidth() * factor);
        clampWithin(paddle, worldW);
    }

}
