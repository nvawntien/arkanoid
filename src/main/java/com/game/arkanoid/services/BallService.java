package com.game.arkanoid.services;

import com.game.arkanoid.config.GameSettings;
import com.game.arkanoid.models.Ball;
import com.game.arkanoid.models.GameObject;
import com.game.arkanoid.models.Paddle;
import com.game.arkanoid.utils.Constants;

/**
 * Service class for handling ball-related logic in the game.
 */
public class BallService {

    public void launch(Ball ball) {
        if (ball.isMoving()) {
            return;
        }
        double speed = baseSpeed();
        double t = Math.toRadians(Constants.BALL_LAUNCH_ANGLE);
        ball.setVelocity(speed * Math.cos(t), -speed * Math.sin(t));
        ball.setMoving(true);
    }

    public void step(Ball ball, double dt) {
        ball.update(dt);
    }

    public void bounceWorld(Ball ball, double worldW, double worldH) {
        double r = ball.getRadius();
        if (ball.getCenterX() - r < 22) {
            ball.setCenter(r + 22, ball.getCenterY());
            ball.setVelocity(Math.abs(ball.getDx()) * Constants.BALL_RESTITUTION, ball.getDy());
        } else if (ball.getCenterX() + r > worldW - 22) {
            ball.setCenter(worldW - 22 - r, ball.getCenterY());
            ball.setVelocity(-Math.abs(ball.getDx()) * Constants.BALL_RESTITUTION, ball.getDy());
        }
        if (ball.getCenterY() - r < 172) {
            ball.setCenter(ball.getCenterX(), r + 172);
            ball.setVelocity(ball.getDx(), Math.abs(ball.getDy()) * Constants.BALL_RESTITUTION);
        }
    }

    public boolean fellBelow(Ball ball, double worldH) {
        return ball.getCenterY() - ball.getRadius() > worldH;
    }

    public void resetOnPaddle(Ball ball, Paddle paddle) {
        ball.setMoving(false);
        ball.setVelocity(0, 0);
        ball.setCenter(
                paddle.getX() + paddle.getWidth() / 2.0,
                paddle.getY() - ball.getRadius() - Constants.BALL_SPAWN_OFFSET
        );
    }

    /**
     * Handles bounce behavior when the ball collides with another object.
     * Includes paddle motion influence for realistic deflection.
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
                double paddleDx = paddle.getDx();

                // Influence constant — tweak for responsiveness
                final double influence = 0.35;

                // Add paddle velocity influence
                vx += paddleDx * influence;

                // Normalize to keep overall speed consistent
                double speed = Math.sqrt(vx * vx + vy * vy);
                double base = baseSpeed();
                double scale = base / speed;
                vx *= scale;
                vy *= scale;

                // Clamp outgoing angle to stay within limits
                double angle = Math.toDegrees(Math.atan2(-vy, vx));
                angle = Math.max(Constants.MIN_BALL_ANGLE, Math.min(Constants.MAX_BALL_ANGLE, angle));

                double rad = Math.toRadians(angle);
                vx = base * Math.cos(rad);
                vy = -base * Math.sin(rad);
            }

            ball.setVelocity(vx, vy);
        } else {
            ball.setCenter(ball.getX(), oB + ball.getRadius() + Constants.BALL_NUDGE);
            ball.setVelocity(ball.getDx(), Math.abs(ball.getDy()) * Constants.BALL_RESTITUTION);
        }

        ensureMinimumSpeed(ball);
    }

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

    private void ensureMinimumSpeed(Ball ball) {
        double speed = Math.hypot(ball.getDx(), ball.getDy());
        if (speed < 1e-3) {
            double target = baseSpeed();
            double t = Math.toRadians(Constants.BALL_LAUNCH_ANGLE);
            ball.setVelocity(target * Math.cos(t), -target * Math.sin(t));
        }
    }

    private double baseSpeed() {
        return Constants.BALL_SPEED * GameSettings.getBallSpeedMultiplier();
    }
}
