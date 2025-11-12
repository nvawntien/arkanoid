package com.game.arkanoid.services;

import com.game.arkanoid.config.GameSettings;
import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.events.sound.WallHitSoundEvent;
import com.game.arkanoid.models.Ball;
import com.game.arkanoid.models.GameObject;
import com.game.arkanoid.models.Paddle;
import com.game.arkanoid.utils.Constants;

/**
 * Service class responsible for handling all ball-related logic in the game.
 * Includes launching, stepping, bouncing, collision detection, and interactions with paddle and world boundaries.
 */
public class BallService {

    /**
     * Launches a stationary ball with initial velocity at a predefined angle.
     * If the ball is already moving, this method does nothing.
     *
     * @param ball the ball to be launched
     */
    public void launch(Ball ball) {
        if (ball.isMoving()) {
            return;
        }
        double speed = baseSpeed();
        double t = Math.toRadians(Constants.BALL_LAUNCH_ANGLE);
        ball.setVelocity(speed * Math.cos(t), -speed * Math.sin(t));
        ball.setMoving(true);
        ball.setStuck(false);
        ball.setStuckOffsetX(0.0);
    }

    /**
     * Updates the ball's position based on its current velocity and the elapsed time.
     *
     * @param ball the ball to update
     * @param dt   the delta time in milliseconds
     */
    public void step(Ball ball, double dt) {
        ball.update(dt);
    }

    /**
     * Handles bouncing off the world boundaries (walls, ceiling).
     * Adjusts ball's position and velocity according to restitution.
     * Publishes a WallHitSoundEvent if the ball hits any wall.
     *
     * @param ball   the ball to check for boundary collisions
     * @param worldW the width of the game world
     * @param worldH the height of the game world
     */
    public void bounceWorld(Ball ball, double worldW, double worldH) {
        double r = ball.getRadius();
        boolean check = false;
        if (ball.getCenterX() - r < 22) {
            ball.setCenter(r + 22, ball.getCenterY());
            ball.setVelocity(Math.abs(ball.getDx()) * Constants.BALL_RESTITUTION, ball.getDy());
            check = true;
        } else if (ball.getCenterX() + r > worldW - 22) {
            ball.setCenter(worldW - 22 - r, ball.getCenterY());
            ball.setVelocity(-Math.abs(ball.getDx()) * Constants.BALL_RESTITUTION, ball.getDy());
            check = true;
        }
        if (ball.getCenterY() - r < 172) {
            ball.setCenter(ball.getCenterX(), r + 172);
            ball.setVelocity(ball.getDx(), Math.abs(ball.getDy()) * Constants.BALL_RESTITUTION);
            check = true;
        }
        if (check) GameEventBus.getInstance().publish(new WallHitSoundEvent());
    }

    /**
     * Checks if the ball has fallen below the bottom of the world.
     *
     * @param ball   the ball to check
     * @param worldH the height of the game world
     * @return true if the ball fell below the bottom boundary
     */
    public boolean fellBelow(Ball ball, double worldH) {
        return ball.getCenterY() - ball.getRadius() > worldH;
    }

    /**
     * Resets the ball on top of the paddle.
     * Stops its movement and sets the ball's position relative to the paddle.
     *
     * @param ball   the ball to reset
     * @param paddle the paddle on which to place the ball
     */
    public void resetOnPaddle(Ball ball, Paddle paddle) {
        ball.setMoving(false);
        ball.setStuck(false);
        ball.setStuckOffsetX(0.0);
        ball.setVelocity(0, 0);
        ball.setCenter(
                paddle.getX() + paddle.getWidth() / 2.0,
                paddle.getY() - ball.getRadius() - Constants.BALL_SPAWN_OFFSET
        );
    }

