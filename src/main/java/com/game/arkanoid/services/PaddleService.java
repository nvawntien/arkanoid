package com.game.arkanoid.services;

import com.game.arkanoid.models.Paddle;
import com.game.arkanoid.utils.Constants;

/**
 * Service class responsible for handling all paddle-related logic in the game.
 * This includes movement, clamping within bounds, resizing, and resetting position.
 * <p>
 * The paddle moves left/right based on player input and can be affected by power-ups.
 * </p>
 * 
 * @author bmgnxn
 */
public class PaddleService {

    /**
     * Default constructor.
     */
    public PaddleService() {}

    /**
     * Moves the paddle to the left for a single frame.
     * Velocity is applied, the paddle is updated, clamped within the world bounds, and stopped to prevent drift.
     *
     * @param paddle The paddle to move.
     * @param dt Time delta for this frame (seconds).
     * @param worldW Width of the game world used to clamp the paddle.
     */
    public void moveLeft(Paddle paddle, double dt, double worldW) {
        paddle.setVelocity(-paddle.getSpeed(), 0);
        paddle.update(dt);
        clampWithin(paddle, worldW);
        stop(paddle); // avoid drift in next frame
    }

    /**
     * Moves the paddle to the right for a single frame.
     * Velocity is applied, the paddle is updated, clamped within the world bounds, and stopped to prevent drift.
     *
     * @param paddle The paddle to move.
     * @param dt Time delta for this frame (seconds).
     * @param worldW Width of the game world used to clamp the paddle.
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
     * @param paddle The paddle to clamp.
     * @param worldW Width of the game world; used as the right boundary (left boundary is 0).
     */
    public void clampWithin(Paddle paddle, double worldW) {
        if (paddle.getX() < 22) {
            paddle.setX(22);
        }
        double maxX = worldW - paddle.getWidth() - 22;
        if (paddle.getX() > maxX) {
            paddle.setX(maxX);
        }
    }

    /**
     * Stops the paddle's movement by setting velocity to zero.
     *
     * @param paddle The paddle to stop.
     */
    public void stop(Paddle paddle) {
        paddle.setVelocity(0, 0);
    }

    /**
     * Scales the paddle's width by a given factor, typically due to power-ups.
     * After scaling, the paddle is clamped within the world bounds.
     *
     * @param paddle The paddle to scale.
     * @param factor Scaling factor for the width.
     * @param worldW Width of the game world used for clamping.
     */
    public void scaleWidth(Paddle paddle, double factor, double worldW) {
        paddle.setWidthClamped(paddle.getWidth() * factor);
        clampWithin(paddle, worldW);
    }

    /**
     * Resets the paddle to its default position at the bottom center of the playfield.
     *
     * @param paddle The paddle to reset.
     */
    public void resetPaddlePosition(Paddle paddle) {
        paddle.setX(Constants.GAME_WIDTH / 2.0 - paddle.getWidth() / 2.0);
        paddle.setY(Constants.GAME_HEIGHT - Constants.PADDLE_HEIGHT - Constants.PADDLE_MARGIN_BOTTOM);
    }

}
