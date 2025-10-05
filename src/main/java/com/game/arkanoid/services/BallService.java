package com.game.arkanoid.services;

import com.game.arkanoid.models.Ball;
import com.game.arkanoid.models.Paddle;

public class BallService {
    private final Ball ball;

    public BallService(Ball ball) {
        this.ball = ball;
    }

    public Ball getBall() {
        return ball;
    }

    public void update(double paneWidth, double paneHeight) {
        if (!ball.isMoving()) return;

        double newX = ball.getX() + ball.getDx();
        double newY = ball.getY() + ball.getDy();

        // kiểm tra va chạm tường dựa theo kích thước truyền vào
        if (newX - ball.getRadius() <= 0 || newX + ball.getRadius() >= paneWidth) {
            ball.setDx(-ball.getDx());
        }

        if (newY - ball.getRadius() <= 0) {
            ball.setDy(-ball.getDy());
        }

        if (newY + ball.getRadius() >= paneHeight) {
            ball.setMoving(false);
            System.out.println("Ball fell! Reset or lose a life.");
            return;
        }

        ball.setX(newX);
        ball.setY(newY);
    }

    public void start() {
        ball.setMoving(true);
    }

    public void stop()  {
        ball.setMoving(false);
    }

    public void reset(double x, double y) {
        ball.setX(x);
        ball.setY(y);
        ball.setDx(3);
        ball.setDy(-3);
        ball.setMoving(false);
    }

    public void dockTo(Paddle paddle) {
        double cx = paddle.getX() + paddle.getWidth() / 2.0;
        double cy = paddle.getY() - this.ball.getRadius();
        this.ball.setCenter(cx, cy);
    }
}