    /**
     * Handles collision response when the ball hits another GameObject.
     * Calculates penetration, updates velocity and position, and applies paddle motion influence if colliding with a paddle.
     *
     * @param ball  the ball involved in the collision
     * @param other the other game object collided with
     */
    public void bounceOff(Ball ball, GameObject other) {
        double oL = other.getX();
        double oT = other.getY();
        double oR = oL + other.getWidth();
        double oB = oT + other.getHeight();

        double penLeft = Math.abs(ball.right() - oL);
        double penRight = Math.abs(oR - ball.left());
        double penTop = Math.abs(ball.bottom() - oT);
        double penBottom = Math.abs(oB - ball.top());

        double minPen = Math.min(Math.min(penLeft, penRight), Math.min(penTop, penBottom));

        if (minPen == penLeft) {
            ball.setCenter(oL - ball.getRadius() - Constants.BALL_NUDGE, ball.getY());
            ball.setVelocity(-Math.abs(ball.getDx()) * Constants.BALL_RESTITUTION, ball.getDy());
        } else if (minPen == penRight) {
            ball.setCenter(oR + ball.getRadius() + Constants.BALL_NUDGE, ball.getY());
            ball.setVelocity(Math.abs(ball.getDx()) * Constants.BALL_RESTITUTION, ball.getDy());
        } else if (minPen == penTop) {
            // --- Paddle collision with influence ---
            ball.setCenter(ball.getX(), oT - ball.getRadius() - Constants.BALL_NUDGE);

            double vx = ball.getDx();
            double vy = -Math.abs(ball.getDy()) * Constants.BALL_RESTITUTION;

            if (other instanceof Paddle) {
                Paddle paddle = (Paddle) other;
                if (ball.isStuck()) {
                    double paddleX = paddle.getX();
                    double paddleW = paddle.getWidth();
                    double collisionX = Math.max(paddleX + ball.getRadius(), Math.min(ball.getCenterX(), paddleX + paddleW - ball.getRadius()));
                    ball.setCenter(collisionX, oT - ball.getRadius() - Constants.BALL_NUDGE);
                    ball.setVelocity(0.0, 0.0);
                    ball.setMoving(false);
                    ball.setStuck(true);
                    ball.setStuckOffsetX(collisionX - paddleX);
                    return;
                }

                ball.setCenter(ball.getX(), oT - ball.getRadius() - Constants.BALL_NUDGE);

                double paddleX = paddle.getX();
                double paddleW = paddle.getWidth();
                double paddleH = paddle.getHeight();
                double ballX = ball.getCenterX();

                double relX = (ballX - paddleX) / paddleW;
                double offset = (relX - 0.5) * 2.0;

                double cornerRadius = paddleH / 2.0;
                boolean hitLeftCorner = (ballX < paddleX + cornerRadius);
                boolean hitRightCorner = (ballX > paddleX + paddleW - cornerRadius);

                double minAngle = Math.toRadians(Constants.MIN_BALL_ANGLE);
                double maxAngle = Math.toRadians(Constants.MAX_BALL_ANGLE);
                double angle;

                if (hitLeftCorner) {
                    angle = maxAngle - Math.abs(offset) * (maxAngle - minAngle);
                    angle = Math.min(maxAngle, angle);
                } else if (hitRightCorner) {
                    angle = maxAngle - Math.abs(offset) * (maxAngle - minAngle);
                    angle = Math.min(maxAngle, angle);
                } else {
                    angle = minAngle + (maxAngle - minAngle) * Math.abs(offset);
                }

                double paddleDx = paddle.getDx();
                double influence = 0.25;
                angle += paddleDx * influence * 0.01;

                angle = Math.max(minAngle, Math.min(maxAngle, angle));

                double speed = baseSpeed();
                vx = speed * Math.sin(angle) * Math.signum(offset);
                vy = -speed * Math.cos(angle);

                ball.setVelocity(vx, vy);
            }

            ball.setVelocity(vx, vy);
        } else {
            ball.setCenter(ball.getX(), oB + ball.getRadius() + Constants.BALL_NUDGE);
            ball.setVelocity(ball.getDx(), Math.abs(ball.getDy()) * Constants.BALL_RESTITUTION);
        }

        ensureMinimumSpeed(ball);
    }

    /**
     * Checks if the ball is currently colliding with a given GameObject.
     *
     * @param ball  the ball to check
     * @param other the other object to test collision against
     * @return true if the ball collides with the other object
     */
    public boolean checkCollision(Ball ball, GameObject other) {
        double oL = other.getX();
        double oT = other.getY();
        double oR = oL + other.getWidth();
        double oB = oT + other.getHeight();
        double cx = ball.getCenterX();
        double cy = ball.getCenterY();
        double nearestX = Math.max(oL, Math.min(cx, oR));
        double nearestY = Math.max(oT, Math.min(cy, oB));
        double ddx = cx - nearestX;
        double ddy = cy - nearestY;
        return ddx * ddx + ddy * ddy <= ball.getRadius() * ball.getRadius();
    }

    /**
     * Ensures the ball maintains a minimum speed.
     * If the speed is too small, reinitializes the velocity to the base speed and launch angle.
     *
     * @param ball the ball to adjust
     */
    private void ensureMinimumSpeed(Ball ball) {
        double speed = Math.hypot(ball.getDx(), ball.getDy());
        if (speed < 1e-3) {
            double target = baseSpeed();
            double t = Math.toRadians(Constants.BALL_LAUNCH_ANGLE);
            ball.setVelocity(target * Math.cos(t), -target * Math.sin(t));
        }
    }

    /**
     * Returns the base speed of the ball, modified by game settings.
     *
     * @return base speed for the ball
     */
    private double baseSpeed() {
        return Constants.BALL_SPEED * GameSettings.getBallSpeedMultiplier();
    }
}
