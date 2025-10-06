package com.game.arkanoid.services;

import com.game.arkanoid.models.Ball;
import com.game.arkanoid.models.GameObject;
import com.game.arkanoid.models.Paddle;
import com.game.arkanoid.utils.Constants;

/**
 * Service class for handling ball-related logic in the game.
 * 
 * @author bmngxn
 */
public class BallService {

    public BallService() {}

    /**
     * Launches the ball at a fixed angle if it's not already moving.
     * 
     * @param b the ball to launch
     */
    public void launch(Ball b) {
        if (b.isMoving()) {
            return;
        }

        double t = Math.toRadians(Constants.BALL_LAUNCH_ANGLE);
        b.setVelocity(Constants.BALL_SPEED * Math.cos(t), -Constants.BALL_SPEED * Math.sin(t));
        b.setMoving(true);
    }

    /**
     * Updates the ball's position based on its velocity and time delta.
     * 
     * @param b the ball
     * @param dt time delta (seconds)
     */
    public void step(Ball b, double dt) { 
        b.update(dt); 
    }

    /**
     * Handles ball collisions with world boundaries (left, right, top).
     * 
     * @param b the ball
     * @param worldW world width
     * @param worldH world height
     */
    public void bounceWorld(Ball b, double worldW, double worldH) {
        double r = b.getRadius();
        if (b.getCenterX() - r < 0) {
            b.setCenter(r, b.getCenterY());
            b.setVelocity(Math.abs(b.getDx()) * Constants.BALL_RESTITUTION, b.getDy());
        } else if (b.getCenterX() + r > worldW) {
            b.setCenter(worldW - r, b.getCenterY());
            b.setVelocity(-Math.abs(b.getDx()) * Constants.BALL_RESTITUTION, b.getDy());
        }
        if (b.getCenterY() - r < 0) {
            b.setCenter(b.getCenterX(), r);
            b.setVelocity(b.getDx(), Math.abs(b.getDy()) * Constants.BALL_RESTITUTION);
        }
    }

    /**
     * Checks if the ball has fallen below the bottom of the world.
     * 
     * @param b the ball
     * @param worldH world height
     * @return true if the ball is below the screen
     */
    public boolean fellBelow(Ball b, double worldH) {
        return b.getCenterY() - b.getRadius() > worldH;
    }

    /**
     * Resets the ball to rest on top of the paddle.
     * 
     * @param b the ball
     * @param p the paddle
     */
    public void resetOnPaddle(Ball b, Paddle p) {
        b.setMoving(false);
        b.setVelocity(0, 0);
        b.setCenter(
            p.getX() + p.getWidth() / 2.0,
            p.getY() - b.getRadius() - Constants.BALL_SPAWN_OFFSET
        );
    }

    /**
     * Makes the ball bounce off a game object.
     * 
     * @param b the ball
     * @param other the object it collided with
     */
    public void bounceOff(Ball b, GameObject other) {
        b.bounceOff(other);
    }


    /**
     * Check if ball is docked to paddle
     * @param b ball
     * @param p paddle
     */
    public void dockToPaddle(Ball b, Paddle p) {
            b.setMoving(false);
            b.setVelocity(0, 0);
            b.setCenter(
                        p.getX() + p.getWidth() / 2.0,
                        p.getY() - b.getRadius() - Constants.BALL_SPAWN_OFFSET
            );

    }
}
