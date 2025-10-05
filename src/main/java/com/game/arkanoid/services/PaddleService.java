package com.game.arkanoid.services;
import com.game.arkanoid.models.Paddle;
import com.game.arkanoid.utils.Constants;


public class PaddleService {
     private final Paddle paddle;

     public PaddleService(Paddle paddle) {
        this.paddle = paddle;
    }

    public void moveLeft() {
        double newX = paddle.getX() - paddle.getSpeed();
        if (newX < 0) {
            newX = 0;
        }
        paddle.setX(newX);
    }

    public void moveRight() {
        double newX = paddle.getX() + paddle.getSpeed();
       
        if (newX + paddle.getWidth() > Constants.GAME_WIDTH) {
            newX = Constants.GAME_WIDTH - paddle.getWidth();
        }
        paddle.setX(newX);
    }
    public void scaleSpeed(double factor) {
        if (factor > 0) {
            paddle.setSpeed(paddle.getSpeed() * factor);
        }
    }
    public void reset() {
        paddle.setX(Constants.GAME_WIDTH/2-Constants.PADDLE_WIDTH/2);
        paddle.setY(Constants.GAME_HEIGHT - 50);
        paddle.setWidth(Constants.PADDLE_WIDTH);
        paddle.setSpeed(Constants.PADDLE_SPEED);
    }
    public Paddle getPaddle() {
        return paddle;
    }
}
