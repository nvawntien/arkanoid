package com.game.arkanoid.services;

import com.game.arkanoid.models.Paddle;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PaddleServiceTest {

    @Test
    void moveLeftRightClamped() {
        PaddleService svc = new PaddleService();
        Paddle p = new Paddle(22, 300, 100, 20, 200);

        double worldW = 600;
        svc.moveLeft(p, 1.0, worldW);
        assertEquals(22, p.getX(), 1e-6, "Left bound should clamp at margin");

        p.setX(worldW);
        svc.moveRight(p, 1.0, worldW);
        assertTrue(p.getX() <= worldW - p.getWidth() - 22 + 1e-6);
    }
}

