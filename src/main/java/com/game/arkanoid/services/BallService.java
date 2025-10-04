package com.game.arkanoid.services;

import com.game.arkanoid.models.Ball;
import com.game.arkanoid.utils.Constants; // Cần thiết cho Math.sqrt

public class BallService {
    private final Ball ball;
    
    public BallService(Ball ball) {
        this.ball = ball;
    }

    public void moveBall() {
        double newX = ball.getCenterX() + ball.getDx();
        double newY = ball.getCenterY() + ball.getDy();
        ball.setCenter(newX, newY); // Ball Model tự cập nhật Node
    }
    
    //đổi hướng
    public void reflectX() {
        ball.setDx(-ball.getDx());
    }

    public void reflectY() {
        ball.setDy(-ball.getDy());
    }
    
    /**
     * Tính toán tốc độ 
     */
    public double getCurrentSpeed() {
        double dx = ball.getDx();
        double dy = ball.getDy();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Tăng/Giảm tốc độ 
     */
    public void scaleSpeed(double factor) {
        if (factor <= 0) return;
        ball.setDx(ball.getDx() * factor);
        ball.setDy(ball.getDy() * factor);
    }
    
    // Phương thức update chính (được gọi từ Controller)
    public void update() {
        //moveBall();
    }
    
    public Ball getBall() {
        return ball;
    }
}