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
     * @param p paddle
     * @param dt time delta for this frame (s)
     * @param worldW world width used to clamp the paddle within bounds
     */
    public void moveLeft(Paddle p, double dt, double worldW) {
        p.setVelocity(-p.getSpeed(), 0);
        p.update(dt);
        clampWithin(p, worldW);
        stop(p); // tránh drift sang frame sau
    }

    /**
     * Moves the paddle to the right for a single frame.
     * 
     * @param p paddle
     * @param dt time delta for this frame (s)
     * @param worldW world width used to clamp the paddle within bounds
     */
    public void moveRight(Paddle p, double dt, double worldW) {
        p.setVelocity(p.getSpeed(), 0);
        p.update(dt);
        clampWithin(p, worldW);
        stop(p);
    }

    /**
     * Clamps the paddle's horizontal position so it stays within the playfield.
     *
     * @param p      paddle to clamp
     * @param worldW world width used as the right boundary (left boundary is 0)
     */
    public void clampWithin(Paddle p, double worldW) {
        if (p.getX() < 0) {
            p.setX(0);
        }
        double maxX = worldW - p.getWidth();
        if (p.getX() > maxX) {
            p.setX(maxX);
        }
    }

    /**
     * Stop paddle.
     * @param p paddle to stop
     */
    public void stop(Paddle p) { 
        p.setVelocity(0, 0); 
    }

    /** 
     * Scale the width (power-ups and shi).
     * @param p paddle to scale
     * @param factor scale
     * @param worldW world width used to clamp the paddle after scaling
     */
    public void scaleWidth(Paddle p, double factor, double worldW) {
        p.setWidthClamped(p.getWidth() * factor);
        clampWithin(p, worldW);
    }
}
