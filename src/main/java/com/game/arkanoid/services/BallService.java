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
        ball.setStuck(false);
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
                if (ball.isStuck()) {
                    // Nếu bóng đã được "catch" bởi power-up: gắn bóng vào vị trí va chạm trên paddle
                    Paddle paddle = (Paddle) other;
                    double paddleX = paddle.getX();
                    double paddleW = paddle.getWidth();
                    // Giữ bóng nằm trong biên paddle (theo trục X)
                    double collisionX = Math.max(paddleX + ball.getRadius(), Math.min(ball.getCenterX(), paddleX + paddleW - ball.getRadius()));
                    // Đặt bóng lên trên paddle, dừng chuyển động và ghi offset để theo paddle
                    ball.setCenter(collisionX, oT - ball.getRadius() - Constants.BALL_NUDGE);
                    ball.setVelocity(0.0, 0.0);
                    ball.setMoving(false);
                    ball.setStuck(true);
                    ball.setStuckOffsetX(collisionX - paddleX);
                    return;
                }
                Paddle paddle = (Paddle) other;

                // Đưa bóng ra khỏi paddle một chút
                ball.setCenter(ball.getX(), oT - ball.getRadius() - Constants.BALL_NUDGE);

                double paddleX = paddle.getX();
                double paddleW = paddle.getWidth();
                double paddleH = paddle.getHeight();

                double ballX = ball.getCenterX();

                // --- 1. Tính vị trí va chạm tương đối ---
                double relX = (ballX - paddleX) / paddleW; // 0..1
                double offset = (relX - 0.5) * 2.0;        // -1 (trái) .. +1 (phải)

                // --- 2. Phát hiện va chạm ở góc cong ---
                double cornerRadius = paddleH / 2.0; // bán kính giả định góc cong
                boolean hitLeftCorner = (ballX < paddleX + cornerRadius);
                boolean hitRightCorner = (ballX > paddleX + paddleW - cornerRadius);

                // --- 3. Tính góc nảy theo vật lý ---
                double minAngle = Math.toRadians(Constants.MIN_BALL_ANGLE); // vd: 35°
                double maxAngle = Math.toRadians(Constants.MAX_BALL_ANGLE); // vd: 75°
                double angle;

                if (hitLeftCorner) {
                    // góc trái cong → nảy về trái, góc lớn hơn
                    angle = maxAngle - Math.abs(offset) * (maxAngle - minAngle);
                    angle = Math.min(maxAngle, angle);
                } else if (hitRightCorner) {
                    // góc phải cong → nảy về phải
                    angle = maxAngle - Math.abs(offset) * (maxAngle - minAngle);
                    angle = Math.min(maxAngle, angle);
                } else {
                    // giữa paddle → góc gần vuông lên trên
                    angle = minAngle + (maxAngle - minAngle) * Math.abs(offset);
                }

                // --- 4. Ảnh hưởng vận tốc paddle ---
                double paddleDx = paddle.getDx();
                double influence = 0.25; // hệ số ảnh hưởng paddle
                angle += paddleDx * influence * 0.01; // tăng/giảm nhẹ theo hướng paddle

                // Giới hạn lại góc
                angle = Math.max(minAngle, Math.min(maxAngle, angle));

                // --- 5. Tính vận tốc mới ---
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
