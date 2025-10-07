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
     * @param ball the ball to launch
     */
    public void launch(Ball ball) {
        if (ball.isMoving()) {
            return;
        }

        double t = Math.toRadians(Constants.BALL_LAUNCH_ANGLE);
        ball.setVelocity(Constants.BALL_SPEED * Math.cos(t), -Constants.BALL_SPEED * Math.sin(t));
        ball.setMoving(true);
    }

    /**
     * Updates the ball's position based on its velocity and time delta.
     * 
     * @param ball the ball
     * @param dt time delta (seconds)
     */
    public void step(Ball ball, double dt) {
        ball.update(dt);
    }

    /**
     * Handles ball collisions with world boundaries (left, right, top).
     * 
     * @param ball the ball
     * @param worldW world width
     * @param worldH world height
     */
    public void bounceWorld(Ball ball, double worldW, double worldH) {
        double r = ball.getRadius();
        if (ball.getCenterX() - r < 0) {
            ball.setCenter(r, ball.getCenterY());
            ball.setVelocity(Math.abs(ball.getDx()) * Constants.BALL_RESTITUTION, ball.getDy());
        } else if (ball.getCenterX() + r > worldW) {
            ball.setCenter(worldW - r, ball.getCenterY());
            ball.setVelocity(-Math.abs(ball.getDx()) * Constants.BALL_RESTITUTION, ball.getDy());
        }
        if (ball.getCenterY() - r < 0) {
            ball.setCenter(ball.getCenterX(), r);
            ball.setVelocity(ball.getDx(), Math.abs(ball.getDy()) * Constants.BALL_RESTITUTION);
        }
    }

    /**
     * Checks if the ball has fallen below the bottom of the world.
     * 
     * @param ball the ball
     * @param worldH world height
     * @return true if the ball is below the screen
     */
    public boolean fellBelow(Ball ball, double worldH) {
        return ball.getCenterY() - ball.getRadius() > worldH;
    }

    /**
     * Resets the ball to rest on top of the paddle.
     * 
     * @param ball the ball
     * @param paddle the paddle
     */
    public void resetOnPaddle(Ball ball, Paddle paddle) {
        ball.setMoving(false);
        ball.setVelocity(0, 0);
        ball.setCenter(
                paddle.getX() + paddle.getWidth() / 2.0,
                paddle.getY() - ball.getRadius() - Constants.BALL_SPAWN_OFFSET
        );
    }


    /**
     * Check if ball is docked to paddle
     * @param ball ball
     * @param paddle paddle
     */
    public void dockToPaddle(Ball ball, Paddle paddle) {
            ball.setMoving(false);
            ball.setVelocity(0, 0);
            ball.setCenter(
                        paddle.getX() + paddle.getWidth() / 2.0,
                        paddle.getY() - ball.getRadius() - Constants.BALL_SPAWN_OFFSET
            );

    }

    /** Nảy khỏi AABB theo trục có độ xuyên nhỏ nhất (đơn giản, ổn định). */

    public void bounceOff(Ball ball, GameObject other) {
        double oL = other.getX();
        double oT = other.getY();
        double oR = oL + other.getWidth();
        double oB = oT + other.getHeight();

        double penLeft   = Math.abs(ball.right() - oL);
        double penRight  = Math.abs(oR - ball.left());
        double penTop    = Math.abs(ball.bottom() - oT);
        double penBottom = Math.abs(oB - ball.top());

        double minPen = Math.min(Math.min(penLeft, penRight), Math.min(penTop, penBottom));

        if (minPen == penLeft) {
            ball.setCenter(oL - ball.getRadius() - Constants.BALL_NUDGE, ball.getY());
            ball.setVelocity(-Math.abs(ball.getDx()) * Constants.BALL_RESTITUTION, ball.getDy());
        } else if (minPen == penRight) {
            ball.setCenter(oR + ball.getRadius() + Constants.BALL_NUDGE, ball.getY());
            ball.setVelocity(Math.abs(ball.getDx()) * Constants.BALL_RESTITUTION, ball.getDy());
        } else if (minPen == penTop) {
            ball.setCenter(ball.getX(), oT - ball.getRadius() - Constants.BALL_NUDGE);
            ball.setVelocity(ball.getDx(), -Math.abs(ball.getDy()) * Constants.BALL_RESTITUTION);
        } else {
            ball.setCenter(ball.getX(), oB + ball.getRadius() + Constants.BALL_NUDGE);
            ball.setVelocity(ball.getDx(), Math.abs(ball.getDy()) * Constants.BALL_RESTITUTION);
        }

        double speed = Math.hypot(ball.getDx(), ball.getDy());
        if (speed < 1e-3) { // tránh “đứng chết”
            double t = Math.toRadians(Constants.BALL_LAUNCH_ANGLE);
            ball.setVelocity(Constants.BALL_SPEED * Math.cos(t), -Constants.BALL_SPEED * Math.sin(t));
        }
    }

    public boolean intersectsAABB(Ball ball, GameObject other) {
    double oL = other.getX(), oT = other.getY();
    double oR = oL + other.getWidth(), oB = oT + other.getHeight();
    double cx = ball.getCenterX(), cy = ball.getCenterY();
    double nearestX = Math.max(oL, Math.min(cx, oR));
    double nearestY = Math.max(oT, Math.min(cy, oB));
    double ddx = cx - nearestX, ddy = cy - nearestY;
    return ddx*ddx + ddy*ddy <= ball.getRadius() * ball.getRadius();
}

    public void bounceOffAABB(Ball ball, GameObject other) {
        double oL = other.getX(), oT = other.getY();
        double oR = oL + other.getWidth(), oB = oT + other.getHeight();

        double penLeft   = Math.abs((ball.getCenterX() + ball.getRadius()) - oL);
        double penRight  = Math.abs(oR - (ball.getCenterX() - ball.getRadius()));
        double penTop    = Math.abs((ball.getCenterY() + ball.getRadius()) - oT);
        double penBottom = Math.abs(oB - (ball.getCenterY() - ball.getRadius()));
        double minPen = Math.min(Math.min(penLeft, penRight), Math.min(penTop, penBottom));

        if (minPen == penLeft) {
            ball.setCenter(oL - ball.getRadius() - Constants.BALL_NUDGE, ball.getCenterY());
              ball.setVelocity(-Math.abs(ball.getDx()) * Constants.BALL_RESTITUTION, ball.getDy());
        } else if (minPen == penRight) {
            ball.setCenter(oR + ball.getRadius() + Constants.BALL_NUDGE, ball.getCenterY());
            ball.setVelocity(Math.abs(ball.getDx()) * Constants.BALL_RESTITUTION, ball.getDy());
        } else if (minPen == penTop) {
            ball.setCenter(ball.getCenterX(), oT - ball.getRadius() - Constants.BALL_NUDGE);
            ball.setVelocity(ball.getDx(), -Math.abs(ball.getDy()) * Constants.BALL_RESTITUTION);
        } else {
            ball.setCenter(ball.getCenterX(), oB + ball.getRadius() + Constants.BALL_NUDGE);
            ball.setVelocity(ball.getDx(), Math.abs(ball.getDy()) * Constants.BALL_RESTITUTION);
        }

        double speed = Math.hypot(ball.getDx(), ball.getDy());
        if (speed < 1e-3) {
            double t = Math.toRadians(Constants.BALL_LAUNCH_ANGLE);
            ball.setVelocity(Constants.BALL_SPEED * Math.cos(t), -Constants.BALL_SPEED * Math.sin(t));
        }
    }

    /** Ball vs AABB (Paddle/Brick) – test va chạm hình tròn với hộp. */

    public boolean checkCollision(Ball ball, GameObject other) {
        double oL = other.getX();
        double oT = other.getY();
        double oR = oL + other.getWidth();
        double oB = oT + other.getHeight();

        double cx = ball.getCenterX(), cy = ball.getCenterY();
        double nearestX = Math.max(oL, Math.min(cx, oR));
        double nearestY = Math.max(oT, Math.min(cy, oB));
        double ddx = cx - nearestX, ddy = cy - nearestY;
        return ddx*ddx + ddy*ddy <= ball.getRadius() * ball.getRadius();
    }
}
