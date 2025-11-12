package com.game.arkanoid.services;

import com.game.arkanoid.models.Ball;
import com.game.arkanoid.models.Paddle;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BallServiceTest {

    @Test
    void launchSetsVelocityAndMoving() {
        BallService svc = new BallService();
        Ball ball = new Ball(100, 200, 8);
        assertFalse(ball.isMoving());
        svc.launch(ball);
        assertTrue(ball.isMoving());
        assertNotEquals(0.0, ball.getDx(), 1e-9);
        assertNotEquals(0.0, ball.getDy(), 1e-9);
    }

    @Test
    void resetOnPaddlePositionsBall() {
        BallService svc = new BallService();
        Ball ball = new Ball(10, 10, 8);
        Paddle p = new Paddle(100, 400, 120, 20, 200);
        svc.resetOnPaddle(ball, p);
        assertFalse(ball.isMoving());
        assertEquals(p.getY() - ball.getRadius() - com.game.arkanoid.utils.Constants.BALL_SPAWN_OFFSET,
                ball.getCenterY(), 1e-6);
    }
}

